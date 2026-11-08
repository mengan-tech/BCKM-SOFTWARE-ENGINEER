### Check that hibernat is the right version (do it one time only)
- .\mvnw.cmd dependency:tree "-Dincludes=org.hibernate.orm"

### To test the project in Lab-03:
- ./mvnw test

### To run the project in Lab-03:
- cd Lab-03\shop-jpa
- ./mvnw spring-boot:run

### To run the project in Lab-03 ex01 MappingTest:
./mvnw clean test -Dtest=ProductMappingTest

SchemaManagementException: Schema-validation: missing column [sku] in table [product]

nested exception is org.hibernate.MappingException: The increment size of the [product_seq] sequence is set to [50] in the entity mapping but the mapped database sequence increment size is [1]

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

### Entity `toString()` pitfall

Temporarily including `lines` in `Order.toString()` and `order` in `OrderLine.toString()`, then printing a detached order outside a transaction, reproduced a `org.hibernate.LazyInitializationException`. Formatting the relationship fields tries to initialize the lazy order lines after the persistence context is closed. If the relationships are initialized, traversing both sides can instead recurse indefinitely and cause a `StackOverflowError`. Entity `toString()` methods therefore include scalar fields only.