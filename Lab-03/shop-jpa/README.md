# Lab-03 - Shop JPA

## How to run

### Check that hibernat is the right version (do it one time only)
- .\mvnw.cmd dependency:tree "-Dincludes=org.hibernate.orm"

### Run all test in Lab-03:
- ./mvnw test

### To run the application: (this is no test data)
- cd Lab-03\shop-jpa
- ./mvnw spring-boot:run

### Run the application with test data
- ./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"


---

## Task 1

### To run the test on task-1:
- ./mvnw clean test -Dtest=ProductMappingTest

SchemaManagementException: Schema-validation: missing column [sku] in table [product]

nested exception is org.hibernate.MappingException: The increment size of the [product_seq] sequence is set to [50] in the entity mapping but the mapped database sequence increment size is [1]

---

## Task 2

### To run the test on task-2:
- ./mvnw test -Dtest=OrderCascadeTest
- ./mvnw test -Dtest=EntityIdentiyTest

### Entity `toString()` pitfall

Temporarily including `lines` in `Order.toString()` and `order` in `OrderLine.toString()`, then printing a detached order outside a transaction, reproduced a `org.hibernate.LazyInitializationException`. Formatting the relationship fields tries to initialize the lazy order lines after the persistence context is closed. If the relationships are initialized, traversing both sides can instead recurse indefinitely and cause a `StackOverflowError`. Entity `toString()` methods therefore include scalar fields only.

## Task 3

### To run the test on task-3:
- ./mvnw test -Dtest=OrderRepositoryTest

### Spring Data derived query typo — boot error

If a derived query method contains a typo (e.g. `findByCustomerEmali…` instead of `findByCustomerEmail…`), Spring Data parses the method name at startup and fails with:

```
org.springframework.beans.factory.UnsatisfiedDependencyException:
Error creating bean with name 'orderController':
Unsatisfied dependency expressed through constructor parameter 0:
Error creating bean with name 'orderRepository':
No property 'emali' found for type 'Customer'; Did you mean 'email';
Traversed path: Order.customer
```

Every repository method is validated at boot, so the typo is caught immediately.

## Task 4

### To run the test on task-4:
- ./mvnw test -Dtest=OrderServiceTest
- ./mvnw test -Dtest=FlushOrderTest

Hibernate executes statements at flush in a fixed order:
- INSERT
- UPDATE
- Delete
- save

without flush in between (test expects the exception)
```
org.hibernate.SQL : insert into tag (name,id) values (?,?)
org.hibernate.SQL : insert into tag (name,id) values (?,?)
WARN org.hibernate.orm.jdbc.error : Unique index or primary key violation:
"PUBLIC.CONSTRAINT_143 INDEX PUBLIC.CONSTRAINT_INDEX_14 ON PUBLIC.TAG(NAME NULLS FIRST)
VALUES ( /* 3 */ 'sale' )"; SQL statement:
insert into tag (name,id) values (?,?) [23505-240]
```

Fix: `tags.flush()` between the two operations (test `deleteThenInsert_withFlushInBetween_works`).
The DELETE is sent first, then the INSERT succeeds:
```
org.hibernate.SQL : insert into tag (name,id) values (?,?)
org.hibernate.SQL : delete from tag where id=?
org.hibernate.SQL : insert into tag (name,id) values (?,?)
```


---

## Task 5

### Run the Task 5 tests (PowerShell, from `Lab-03\shop-jpa`)

```powershell
.\mvnw.cmd -q test "-Dtest=NPlusOneTest,BatchInsertTest"
```

The verified statement counts from `NPlusOneTest` are:

| variant | statements |
|---|---:|
| naive lazy loop | 51 |
| join fetch lines | 1 |
| entity graph (lines and products) | 1 |
| service-level batch size 16 | 5 |

The batch-size variant sets Hibernate's fetch batch size to 16 inside the service method. It does **not** use `@BatchSize(16)` on `Order.lines`, because that mapping-level setting also batches the naive variant and changes its expected count. These tests count statements; they do not measure elapsed time.

### Timing benchmark

No timing benchmark is currently implemented, so median times are not reported here. To complete that part of the task, run each variant after a warm-up, collect 20 elapsed-time samples under the same conditions, and record the median:

| variant | statements | median ms |
|---|---:|---:|
| naive lazy loop | 51 | not measured |
| join fetch lines | 1 | not measured |
| entity graph | 1 | not measured |
| service-level batch size 16 | 5 | not measured |

### Product-name fetching

The current report methods only call `order.getLines().size()`; they do not access `line.getProduct().getName()`. The repository entity-graph query fetches both `lines` and `lines.product`, while the join-fetch and batch-size variants fetch only the lines. Add and measure product-name report variants before claiming which ones regress into additional product queries. `default_batch_fetch_size=32` is not currently configured in `application.yml`; add it when testing the global batching comparison.

### JDBC insert batching

`application.yml` sets `hibernate.jdbc.batch_size=50` and `hibernate.order_inserts=true`. `BatchInsertTest` saves 1,000 products in one transaction and verifies 1,000 entity inserts and fewer than 100 prepared statements. Product IDs use a sequence with allocation size 50. The `IDENTITY` comparison is not part of the current test; perform it separately before documenting observed results. Batch logging is configured at `TRACE` for `org.hibernate.engine.jdbc.batch`.

### V2 indexes and EXPLAIN

The V2 migration adds indexes on `orders(customer_id, placed_at DESC)`, `order_line(order_id)`, and `product(category, amount)`. Capture actual H2 plans before and after applying V2; plans depend on the database version, schema contents, and query. The before/after plan output has not yet been recorded here.

In the H2 console, run the same query before and after the migration and paste the actual output:

```sql
EXPLAIN SELECT * FROM orders
WHERE customer_id = 1
ORDER BY placed_at DESC;
```