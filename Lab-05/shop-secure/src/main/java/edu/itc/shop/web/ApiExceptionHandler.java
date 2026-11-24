package edu.itc.shop.web;

import edu.itc.shop.service.ProductNotFoundException;
import edu.itc.shop.service.UsernameTakenException;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception, ServletWebRequest request) {
        List<Map<String, String>> errors = exception.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField)
                        .thenComparingInt(ApiExceptionHandler::validationOrder))
                .map(error -> Map.of("field", error.getField(), "message", formatFieldError(error)))
                .toList();
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Bad Request", "Validation failed");
        problem.setProperty("errors", errors);
        problem.setInstance(URI.create(request.getRequest().getRequestURI()));
        return problem;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail handleNotFound(ProductNotFoundException exception, ServletWebRequest request) {
        ProblemDetail problem = problem(HttpStatus.NOT_FOUND, "Not Found", exception.getMessage());
        problem.setInstance(URI.create(request.getRequest().getRequestURI()));
        return problem;
    }

    @ExceptionHandler(UsernameTakenException.class)
    ProblemDetail handleUsernameTaken(
            UsernameTakenException exception, ServletWebRequest request) {
        ProblemDetail problem = problem(HttpStatus.CONFLICT, "Conflict", exception.getMessage());
        problem.setInstance(URI.create(request.getRequest().getRequestURI()));
        return problem;
    }

    private static String formatFieldError(FieldError error) {
        return error.getDefaultMessage();
    }

    private static int validationOrder(FieldError error) {
        return switch (error.getCode()) {
            case "NotBlank" -> 0;
            case "Size" -> 1;
            case "Pattern" -> 2;
            default -> 3;
        };
    }

    public static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("about:blank"));
        problem.setTitle(title);
        return problem;
    }
}
