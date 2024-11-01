# Test SpringBoot App

This repo houses 2 very simple java apps to test the ability to emit custom metrics and traces from the Open Telemetry java agent. In addition, the otel-log-example-springboot app implements an open telemetry log appender to send logging telemetry to an open telemetry target.

## otel-log-example-springboot-nocustomlogging

This app is instrumented to to emit a custom metric and trace. Logging is carried out by running the Open Telemetry java agent by setting:

```-Dotel.logs.exporter="some_otlp_target"```

This will tell the java agent to automatically intercept logging calls made to the logging framework and populate fields such as trace_id and span_id should the logging call be made within an active trace.

Details on the emission of custom metrics and traces can be found in those specific sections below in this REAMDE.

### Example start command

```java -javaagent:../../opentelemetry-javaagent.jar.2.5.0 -Dotel.service.name="java-test" -Dotel.exporter.otlp.endpoint="http://localhost:4318" -Dotel.exporter.otlp.protocol=http/protobuf -Dotel.logs.exporter=otlp -Dotel.traces.exporter=otlp -Dotel.metrics.exporter=otlp -Dotel.javaagent.debug=false -Dotel.instrumentation.log4j-appender.experimental.capture-mdc-attributes=\* -jar ./target/otel-log-example-springboot-nocustomlogging-1.0-SNAPSHOT.jar```

Important options to note:

| Option | Description |
| --- | --- |
| otel.instrumentation.log4j-appender.experimental.capture-mdc-attributes | You should set this option to enable the java agent to capture all of your log fields. Without setting this, you will only get a subset of logging fields. This example is assuming the use of log4j, for other logging frameworks see details [here](https://signoz.io/docs/userguide/collecting_application_logs_otel_sdk_java/#settings-for-appender-instrumentation-based-on-the-logging-library)

## otel-log-example-springboot

This app is instrumented to emit a custom metric and trace as well as implementing custom logging via the Open Telemetry log4j log appender. 

Details on the emission of custom metrics, traces and logging via the log appender can be found in those specific sections below in this REAMDE.

### Example start command

```java -javaagent:../../opentelemetry-javaagent.jar.2.5.0 -Dotel.service.name="java-test" -Dotel.exporter.otlp.endpoint="http://localhost:4318" -Dotel.exporter.otlp.protocol=http/protobuf -Dotel.logs.exporter=none -Dotel.metrics.exporter=otlp -Dotel.traces.exporter=otlp -Dotel.javaagent.debug=false -Dotel.instrumentation.log4j-appender.enabled=false -jar ./target/otel-log-example-springboot-1.0-SNAPSHOT.jar```

Important options to note:

| Option | Description |
| --- | --- |
| otel.logs.exporter | When integrating Open Telemetry via the log appender method it is important to set this value to "none" because you will manually initialise the exporter configuration in your code |
| otel.instrumentation.log4j-appender.enabled | You do not want the java agent intercepting logging, so you should set this value to false (this is an exampe using log4j as your logging framework, this option will differ if you are using a different logging framework, see details [here](www.google.com))

## Metrics

To manually instrument your code to produce custom metrics you should review the snippets below and the official documentation (linked below). If you are running your java application via the java agent, no other configuration should be required. The java agent will send metric telemetry to the target specified by ```otel.exporter.otlp.endpoint``` or ```otel.exporter.otlp.metrics.endpoint``` at startup

The code snippets in the app to produce custom metrics are:

```
private final Meter meter = GlobalOpenTelemetry.get().getMeter("com.example.App");
private final LongCounter logCounter = meter
        .counterBuilder("java_test_log_messages")
        .setDescription("Counts the number of log messages sent")
        .setUnit("messages")
        .build();
```

and 

```
Attributes attributes = Attributes.builder()
    .put("log_level", "info")
    .build();
```

and

```
logCounter.add(1, attributes);
```

Details on manually instrumenting your code to produce metrics can be found [here](https://opentelemetry.io/docs/languages/java/api-components/#meterprovider) and examples can be found [here](https://github.com/open-telemetry/opentelemetry-java-examples/tree/main/metrics).


## Traces

To manually instrument your code to produce custom traces you should review the snippets below and the official documentation (linked below). If you are running your java application via the java agent, no other configuration should be required. The java agent will send trace telemetry to the target specified by ```otel.exporter.otlp.endpoint``` or ```otel.exporter.otlp.traces.endpoint``` at startup.

The code snippets in the app to produce custom metrics are:

```
private final Tracer tracer = GlobalOpenTelemetry.getTracer("com.example.App");
```

and

```
Span iterationSpan = tracer.spanBuilder("iteration-span").startSpan();
    iterationSpan.makeCurrent();
    try {
        logger.info("Current ThreadContext map: {}", ThreadContext.getContext());
        logger.info("This is a test log message!");
        logCounter.add(1, attributes);
        logger.info("Incremented log_messages_sent counter!");
        iterationSpan.addEvent("Log message sent and counter incremented");
    } catch (Exception e) {
        iterationSpan.setStatus(StatusCode.ERROR, "Error occurred in iteration");
        logger.error("Error in iteration", e);
    } finally {
        iterationSpan.end();
    }
```

Details on manually instrumenting your code to produce traces can be found [here](https://opentelemetry.io/docs/languages/java/api-components/#tracerprovider) and examples can be found [here](https://github.com/open-telemetry/opentelemetry-java-examples/tree/main/manual-tracing).

## Logs

### otel-log-example-springboot-nocustomlogging

The otel-log-example-springboot-nocustomlogging app does not contain any specific instrumentation to produce logs. The java agent will intercept logging calls made and forward those logs on to the target specificed by ```otel.exporter.otlp.endpoint``` or ```otel.exporter.otlp.logs.endpoint``` at startup.

The list of libraries and frameworks suported can be found [here](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/supported-libraries.md). The example app in this repo is using log4j and so the specific setting used to ensure the agent captures all log fields is ```otel.instrumentation.log4j-appender.experimental.capture-mdc-attributes```. 

If you are using a different logging framework see the details [here](https://signoz.io/docs/userguide/collecting_application_logs_otel_sdk_java/#settings-for-appender-instrumentation-based-on-the-logging-library) to ensure the agent includes all your log fields.

### otel-log-example-springboot

The otel-log-example-springboot app implements the open telemetry log4j appender to push log data to an OTLP target.

The relevant code snippets in the code to integrate with the open telemetry log4j appender are:

```
    private static OpenTelemetry initializeOpenTelemetry() {
        OpenTelemetrySdk sdk =
            OpenTelemetrySdk.builder()
                .setTracerProvider(SdkTracerProvider.builder().setSampler(Sampler.alwaysOn()).build())
                .setLoggerProvider(
                    SdkLoggerProvider.builder()
                        .setResource(
                            Resource.getDefault().toBuilder()
                                .put(ResourceAttributes.SERVICE_NAME, "java-test")
                                .build())
                        .addLogRecordProcessor(
                            BatchLogRecordProcessor.builder(
                                    //OtlpGrpcLogRecordExporter.builder()
                                    OtlpHttpLogRecordExporter.builder()
                                        //.setEndpoint("http://localhost:14317")
                                        .setEndpoint("http://10.152.7.142:4318/v1/logs")
                                        .build())
                                .build())
                        .build())
                .build();

        // Add hook to close SDK, which flushes logs
        Runtime.getRuntime().addShutdownHook(new Thread(sdk::close));

        return sdk;
    }
```

You must manually initialise the open telemetry instance with the details of your exporter configuration. It's important to set a service name and that should match the service name you have used at agent startup unless you have a specific reason not to correlate logs with trace data at your back-end.

and

```
OpenTelemetry openTelemetry = initializeOpenTelemetry();
OpenTelemetryAppender.install(openTelemetry);
```

You can now issue logging commands as usual and the open telemetry log4j appender can be configured in your log4j2 configuration. This is the example used in the application:

```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="DEBUG" packages="io.opentelemetry.instrumentation.log4j.appender.v2_17">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <!--<PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - CONSOLE - %msg%n"/>-->
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - CONSOLE - trace_id=%X{trace_id} span_id=%X{span_id} trace_flags=%X{trace_flags} - %msg - %X%n"/>
        </Console>

        <OpenTelemetry name="OpenTelemetryAppender" captureMapMessageAttributes="true" captureMarkerAttribute="true" captureContextDataAttributes="*">
            <!-- Only allow ERROR level logs to OpenTelemetryAppender -->
            <!--<ThresholdFilter level="ERROR" onMatch="ACCEPT" onMismatch="DENY"/>-->
            <!-- Filter on regex of msg contents -->
            <!--<RegexFilter regex=".*routing_key=lightstep.*" onMatch="ACCEPT" onMismatch="DENY"/>-->
        <ThreadContextMapFilter>
            <KeyValuePair key="routing_key" value="lightstep"/>
        </ThreadContextMapFilter>
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} trace_id=%X{trace_id} span_id=%X{span_id} trace_flags=%X{trace_flags} - %msg - %X{routing_key}%n">
            </PatternLayout>    
        </OpenTelemetry>
    </Appenders>
    
    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="OpenTelemetryAppender"/>
        </Root>
    </Loggers>
</Configuration>
```

You should be able to configure your open telemetry appender using any standard techniques available in the log4j framework.

In the example above we are filtering only log entries containing the fields "routing_key" with a value of "lightstep".

Note: It's important that you don't have conflicting logging dependencies in your project. If you are seeing strange behaviour or your log4j configuration doesn't appear to be being honoured, it is more than likely you are experiencing a conflict. See the pom.xml file in these examples for guidance.

### Helpful Links

[Log Appender Examples](https://github.com/open-telemetry/opentelemetry-java-examples/tree/main/log-appender)

[General Java Open Telemetry code examples](https://github.com/open-telemetry/opentelemetry-java-examples?tab=readme-ov-file#java-opentelemetry-examples)

[Log4j Example](https://opentelemetry.io/docs/specs/otel/logs/supplementary-guidelines/#how-to-create-a-log4j-log-appender)

[Log4j Appender Instructions](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/instrumentation/log4j/log4j-appender-2.17/library/README.md)



