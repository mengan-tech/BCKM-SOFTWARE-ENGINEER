# Lab-04 - shop-rest (Jakarta REST 4.0 / Jersey 4)

## Build and run
```
mvn -q compile                 # first run downloads all dependencies
mvn -q compile exec:java       # starts the server, Ctrl+C to stop
mvn test                       # runs the JerseyTest classes
```

Base URI: `http://localhost:8080/api/`

# How to test Task2:
run mvn -q compile exec:java and open new window terminal or bash
1. Create a Product (Should return 201 Created + Location Header)
curl -i -X POST localhost:8080/api/products -H 'Content-Type: application/json' -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'

2. Prevent Duplicate SKU (Should return 409 Conflict)
curl -i -X POST localhost:8080/api/products -H 'Content-Type: application/json' -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'

3. XML Round Trip (POST XML)
curl -i -X POST localhost:8080/api/products -H 'Content-Type: application/xml' -d '<product><name>Plate</name><sku>PL-01</sku><price>12.00</price><stock>20</stock></product>'

4. XML Round Trip (GET XML - Should return the Plate in XML format)
curl -s localhost:8080/api/products/4 -H 'Accept: application/xml'

5. PATCH Stock Success (Should return 200 OK)
curl -i -X PATCH localhost:8080/api/products/3/stock -H 'Content-Type: application/json' -d '{"delta":5}'

6. PATCH Stock Failure (Should return 409 Conflict because stock goes negative)
curl -i -X PATCH localhost:8080/api/products/3/stock -H 'Content-Type: application/json' -d '{"delta":-10}'

7. DELETE Success (Should return 204 No Content)
curl -i -X DELETE localhost:8080/api/products/4

8. DELETE Missing Product (Should return 404 Not Found)
curl -i -X DELETE localhost:8080/api/products/4

9. Unsupported Media Type (Should return 415)
curl -i -X POST localhost:8080/api/products -H 'Content-Type: text/plain' -d 'Teapot'

---

## First resource and server
- Explaining why the resource class has no mutable fields even though it is stateless per request.
  > The resource class holds only the immutable `final` reference to `ProductRepository`; all per-request data (such as the product id looked up) lives in method-local parameters, so no mutable instance field can cause cross-request interference.

## shows all eight calls.
1. HTTP/1.1 201 Created
Location: http://localhost:8080/api/products/4
Content-Type: application/json
Content-Length: 62

{"id":4,"name":"Teapot","price":31.00,"sku":"TP-01","stock":5}

2. HTTP/1.1 409 Conflict
Content-Length: 0

3. HTTP/1.1 201 Created
Location: http://localhost:8080/api/products/5
Content-Type: application/json
Content-Length: 62

4. <?xml version="1.0" encoding="UTF-8" standalone="yes"?><product id="4"><name>Teapot</name><sku>TP-01</sku><price>31.00</price><stock>5</stock></product>

5. HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 58
{"id":3,"name":"Tea","price":4.20,"sku":"TE-01","stock":5}

6. HTTP/1.1 409 Conflict
Content-Length: 0

7. HTTP/1.1 204 No Content

8. HTTP/1.1 404 Not Found
Content-Length: 0

9. HTTP/1.1 415 Unsupported Media Type
Content-Length: 0


