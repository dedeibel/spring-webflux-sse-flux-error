#!/bin/bash
set -x

true "--------------- Regular mono request and response for reference: ---------------------"

curl -v --no-buffer --header "Accept:text/event-stream" http://localhost:8080/api/hello-mono

true "--------------- Regular flux request and response for reference: ---------------------"

curl -v --no-buffer --header "Accept:text/event-stream" http://localhost:8080/api/hello-flux

true "--------------- Mono with exception during request, message show: ---------------------"

curl -v --no-buffer --header "Accept:text/event-stream" http://localhost:8080/api/fail/hello-mono

true "--------------- Flux with exception during request, message shown: ---------------------"

curl -v --no-buffer --header "Accept:text/event-stream" http://localhost:8080/api/fail/hello-flux

true "--------------- Flux with exception after initial request but during flux lifetime, no message shown: ---------------------"

curl -v --no-buffer --header "Accept:text/event-stream" http://localhost:8080/api/fail/hello-flux-later

true "--------------- Flux of a sink with exception after initial request but during flux lifetime, no message shown: ---------------------"

SINK_ID=$RANDOM
curl -v --no-buffer --header "Accept:text/event-stream" http://localhost:8080/api/fail/hello-flux-sink\?id\=$SINK_ID

true "--------------- Flux subscribe request of a the same sink with exception, message shown: ---------------------"

curl -v --no-buffer --header "Accept:text/event-stream" http://localhost:8080/api/fail/hello-flux-sink\?id\=$SINK_ID

true "done"
