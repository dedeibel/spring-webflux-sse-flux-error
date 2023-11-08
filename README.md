# spring-webflux-sse-flux-error

Run

```
mvn compile exec:java
```

Call endpoint

```
curl -v --no-buffer --header Accept:text/event-stream http://localhost:8080/api/fail/hello-flux-later

*   Trying ::1:8080...
* Connected to localhost (::1) port 8080 (#0)
> GET /api/fail/hello-flux-later HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.74.0
> Accept:text/event-stream
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< transfer-encoding: chunked
< Content-Type: text/event-stream;charset=UTF-8
< 
data:{"content":"hello 2023-11-08T08:41:27.365653396Z"}

data:{"content":"hello 2023-11-08T08:41:28.384604909Z"}

data:{"content":"hello 2023-11-08T08:41:29.389031171Z"}

* transfer closed with outstanding read data remaining
* Closing connection 0
curl: (18) transfer closed with outstanding read data remaining
```

Try all endpoints:

```
bash request.sh
```
