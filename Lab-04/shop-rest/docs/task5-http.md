# Task 5: Paging, Caching, Versioning and Sub-resources

## 1. Page 1 of size 10 (X-Total-Count + Link headers)
```bash
$ curl -s -D - -o NUL 'localhost:8080/api/products?page=1&size=10'
HTTP/1.1 200 OK
Link: <http://localhost:8080/api/products?page=0&size=10>; rel="first"
Link: <http://localhost:8080/api/products?page=0&size=10>; rel="prev"
Link: <http://localhost:8080/api/products?page=2&size=10>; rel="next"
Link: <http://localhost:8080/api/products?page=3&size=10>; rel="last"
X-Total-Count: 33
Content-Type: application/json
```

## 2. GET with ETag and Cache-Control (200)
```bash
$ curl -i localhost:8080/api/products/1
HTTP/1.1 200 OK
ETag: "1"
Cache-Control: private, no-transform, max-age=60
Content-Type: application/json

{"id":1,"name":"Kettle","price":24.90,"sku":"KT-01","stock":12}
```

## 3. GET with a matching If-None-Match (304)
```bash
$ curl -i localhost:8080/api/products/1 -H 'If-None-Match: "1"'
HTTP/1.1 304 Not Modified
ETag: "1"
```

## 4. PUT without If-Match (428 problem+json)
```bash
$ curl -i -X PUT localhost:8080/api/products/1 -H 'X-Api-Key: demo-123' -H 'Content-Type: application/json' -d '{"name":"Kettle XL","sku":"KT-01","price":29.90,"stock":12}'
HTTP/1.1 428 Precondition Required
Content-Type: application/problem+json

{"detail":"If-Match header is required to update a product","instance":"/api/products/1","status":428,"title":"Precondition Required","type":"about:blank"}
```

## 5. PUT with a stale tag (412 problem+json)
```bash
$ curl -i -X PUT localhost:8080/api/products/1 -H 'X-Api-Key: demo-123' -H 'If-Match: "0"' -H 'Content-Type: application/json' -d '{"name":"Kettle XL","sku":"KT-01","price":29.90,"stock":12}'
HTTP/1.1 412 Precondition Failed
Content-Type: application/problem+json

{"detail":"If-Match \"0\" does not match the current ETag \"1\"","instance":"/api/products/1","status":412,"title":"Precondition Failed","type":"about:blank"}
```

## 6. PUT with the right tag (200 + new ETag)
```bash
$ curl -i -X PUT localhost:8080/api/products/1 -H 'X-Api-Key: demo-123' -H 'If-Match: "1"' -H 'Content-Type: application/json' -d '{"name":"Kettle XL","sku":"KT-01","price":29.90,"stock":12}'
HTTP/1.1 200 OK
ETag: "2"
Content-Type: application/json

{"id":1,"name":"Kettle XL","price":29.90,"sku":"KT-01","stock":12}
```

## 7. v1 and v2 of the same product
```bash
$ curl -s localhost:8080/api/products/1
{"id":1,"name":"Kettle XL","price":29.90,"sku":"KT-01","stock":12}

$ curl -s localhost:8080/api/products/1 -H 'Accept: application/vnd.shop.v2+json'
{"available":true,"id":1,"name":"Kettle XL","price":{"amount":29.90,"currency":"EUR"},"sku":"KT-01","stock":12}
```

## 8. POST a review (201 + Location)
```bash
$ curl -i -X POST localhost:8080/api/products/1/reviews -H 'X-Api-Key: demo-123' -H 'Content-Type: application/json' -d '{"stars":5,"author":"Ana","text":"Boils fast."}'
HTTP/1.1 201 Created
Location: http://localhost:8080/api/products/1/reviews/1
Content-Type: application/json

{"author":"Ana","id":1,"productId":1,"stars":5,"text":"Boils fast."}
```

## 9. GET the reviews of a product (200)
```bash
$ curl -i localhost:8080/api/products/1/reviews
HTTP/1.1 200 OK
Content-Type: application/json

[{"author":"Ana","id":1,"productId":1,"stars":5,"text":"Boils fast."}]
```

## 10. Reviews of an unknown product (404 problem+json)
```bash
$ curl -i localhost:8080/api/products/999/reviews
HTTP/1.1 404 Not Found
Content-Type: application/problem+json

{"detail":"Product 999 not found","instance":"/api/products/999/reviews","status":404,"title":"Not Found","type":"about:blank"}
```

## 11. Page size too large (400 validation)
```bash
$ curl -i 'localhost:8080/api/products?size=500'
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json

{"detail":"Validation failed","errors":[{"field":"size","message":"must be less than or equal to 100"}],"instance":"/api/products","status":400,"title":"Invalid request","type":"about:blank"}
```

## 12. Invalid review (400 validation)
```bash
$ curl -i -X POST localhost:8080/api/products/1/reviews -H 'X-Api-Key: demo-123' -H 'Content-Type: application/json' -d '{"stars":9,"author":"Bo","text":"Too good."}'
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json

{"detail":"Validation failed","errors":[{"field":"stars","message":"must be less than or equal to 5"}],"instance":"/api/products/1/reviews","status":400,"title":"Invalid request","type":"about:blank"}
```