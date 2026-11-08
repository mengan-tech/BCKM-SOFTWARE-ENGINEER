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