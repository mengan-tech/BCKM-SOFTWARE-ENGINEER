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

## Security session (task-2)

The registration endpoint rejects invalid passwords with field-level problem details and persists successful accounts with delegating bcrypt hashes:

```text
$ curl -i -X POST localhost:8080/api/register -H 'Content-Type: application/json' -d '{"username":"bob","password":"short"}'
HTTP/1.1 400
Content-Type: application/problem+json

{"type":"about:blank","title":"Bad Request","status":400,"detail":"Validation failed","instance":"/api/register","errors":[{"field":"password","message":"size must be between 12 and 72"},{"field":"password","message":"must contain an upper-case letter, a lower-case letter and a digit"}]}

$ curl -i -X POST localhost:8080/api/register -H 'Content-Type: application/json' -d '{"username":"bob","password":"Correct-Horse-9"}'
HTTP/1.1 201
{"username":"bob"}

$ curl -i -u bob:Correct-Horse-9 localhost:8080/api/me
HTTP/1.1 200
{"name":"bob","roles":["ROLE_CUSTOMER"]}
```

Queried from the H2 `users` table during the registration integration test:

```text
ID  USERNAME  PASSWORD_HASH                                                          ENABLED
3   bob       {bcrypt}$2a$10$H82.oNk8nY02ldIVAee1N.Fr/C1.SaAujHetfx/3MjDbTaguSL/r2 TRUE
```
