<%-- src/main/webapp/WEB-INF/jsp/products/form.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
  <title>New Product</title>
  <link rel="stylesheet" href="/css/site.css">
</head>
<body>
<h1>New Product (${currency})</h1>

<c:if test="${not empty errors and errors.hasErrors()}">
  <ul class="errors">
    <c:forEach var="err" items="${errors.fieldErrors}">
      <li><strong><c:out value="${err.field}"/>:</strong> <c:out value="${err.defaultMessage}"/></li>
    </c:forEach>
  </ul>
</c:if>

<form method="post" action="/products">
  <div>
    <label>Name:
      <input type="text" name="name" value="<c:out value='${product.name()}'/>">
    </label>
  </div>
  <div>
    <label>Price (${currency}):
      <input type="number" step="0.01" name="price" value="<c:out value='${product.price()}'/>">
    </label>
  </div>
  <div>
    <label>Stock:
      <input type="number" name="stock" value="<c:out value='${product.stock()}'/>">
    </label>
  </div>
  <button type="submit">Create</button>
  <a href="/products">Cancel</a>
</form>
</body>
</html>