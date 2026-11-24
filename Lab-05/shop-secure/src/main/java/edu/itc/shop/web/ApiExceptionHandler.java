package edu.itc.shop.web;

import edu.itc.shop.service.ProductNotFoundException;
import java.net.URI;
import java.util.stream.Collectors;
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
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(ApiExceptionHandler::formatFieldError)
                .collect(Collectors.joining("; "));
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed", detail);
        problem.setInstance(URI.create(request.getRequest().getRequestURI()));
        return problem;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail handleNotFound(ProductNotFoundException exception, ServletWebRequest request) {
        ProblemDetail problem = problem(HttpStatus.NOT_FOUND, "Not Found", exception.getMessage());
        problem.setInstance(URI.create(request.getRequest().getRequestURI()));
        return problem;
    }

    private static String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    public static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("about:blank"));
        problem.setTitle(title);
        return problem;
    }
}
