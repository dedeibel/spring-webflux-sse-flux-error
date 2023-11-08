package de.gsi.bpeter.experiment.springwebfluxplain;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record SimpleResponse(String content) {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleResponse.class);

    static SimpleResponse createResponseValue() {
        final SimpleResponse response = new SimpleResponse("hello " + Instant.now());
        LOGGER.debug("Creating response {}", response);
        return response;
    }

    static RuntimeException createException() {
        final RuntimeException ex = new RuntimeException("Wifi cable not found " + Instant.now());
        LOGGER.debug("Creating exception {}", ex.toString());
        return ex;
    }

}
