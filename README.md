akka-tls-hangs-example
======================

A very simple project running two HTTPS servers (one for HTTP/1.1 and one for HTTP/2) for demonstration what happens if such server receives an unencrypted connection.

In ideal scenario such connection should either be dropped or, preferably, rejected with error page like "Dude, this is a TLS port". Just dropping such connection silently is less preferable since it can cause confusion.

However, Akka HTTP implements third scenario - such connection just hangs. It keeps waiting and waiting and wa..., until either server dies, client gives up with timeout or universe collapses.  This is not acceptable at all, because it **is** very confusing. Trying HTTPS instead of HTTP is the last thing that I will try when I encounter such behavior on some external server.

```
$ curl https://localhost:8080 -kv
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
* ALPN, offering h2
* ALPN, offering http/1.1
* successfully set certificate verify locations:
*   CAfile: /etc/pki/tls/certs/ca-bundle.crt
  CApath: none
* TLSv1.3 (OUT), TLS handshake, Client hello (1):
* TLSv1.3 (IN), TLS handshake, Server hello (2):
* TLSv1.2 (IN), TLS handshake, Certificate (11):
* TLSv1.2 (IN), TLS handshake, Server key exchange (12):
* TLSv1.2 (IN), TLS handshake, Server finished (14):
* TLSv1.2 (OUT), TLS handshake, Client key exchange (16):
* TLSv1.2 (OUT), TLS change cipher, Change cipher spec (1):
* TLSv1.2 (OUT), TLS handshake, Finished (20):
* TLSv1.2 (IN), TLS handshake, Finished (20):
* SSL connection using TLSv1.2 / ECDHE-RSA-AES256-GCM-SHA384
* ALPN, server did not agree to a protocol
* Server certificate:
*  subject: CN=localhost
*  start date: Jul 17 15:35:23 2019 GMT
*  expire date: Jul 16 15:35:23 2020 GMT
*  issuer: CN=localhost
*  SSL certificate verify result: self signed certificate (18), continuing anyway.
> GET / HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.64.0
> Accept: */*
> 
< HTTP/1.1 200 OK
< Server: akka-http/10.1.8
< Date: Wed, 17 Jul 2019 16:47:07 GMT
< Content-Type: text/plain; charset=UTF-8
< Content-Length: 36
< 
I'm alive! Timestamp: 1563382027827
* Connection #0 to host localhost left intact
```
But...
```
$ curl http://localhost:8080 -v -m 5
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET / HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.64.0
> Accept: */*
> 
* Operation timed out after 5000 milliseconds with 0 bytes received
* Closing connection 0
curl: (28) Operation timed out after 5000 milliseconds with 0 bytes received
```

Versions used in this project *(looks like pretty recent)*:

* Akka: **2.5.23**
* Akka HTTP: **10.1.8**