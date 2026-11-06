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