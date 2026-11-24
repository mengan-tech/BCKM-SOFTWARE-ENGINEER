# Shop Secure

## Catalogue API

The application seeds three products and JPA-backed accounts at startup. Set `shop.seed.admin-password` and `shop.seed.customer-password` to configure the seed passwords; development defaults are provided. `GET /api/products` and `GET /api/products/{id}` are public. Creating a product requires authentication, deleting one requires the `ADMIN` role, `POST /api/register` is public, and other `/api/**` routes require authentication.

## Spring Security with zero application configuration

With the explicit `SecurityConfig` and temporary `UserDetailsService` removed, Spring Boot's `SecurityAutoConfiguration` supplies the default web security chain. `UserDetailsServiceAutoConfiguration` also creates an in-memory user named `user` with a random password, logged at startup as:

```text
Using generated security password: <copy the generated value from the startup log>
```

The default chain requires authentication, so the catalogue is not public until the explicit chain is added. Reproduce this behavior by temporarily removing `SecurityConfig` (including `temporaryUsers`), starting the app with `--debug`, and copying the actual generated password from that run:

```text
$ curl -i localhost:8080/api/products
HTTP/1.1 401
WWW-Authenticate: Basic realm="Realm"

$ curl -i -u user:<generated-password> localhost:8080/api/products
HTTP/1.1 200
Content-Type: application/json

[{"id":1,"name":"USB-C cable","price":7.90,"stock":25},{"id":2,"name":"Wireless mouse","price":19.90,"stock":12},{"id":3,"name":"Keyboard","price":49.50,"stock":8}]
```

The generated password depends on each app startup and is intentionally not fabricated in this document. The checked-in implementation uses JPA-backed accounts with bcrypt password hashes.

## Explicit security behavior

The API uses stateless HTTP Basic, permits catalogue GET requests, restricts product deletion to ADMIN, and requires authentication for other API routes. Authentication and authorization errors share a `ProblemDetail` JSON shape with the relevant request URI. CSRF is disabled because this API accepts Basic/Bearer credentials only and does not rely on cookies.

Run the app with `.\mvnw.cmd spring-boot:run`, then use these requests:

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

The explicit-security transcript is also saved in [security-session.md](./security-session.md).
