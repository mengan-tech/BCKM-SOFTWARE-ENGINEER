# Task 5: Paging, Caching, Versioning and Sub-resources

## 1. Page 1 of size 10 (X-Total-Count + Link headers)
- Show Pages and products

```bash
curl -s -D - -o NUL 'localhost:8080/api/products?page=1&size=10'
```
Output:

HTTP/1.1 200 OK
Link: <http://localhost:8080/api/products?page=0&size=10>; rel="first"
Link: <http://localhost:8080/api/products?page=0&size=10>; rel="prev"
Link: <http://localhost:8080/api/products?page=2&size=10>; rel="next"
Link: <http://localhost:8080/api/products?page=3&size=10>; rel="last"
X-Total-Count: 33
Content-Type: application/json

## 2. GET with ETag and Cache-Control (200)

- The product doesn't change show by ETag: 1

```bash
curl -i localhost:8080/api/products/1
```
Output:

HTTP/1.1 200 OK
ETag: "1"
Cache-Control: private, no-transform, max-age=60
Content-Type: application/json

{"id":1,"name":"Kettle","price":24.90,"sku":"KT-01","stock":12}


- If the products change use this. it shows ETag: 2

```bash
curl -i localhost:8080/api/products/1 -H 'If-None-Match: "1"'
```


## 3. GET with a matching If-None-Match (304)

- When two or three people edit the product at the same time. So the solution use header: If-None-Match for show version l.
- when we start the server we get the product all in version 1. (not 0)

```bash
curl -i -X PUT localhost:8080/api/products/1 -H 'X-Api-Key: demo-123' -H 'Content-Type: application/json' -d '{"name":"Kettle XL","sku":"KT-01","price":29.90,"stock":12}'
```

```bash
curl -i -X PUT localhost:8080/api/products/1 -H 'X-Api-Key: demo-123' -H 'If-Match: "0"' -H 'Content-Type: application/json' -d '{"name":"Kettle XL","sku":"KT-01","price":29.90,"stock":12}'
```

```bash
curl -i -X PUT localhost:8080/api/products/1 -H 'X-Api-Key: demo-123' -H 'If-Match: "1"' -H 'Content-Type: application/json' -d '{"name":"Kettle XL","sku":"KT-01","price":29.90,"stock":12}'
```

## 4. PUT without If-Match (428 problem+json)
- If i change the JSON format, The v1 and v2 still works as normal.
```bash
curl -s localhost:8080/api/products/1
```

```bash
curl -s localhost:8080/api/products/1 -H 'Accept: application/vnd.shop.v2+json'
```

## 5. POST a review (201 + Location)
```bash
$ curl -i -X POST localhost:8080/api/products/1/reviews -H 'X-Api-Key: demo-123' -H 'Content-Type: application/json' -d '{"stars":5,"author":"Ana","text":"Boils fast."}'
HTTP/1.1 201 Created
Location: http://localhost:8080/api/products/1/reviews/1
Content-Type: application/json

{"author":"Ana","id":1,"productId":1,"stars":5,"text":"Boils fast."}
```

## 6. GET the reviews of a product (200)
```bash
$ curl -i localhost:8080/api/products/1/reviews
HTTP/1.1 200 OK
Content-Type: application/json

[{"author":"Ana","id":1,"productId":1,"stars":5,"text":"Boils fast."}]
```

## 7. Reviews of an unknown product (404 problem+json)
```bash
$ curl -i localhost:8080/api/products/999/reviews
HTTP/1.1 404 Not Found
Content-Type: application/problem+json

{"detail":"Product 999 not found","instance":"/api/products/999/reviews","status":404,"title":"Not Found","type":"about:blank"}
```
