package com.example;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.StatusCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class App {
    // Create a Logger instance
    private static final Logger logger = LogManager.getLogger(App.class);

    private final Meter meter = GlobalOpenTelemetry.get().getMeter("com.example.App");
    private final LongCounter logCounter = meter
            .counterBuilder("java_test_log_messages")
            .setDescription("Counts the number of log messages sent")
            .setUnit("messages")
            .build();

    private final Tracer tracer = GlobalOpenTelemetry.getTracer("com.example.App");

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void logMessage() {
        // Create attributes to tag the metric with log level
        Attributes attributes = Attributes.builder()
                .put("log_level", "info")
                .build();

        Span iterationSpan = tracer.spanBuilder("iteration-span").startSpan();
        try {
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
    }
}
