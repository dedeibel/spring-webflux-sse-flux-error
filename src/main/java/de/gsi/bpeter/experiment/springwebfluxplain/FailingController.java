package de.gsi.bpeter.experiment.springwebfluxplain;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.publisher.Sinks.Many;

@RestController
@RequestMapping("/api/fail")
public class FailingController {

    static final Logger LOGGER = LoggerFactory.getLogger(SpringWebfluxPlainService.class);

    @GetMapping(path = "hello-mono", produces = TEXT_EVENT_STREAM_VALUE)
    Mono<SimpleResponse> helloMonoFail() {
        return Mono.defer(() -> {
            if (Boolean.TRUE) {
                throw SimpleResponse.createException();
            }

            return Mono.just(SimpleResponse.createResponseValue());
        });
    }

    @GetMapping(path = "hello-flux", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<SimpleResponse> helloFlux() {
        if (Boolean.TRUE) {
            throw SimpleResponse.createException();
        }

        return Flux.just(SimpleResponse.createResponseValue());
    }

    @GetMapping(path = "hello-flux-later", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<SimpleResponse> helloFluxLater() {
        return Flux.create(sink -> {

            final Thread creator = new Thread(() -> {
                LOGGER.info("Starting hello flux thread, failing after 3");

                for (int i = 0; i < 5; ++i) {
                    if (i == 3) {

                        // FIXME: not received by the subscriber
                        LOGGER.info("Sending {} error", i);
                        sink.error(SimpleResponse.createException());
                    } else {

                        LOGGER.info("Sending {}", i);
                        sink.next(SimpleResponse.createResponseValue());
                    }

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

    /*
     * Case when using a sink in between. Active subscription cause java.lang.UnsupportedOperationException: null
     * following new subscriptions will receive proper exception message.
     */

    private final Map<Integer, Many<SimpleResponse>> persistentSinkMap = new ConcurrentHashMap<>();
    private final Map<Integer, Boolean> persistentSinkErrorScheduledMap = new ConcurrentHashMap<>();

    @GetMapping(path = "hello-flux-sink", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<SimpleResponse> helloFluxSink(@RequestParam final int id) {
        final Many<SimpleResponse> persistentSink =
                persistentSinkMap.computeIfAbsent(id, createId -> Sinks.many().multicast().directBestEffort());

        if (persistentSinkErrorScheduledMap.replace(id, true) == null) {
            final Thread creator = new Thread(() -> {
                LOGGER.info("Starting hello flux sink, error thread, error after 3");

                for (int i = 0; i < 5; ++i) {
                    if (i == 3) { // FIXME: actually only two data entries are received by the client
                        LOGGER.info("Sending {} error", i);

                        // FIXME: not received by the "live" subscriber. New subscriptions fail as expected.
                        persistentSink.emitError(SimpleResponse.createException(), this::onEmitFailure);
                    } else {

                        LOGGER.info("Sending {}", i);
                        persistentSink.emitNext(SimpleResponse.createResponseValue(), this::onEmitFailure);
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (final InterruptedException e) {
                        LOGGER.info("Interrupted at {}", i);
                        return;
                    }
                }

                LOGGER.info("Sending done, finishing sink and thread");
                persistentSink.emitComplete(this::onEmitFailure);
            });
            creator.setDaemon(true);
            creator.start();
        }

        return persistentSink.asFlux();
    }

    private boolean onEmitFailure(final SignalType signaltype, final EmitResult emitresult) {
        LOGGER.warn("emit result: {} {}", signaltype, emitresult);
        return false;
    }

}
