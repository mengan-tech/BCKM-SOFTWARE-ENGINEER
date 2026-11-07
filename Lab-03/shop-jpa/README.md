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

### Entity `toString()` pitfall

Temporarily including `lines` in `Order.toString()` and `order` in `OrderLine.toString()`, then printing a detached order outside a transaction, reproduced a `org.hibernate.LazyInitializationException`. Formatting the relationship fields tries to initialize the lazy order lines after the persistence context is closed. If the relationships are initialized, traversing both sides can instead recurse indefinitely and cause a `StackOverflowError`. Entity `toString()` methods therefore include scalar fields only.