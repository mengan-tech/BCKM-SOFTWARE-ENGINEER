# API Session Transcript

## 1. Fetch Orders (Paginated & Sorted)

**Endpoint:** `GET /api/orders?status=PAID&page=0&size=2&sort=placedAt,desc`

```bash

{
  "content": [
    { "id": 151, "status": "PAID", "placedAt": "2026-09-24T09:12:44.120Z", "customer": { "name": "Ada Lovelace" } },
    { "id": 108, "status": "PAID", "placedAt": "2026-09-23T17:03:10.551Z", "customer": { "name": "Grace Hopper" } }
  ],
  "page": { "size": 2, "number": 0, "totalElements": 11, "totalPages": 6 }
}
```

**SQL Log:**
*(Note: Interface projection selects only the listed columns)*
```sql
DEBUG org.hibernate.SQL : select o1_0.id,o1_0.status,o1_0.placed_at,c1_0.name from orders o1_0
                          join customer c1_0 on c1_0.id=o1_0.customer_id where o1_0.status=?
                          order by o1_0.placed_at desc offset ? rows fetch first ? rows only
DEBUG org.hibernate.SQL : select count(o1_0.id) from orders o1_0 where o1_0.status=?
```

---

## 2. Search Products

**Endpoint:** `GET /api/products?q=key&category=HARDWARE&maxPrice=60&inStock=true`

```bash
$ http GET "localhost:8080/api/products?q=key&category=HARDWARE&maxPrice=60&inStock=true" | jq '.content[].sku'
"KB-1"
"KB-7"
```

---

## 3. Fetch Customer Orders

**Endpoint:** `GET /api/customers/{email}/orders`

```bash

{
  "id": 151,
  "status": "PAID",
  "placedAt": "2026-09-24T09:12:44.120Z",
  "customer": { "id": 1, "name": "Ada Lovelace", "email": "ada@example.com" },
  "lines": [
    { "id": 301, "quantity": 2, "product": { "id": 1, "sku": "KB-1", "name": "Mechanical Keyboard" }, "unitPrice": { "amount": 29.99, "currency": "EUR" } }
  ]
}
```

---

## 4. Test Execution

**Suite:** `OrderRepositoryTest`

```bash
$ ./mvnw -q test -Dtest=OrderRepositoryTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
```