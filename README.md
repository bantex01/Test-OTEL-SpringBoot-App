# java-otel-test
Simple java app to run under the OTEL Java Agent to produce a metric, trace and log for testing purposes

Example:

java -javaagent:./opentelemetry-javaagent.jar -Dotel.service.name="java-test" -Dotel.exporter.otlp.endpoint="http://localhost:4318" -Dotel.exporter.otlp.protocol=http/protobuf -Dotel.logs.exporter=otlp -Dotel.traces.exporter=otlp -Dotel.metrics.exporter=otlp  -Dotel.javaagent.debug=true -jar app/build/libs/app-all.jar


