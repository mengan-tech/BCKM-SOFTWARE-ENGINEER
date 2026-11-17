package edu.itc.shop.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReviewRequest {
    @Min(1) @Max(5)
    public int stars;

    @NotBlank
    public String author;

    @Size(max = 500)
    public String text;

    public ReviewRequest() { }
}