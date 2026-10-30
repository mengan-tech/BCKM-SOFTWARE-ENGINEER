Record session of http
---

**Base URL:** `http://localhost:8081/api/products`

---

## 1. GET Methods

### Get All Products
Retrieve a list of all products in the database.
*   **Endpoint:** `/api/products`
*   **Response Format:** JSON array of product objects.

### Get Product by ID
Retrieve a specific product by its ID.
*   **Endpoint:** `/api/products/{id}`
*   **Example:** `/api/products/1`

**Success Response (200 OK):**
```json
{
  "id": 1,
  "name": "Laptop",
  "price": 1000.00,
  "stock": 20
}
```

**Error Responses:**
*   **404 Not Found:** (If the ID does not exist)
    ```json
    {
      "detail": "Product 6 not found",
      "instance": "/api/products/6",
      "status": 404,
      "title": "Not Found"
    }
    ```
*   **500 Internal Server Error:** (e.g., malformed endpoint)
    ```json
    {
      "detail": "An unexpected error occurred",
      "instance": "/api/proucts/1",
      "status": 500,
      "title": "Internal Server Error"
    }
    ```

---

## 2. POST Method

### Create a New Product
Creates a new product in the database. The ID is auto-generated.
*   **Endpoint:** `/api/products`

**Request Body:**
```json
{
  "name": "Lcom",
  "price": 100.00,
  "stock": 20
}
```

**Error Responses:**
*   **409 Conflict:** (If a product with the same name already exists)
    ```json
    {
      "detail": "Duplicate key: PreparedStatementCallback; SQL [INSERT INTO product(name, price, stock) VALUES (?, ?, ?)]; Unique index or primary key violation: \"PUBLIC.CONSTRAINT_18 INDEX PUBLIC.CONSTRAINT_INDEX_1 ON PUBLIC.PRODUCT(NAME NULLS FIRST) VALUES ( /* 8 */ 'Lco' )\"; SQL statement:\nINSERT INTO product(name, price, stock) VALUES (?, ?, ?) [23505-240]",
      "instance": "/api/products",
      "status": 409,
      "title": "Conflict"
    }
    ```

---

## 3. PATCH Method

### Update Product Stock
Increase or decrease the stock of a specific product by a `delta` value.
*   **Endpoint:** `/api/products/{id}/stock`

**Request Body:**
```json
{
  "delta": -1
}
```

**Error Responses:**
*   **409 Conflict:** (If the `delta` decreases the stock below 0)
    ```json
    {
      "detail": "Product 2 has 49 units, cannot remove 100",
      "instance": "/api/products/2/stock",
      "status": 409,
      "title": "Conflict"
    }
    ```

---

## 4. DELETE Method

### Delete a Product
Delete a specific product from the database using its ID.
*   **Endpoint:** `/api/products/{id}`

**Success Response:**
*   **204 No Content** (Successful deletion, no body returned)

**Error Responses:**
*   **404 Not Found:** (If the product ID does not exist)
    ```json
    {
      "detail": "Product 2 not found",
      "instance": "/api/products/2",
      "status": 404,
      "title": "Not Found"
    }
    ```
