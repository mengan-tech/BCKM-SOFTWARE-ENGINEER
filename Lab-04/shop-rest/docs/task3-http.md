# Task 3 — Typed Filters, Validation, and Problem Details

Base URI: `http://localhost:8080/api/`

## Filtered and sorted products

```sh
curl -s 'localhost:8080/api/products?minPrice=5%20EUR&inStock=true&sort=price,desc'
```

Example response:

```json
[
  {"id":1,"name":"Kettle","price":24.90,"sku":"KT-01","stock":12},
  {"id":2,"name":"Mug","price":6.50,"sku":"MG-01","stock":40}
]
```

The name query is case-insensitive, price bounds accept `Money` values in EUR,
`inStock=true` excludes zero-stock products, and sorting supports `name`, `price`,
or `stock` in ascending or descending order.

## Invalid Money query parameter

```sh
curl -i 'localhost:8080/api/products?minPrice=abc'
```

```http
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json

{"detail":"Cannot parse Money from 'abc'","instance":"/api/products","status":400,"title":"Bad Request","type":"about:blank"}
```

## Unknown sort field

```sh
curl -i 'localhost:8080/api/products?sort=colour,asc'
```

This returns `400 Bad Request` with `Content-Type: application/problem+json` and
detail `Unknown sort field: colour`.

## Invalid product body

The following body has three invalid fields (`name`, `sku`, and `price`); stock
zero is valid:

```sh
curl -i -X POST localhost:8080/api/products -H 'Content-Type: application/json' \
  -d '{"name":"","sku":"kettle","price":0,"stock":0}'
```

```http
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json

{"detail":"Validation failed","errors":[{"field":"name","message":"must not be blank"},{"field":"price","message":"must be greater than or equal to 0.01"},{"field":"sku","message":"must match \"[A-Z]{2}-\\d{2}\""}],"instance":"/api/products","status":400,"title":"Invalid request","type":"about:blank"}
```

## Not found

```sh
curl -i localhost:8080/api/products/99
```

```http
HTTP/1.1 404 Not Found
Content-Type: application/problem+json

{"detail":"Product 99 not found","instance":"/api/products/99","status":404,"title":"Not Found","type":"about:blank"}
```

JAX-RS exceptions and the existing 409 conflicts now use the same problem body
format while retaining their HTTP status.

## Internal server error

For the 500 check, temporarily throw `new IllegalStateException("test failure")`
from a resource method, request that method, and then remove the temporary throw.
The response is `500 Internal Server Error`, `application/problem+json`, with
detail `reference <UUID>`; it does not include the exception message or a stack
trace. The server log records the exception alongside that reference id.
