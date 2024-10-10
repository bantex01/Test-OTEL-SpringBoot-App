package com.example;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
    // Create a Logger instance
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) {

        // Obtain a Meter instance from OpenTelemetry
        Meter meter = GlobalOpenTelemetry.get().getMeter("com.example.App");

        // Create a counter to track the number of log messages sent
        LongCounter logCounter = meter
            .counterBuilder("java_test_log_messages")
            .setDescription("Counts the number of log messages sent")
            .setUnit("messages")
            .build();

        // Create attributes to tag the metric with log level
        Attributes attributes = Attributes.builder()
            .put("log_level", "info")
            .build();

        // Obtain a Tracer instance
        Tracer tracer = GlobalOpenTelemetry.getTracer("com.example.App");

        // Create a span to start the trace
        Span mainSpan = tracer.spanBuilder("main-application-trace")
            .startSpan();

        try {
            // Set the parent context to the current one
            Context parentContext = mainSpan.storeInContext(Context.current());

            while (true) {
                // Start a new span for each iteration (e.g., each log message sent)
                Span iterationSpan = tracer.spanBuilder("iteration-span")
                    .setParent(parentContext)  // Set parent context for the new span
                    .startSpan();

                try {
                    // Log a test message
                    logger.info("This is a test log message!");

                    // Increment the counter with attributes
                    logCounter.add(1, attributes);

                    // Log the counter increment
                    logger.info("Incremented log_messages_sent counter!");

                    // Add an event to the span (optional, can be used for tracing detailed events)
                    iterationSpan.addEvent("Log message sent and counter incremented");

                } catch (Exception e) {
                    // If an error occurs in the iteration, set the span status to error
                    iterationSpan.setStatus(StatusCode.ERROR, "Error occurred in iteration");
                    logger.error("Error in iteration", e);
                } finally {
                    // End the iteration span after each loop
                    iterationSpan.end();
                }

                // Wait for 60 seconds (60000 milliseconds)
                try {
                    Thread.sleep(60000); // 60 seconds
                } catch (InterruptedException e) {
                    // Log the interruption error
                    logger.error("Thread was interrupted", e);
                    // Re-interrupt the thread in case we need to handle it elsewhere
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            // Set the main span status to error in case of failure
            mainSpan.setStatus(StatusCode.ERROR, "Error occurred in main application");
            logger.error("Application error", e);
        } finally {
            // End the main span after the loop ends
            mainSpan.end();
        }
    }
}
