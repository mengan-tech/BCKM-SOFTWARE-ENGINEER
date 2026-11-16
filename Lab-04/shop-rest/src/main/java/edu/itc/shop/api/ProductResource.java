package edu.itc.shop.api;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import edu.itc.shop.api.dto.ProductDto;
import edu.itc.shop.api.dto.StockChange;
import edu.itc.shop.api.params.ProductFilter;
import edu.itc.shop.domain.Product;
import edu.itc.shop.domain.ProductRepository;
import edu.itc.shop.domain.DuplicateSkuException;
import edu.itc.shop.domain.InsufficientStockException;
import edu.itc.shop.domain.Money;

@Path("/products")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ProductResource {
    
    private final ProductRepository repo;

    @Inject
    public ProductResource(ProductRepository repo) { 
        this.repo = repo; 
    }

    @GET
    public List<ProductDto> list(@BeanParam @Valid ProductFilter f) {
        validateCurrency(f.minPrice);
        validateCurrency(f.maxPrice);

        Comparator<Product> comparator = switch (f.sortField()) {
            case "name" -> Comparator.comparing(Product::name, String.CASE_INSENSITIVE_ORDER);
            case "price" -> Comparator.comparing(Product::price);
            case "stock" -> Comparator.comparingInt(Product::stock);
            default -> throw new BadRequestException("Unknown sort field: " + f.sortField());
        };
        if (f.descending()) {
            comparator = comparator.reversed();
        }

        return repo.findAll().stream()
                .filter(p -> f.q == null
                        || p.name().toLowerCase(Locale.ROOT).contains(f.q.toLowerCase(Locale.ROOT)))
                .filter(p -> f.minPrice == null
                        || p.price().compareTo(f.minPrice.amount()) >= 0)
                .filter(p -> f.maxPrice == null
                        || p.price().compareTo(f.maxPrice.amount()) <= 0)
                .filter(p -> !f.inStock || p.stock() > 0)
                .sorted(comparator)
                .map(ProductDto::from)
                .collect(Collectors.toList());
    }

    @GET
    @Path("{id: \\d+}")
    public ProductDto getById(@PathParam("id") long id) {
        Product product = repo.findById(id)
            .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
        return ProductDto.from(product);
    }

    @POST
    public Response create(@Valid @NotNull ProductDto dto, @Context UriInfo uriInfo) {
        // 409 Conflict if SKU exists
        repo.findBySku(dto.sku).ifPresent(p -> { throw new DuplicateSkuException(dto.sku); });
        
        Product saved = repo.save(dto.toProduct(0, 0));
        
        // Return 201 Created with Location header
        URI location = uriInfo.getAbsolutePathBuilder().path(Long.toString(saved.id())).build();
        return Response.created(location).entity(ProductDto.from(saved)).build();
    }

   @PUT
@Path("{id: \\d+}")
public ProductDto update(@PathParam("id") long id, @Valid @NotNull ProductDto dto) {
    // This single line replaces the fetch AND the null check
    Product existing = repo.findById(id)
        .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
        
        Product updated = repo.save(dto.toProduct(id, existing.version()));
        return ProductDto.from(updated);
}

    @PATCH
    @Path("{id: \\d+}/stock")
    @Consumes(MediaType.APPLICATION_JSON) // Specific to JSON per instructions
    public ProductDto updateStock(
            @PathParam("id") long id, @Valid @NotNull StockChange change) {
        Product existing = repo.findById(id)
            .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
              
        int newStock = existing.stock() + change.delta();
        if (newStock < 0) {
            throw new InsufficientStockException(); // 409 Conflict
        }
        
        Product updated = new Product(existing.id(), existing.name(), existing.sku(), existing.price(), newStock, existing.version());
        return ProductDto.from(repo.save(updated));
    }

    @DELETE
    @Path("{id: \\d+}")
    public void delete(@PathParam("id") long id) {
        repo.findById(id)
            .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));;
        // JAX-RS automatically translates a void return to 204 No Content
        repo.delete(id); 
    }

    private static void validateCurrency(Money money) {
        if (money != null && !"EUR".equals(money.currency())) {
            throw new BadRequestException("Only EUR prices are supported");
        }
    }
}