package edu.itc.shop.api;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import edu.itc.shop.api.dto.ProductDto;
import edu.itc.shop.api.dto.StockChange;
import edu.itc.shop.domain.Product;
import edu.itc.shop.domain.ProductRepository;
import edu.itc.shop.domain.DuplicateSkuException;
import edu.itc.shop.domain.InsufficientStockException;

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
    public List<ProductDto> list() {
        return repo.findAll().stream().map(ProductDto::from).collect(Collectors.toList());
    }

    @GET
    @Path("{id: \\d+}")
    public ProductDto getById(@PathParam("id") long id) {
        Product product = repo.findById(id)
            .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
        return ProductDto.from(product);
    }

    @POST
    public Response create(ProductDto dto, @Context UriInfo uriInfo) {
        // 409 Conflict if SKU exists
        repo.findBySku(dto.sku).ifPresent(p -> { throw new DuplicateSkuException(dto.sku); });
        
        Product saved = repo.save(dto.toProduct(0, 0));
        
        // Return 201 Created with Location header
        URI location = uriInfo.getAbsolutePathBuilder().path(Long.toString(saved.id())).build();
        return Response.created(location).entity(ProductDto.from(saved)).build();
    }

   @PUT
@Path("{id: \\d+}")
public ProductDto update(@PathParam("id") long id, ProductDto dto) {
    // This single line replaces the fetch AND the null check
    Product existing = repo.findById(id)
        .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
        
        Product updated = repo.save(dto.toProduct(id, existing.version()));
        return ProductDto.from(updated);
}

    @PATCH
    @Path("{id: \\d+}/stock")
    @Consumes(MediaType.APPLICATION_JSON) // Specific to JSON per instructions
    public ProductDto updateStock(@PathParam("id") long id, StockChange change) {
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
}