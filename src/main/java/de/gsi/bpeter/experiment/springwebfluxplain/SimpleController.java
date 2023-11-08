package de.gsi.bpeter.experiment.springwebfluxplain;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class SimpleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringWebfluxPlainService.class);

    @GetMapping(path = "hello-mono", produces = TEXT_EVENT_STREAM_VALUE)
    Mono<SimpleResponse> helloMono() {
        return Mono.defer(() -> Mono.just(createResponseValue()));
    }

    @GetMapping(path = "hello-flux", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<SimpleResponse> helloFlux() {
        return Flux.create(sink -> {

            final Thread creator = new Thread(() -> {
                LOGGER.info("Starting hello flux thread");

                for (int i = 0; i < 5; ++i) {
                    LOGGER.info("Sending {}", i);
                    sink.next(createResponseValue());

                    try {
                        Thread.sleep(1000);
                    } catch (final InterruptedException e) {
                        LOGGER.info("Interrupted at {}", i);
                        return;
                    }
                }

                LOGGER.info("Sending done, finishing sink and thread");
                sink.complete();
            });
            creator.setDaemon(true);
            creator.start();

            sink.onDispose(() -> {
                LOGGER.info("Flux disposed, stopping thread");
                creator.interrupt();
            });
        });
    }

    private static SimpleResponse createResponseValue() {
        final SimpleResponse response = new SimpleResponse("hello " + Instant.now());
        LOGGER.debug("Creating response {}", response);
        return response;
    }

}
