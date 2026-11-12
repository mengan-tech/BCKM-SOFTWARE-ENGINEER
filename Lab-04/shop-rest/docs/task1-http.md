# Task 1 — HTTP Endpoints

## 1. Retrieve All Products

**Endpoint:** `GET /api/products`
**Result:** `200 OK`

### Request

```http
GET http://localhost:8080/api/products HTTP/1.1
```

### Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 220

[
  {"id":1,"name":"Kettle","price":24.90,"sku":"KT-01","stock":12,"version":1},
  {"id":2,"name":"Mug","price":6.50,"sku":"MG-01","stock":40,"version":1},
  {"id":3,"name":"Tea","price":4.20,"sku":"TE-01","stock":0,"version":1}
]
```

## 2. Retrieve a Specific Product by ID

**Endpoint:** `GET /api/products/1`
**Result:** `200 OK`

### Request

```http
GET http://localhost:8080/api/products/1 HTTP/1.1
```

### Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 75

{"id":1,"name":"Kettle","price":24.90,"sku":"KT-01","stock":12,"version":1}
```

## 3. Product Not Found (Explicit)

**Endpoint:** `GET /api/products/99`
**Result:** `404 Not Found`

### Request

```http
GET http://localhost:8080/api/products/99 HTTP/1.1
```

### Response

```http
HTTP/1.1 404 Not Found
Content-Length: 0
```

## 4. Invalid Product ID Format (Framework-Level)

**Endpoint:** `GET /api/products/abc`
**Result:** `404 Not Found`

### Request

```http
GET http://localhost:8080/api/products/abc HTTP/1.1
```

### Response

```http
HTTP/1.1 404 Not Found
Content-Length: 0
```

## Notes on 404 Behavior

* **Explicit 404 (`/api/products/99`):** Returns an explicit `404 Not Found` thrown by `NotFoundException` in `ProductResource.findById()` because the ID format is valid, but no product with ID 99 actually exists in the database.

* **Framework-Level 404 (`/api/products/abc`):** Returns a framework-level `404 Not Found` because the path template `{id: \d+}` is constrained to only match digits. As a result, no resource method matches the incoming request string "abc".