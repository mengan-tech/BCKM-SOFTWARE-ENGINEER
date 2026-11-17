package edu.itc.shop.api.params;

import edu.itc.shop.domain.Money;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

public class ProductFilter {
    @QueryParam("q")
    public String q;

    @QueryParam("minPrice")
    public Money minPrice;

    @QueryParam("maxPrice")
    public Money maxPrice;

    @QueryParam("inStock")
    @DefaultValue("false")
    public boolean inStock;

    @QueryParam("sort")
    @DefaultValue("name,asc")
    @Pattern(regexp = "[a-z]+,(asc|desc)", message = "use field,asc or field,desc")
    public String sort;

    // paging start page at 0
    @QueryParam("page")
    @DefaultValue("0")
    @Min(0)
    public int page;

    @QueryParam("size")
    @DefaultValue("20")
    @Min(1)
    @Max(100)
    public int size;

    public String sortField() {
        return sort.split(",")[0];
    }

    public boolean descending() {
        return sort.endsWith(",desc");
    }
}