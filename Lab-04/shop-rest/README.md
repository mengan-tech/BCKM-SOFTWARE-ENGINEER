# Lab-04 - shop-rest (Jakarta REST 4.0 / Jersey 4)

## Build and run
```
mvn -q compile                 # first run downloads all dependencies
mvn -q compile exec:java       # starts the server, Ctrl+C to stop
mvn test                       # runs the JerseyTest classes
```

Base URI: `http://localhost:8080/api/`

---

## First resource and server
- Explaining why the resource class has no mutable fields even though it is stateless per request.
  > The resource class holds only the immutable `final` reference to `ProductRepository`; all per-request data (such as the product id looked up) lives in method-local parameters, so no mutable instance field can cause cross-request interference.



