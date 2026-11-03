### Check that hibernat is the right version (do it one time only)
- .\mvnw.cmd dependency:tree "-Dincludes=org.hibernate.orm"

### To test the project in Lab-03:
- ./mvnw test

### To run the project in Lab-03:
- cd Lab-03\shop-jpa
- ./mvnw spring-boot:run