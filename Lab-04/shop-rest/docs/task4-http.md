# Task 4: Providers and Filters Validation

### 1. POST without a key (401 problem+json)
```bash
curl -i -X POST localhost:8080/api/products -H 'Content-Type: application/json' -d '{"sku":"TEST-1","name":"Test","price":"10.00 USD","stock":5}'

HTTP/1.1 401 Unauthorized
WWW-Authenticate: ApiKey realm="shop"
Content-Type: application/problem+json

{"detail":"Missing or invalid X-Api-Key","instance":"/api/products","status":401,"title":"Unauthorized","type":"about:blank"}
```

### 2. POST with wrong key (401)
```bash
$ curl -i -X POST localhost:8080/api/products -H 'X-Api-Key: demo-123' -H 'Content-Type: application/json' -d '{"sku":"TEST-1","name":"Test","price":"10.00 USD","stock":5}'
HTTP/1.1 401 Unauthorized
```

### 3. POST with right key (201)
```bash
$ curl -i -X POST localhost:8080/api/products -H 'X-Api-Key: demo-123' -H 'Content-Type: application/json' -d '{"sku":"TEST-1","name":"Test","price":"10.00 USD","stock":5}'
HTTP/1.1 201 Created
```

### 4. OPTIONS Preflight (CORS)
```bash
$ curl -i -X OPTIONS localhost:8080/api/products -H 'Origin: http://localhost:5173' -H 'Access-Control-Request-Method: POST'
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE
```

### 5. GET showing X-Correlation-Id echoed back
```bash
$ curl -si localhost:8080/api/products/1 -H 'X-Correlation-Id: order-check-42'

X-Correlation-Id: order-check-42
```

### 6. GET with Compression (Accept-Encoding: gzip)
```bash
$ curl -sv --compressed localhost:8080/api/products -o /dev/null 2>&1 | grep -i -E 'encoding|vary'
> Accept-Encoding: deflate, gzip, br, zstd
< Content-Encoding: gzip
< Vary: Accept-Encoding
```

### 7. GET without Compression
```bash
$ curl -sv localhost:8080/api/products -o /dev/null 2>&1 | grep -i -E 'encoding|vary'
```