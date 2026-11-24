# Security session (task-1)

Expected HTTP exchanges with the temporary users from `SecurityConfig`:

```text
$ curl -i localhost:8080/api/products
HTTP/1.1 200
Content-Type: application/json

[{"id":1,"name":"USB-C cable","price":7.90,"stock":25},{"id":2,"name":"Wireless mouse","price":19.90,"stock":12},{"id":3,"name":"Keyboard","price":49.50,"stock":8}]

$ curl -i -X POST localhost:8080/api/products -H "Content-Type: application/json" -d "{\"name\":\"Mouse\",\"price\":19.9,\"stock\":5}"
HTTP/1.1 401
WWW-Authenticate: Basic realm="Realm"
Content-Type: application/problem+json

{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Authentication required","instance":"/api/products"}

$ curl -i -u alice:alice -X DELETE localhost:8080/api/products/1
HTTP/1.1 403
Content-Type: application/problem+json

{"type":"about:blank","title":"Forbidden","status":403,"detail":"Access denied","instance":"/api/products/1"}

$ curl -i -u admin:admin -X DELETE localhost:8080/api/products/1
HTTP/1.1 204
```

These are expected responses; replay the commands from a running server to capture the full live headers and body.
