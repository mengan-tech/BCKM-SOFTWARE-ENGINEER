package edu.itc.shop.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import edu.itc.shop.api.dto.ProductDto;
import edu.itc.shop.api.dto.ProductV2;
import edu.itc.shop.api.dto.StockChange;
import edu.itc.shop.api.filter.Compress;
import edu.itc.shop.api.params.ProductFilter;
import edu.itc.shop.domain.DuplicateSkuException;
import edu.itc.shop.domain.InsufficientStockException;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Product;
import edu.itc.shop.domain.ProductRepository;
import edu.itc.shop.domain.ReviewRepository;

@Path("/products")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ProductResource {
    static final String V2_JSON = "application/vnd.shop.v2+json";

    private final ProductRepository repo;
    private final ReviewRepository reviewRepo;   // the reviews sub-resource

    @Inject
    public ProductResource(ProductRepository repo, ReviewRepository reviewRepo) {
        this.repo = repo;
        this.reviewRepo = reviewRepo;
    }

    @GET
    @Compress
    public Response list(@BeanParam @Valid ProductFilter f, @Context UriInfo uriInfo) {
        List<Product> all = search(f);                                   // filtered + sorted
        int total = all.size();
        int lastPage = Math.max(0, (total - 1) / f.size);

        List<ProductDto> page = all.stream()
                .skip((long) f.page * f.size)
                .limit(f.size)
                .map(ProductDto::from)
                .toList();

        List<Link> links = new ArrayList<>();
        links.add(link(uriInfo, 0, "first"));
        if (f.page > 0) {
            links.add(link(uriInfo, Math.min(f.page - 1, lastPage), "prev"));
        }
        if (f.page < lastPage) {
            links.add(link(uriInfo, f.page + 1, "next"));
        }
        links.add(link(uriInfo, lastPage, "last"));

        return Response.ok(new GenericEntity<List<ProductDto>>(page) {})
                .links(links.toArray(Link[]::new))
                .header("X-Total-Count", total)
                .build();
    }

    // GET /products/{id} returns v1 or v2 depending on Accept header
    @GET
    @Path("{id: \\d+}")
    @Produces({"application/json; qs=1.0", "application/vnd.shop.v1+json", MediaType.APPLICATION_XML})
    public Response getById(@PathParam("id") long id, @Context Request request) {
        Product p = findOrThrow(id);
        return cacheable(p, ProductDto.from(p), request);
    }

    // GET /products/{id} returns v2 if Accept header is v2, otherwise 406 Not Acceptable
    @GET
    @Path("{id: \\d+}")
    @Produces(V2_JSON + "; qs=0.9")
    public Response getByIdV2(@PathParam("id") long id, @Context Request request) {
        Product p = findOrThrow(id);
        return cacheable(p, ProductV2.from(p), request);
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

    // PUT /products/{id} updates a product, requires If-Match header with ETag
    @PUT
    @Path("{id: \\d+}")
    public Response update(@PathParam("id") long id,
                           @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
                           @Valid @NotNull ProductDto dto,
                           @Context Request request) {
        Product existing = findOrThrow(id);                              // 404 first

        if (ifMatch == null || ifMatch.isBlank()) {
            throw new ClientErrorException("If-Match header is required to update a product",
                    Response.Status.PRECONDITION_REQUIRED);              // 428
        }
        EntityTag current = tagOf(existing);
        if (request.evaluatePreconditions(current) != null) {
            throw new ClientErrorException("If-Match " + ifMatch + " does not match the current ETag \""
                    + current.getValue() + "\"", Response.Status.PRECONDITION_FAILED);   // 412
        }

        // Another product must not already use the new SKU
        repo.findBySku(dto.sku)
            .filter(other -> other.id() != id)
            .ifPresent(other -> { throw new DuplicateSkuException(dto.sku); });

        Product updated = repo.save(dto.toProduct(id, existing.version()));
        return Response.ok(ProductDto.from(updated)).tag(tagOf(updated)).build();
    }

    @PATCH
    @Path("{id: \\d+}/stock")
    @Consumes(MediaType.APPLICATION_JSON) // Specific to JSON per instructions
    public ProductDto updateStock(
            @PathParam("id") long id, @Valid @NotNull StockChange change) {
        Product existing = findOrThrow(id);

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
        findOrThrow(id);
        repo.delete(id);
    }

    @Path("{id: \\d+}/reviews")
    public ReviewResource reviews(@PathParam("id") long id) {
        return new ReviewResource(findOrThrow(id), reviewRepo);
    }

    // for a given product id, find it or throw 404 Not Found
    private Product findOrThrow(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
    }

    // for a given filter, return the matching products, sorted and filtered
    private List<Product> search(ProductFilter f) {
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
                .toList();
    }

    // for a given Money, validate that the currency is EUR or null, otherwise throw 400 Bad Request
    private static void validateCurrency(Money money) {
        if (money != null && !"EUR".equals(money.currency())) {
            throw new BadRequestException("Only EUR prices are supported");
        }
    }

    // for a given page number, create a Link with the same query parameters but a different page
    private static Link link(UriInfo uriInfo, int page, String rel) {
        return Link.fromUriBuilder(uriInfo.getRequestUriBuilder().replaceQueryParam("page", page))
                .rel(rel)
                .build();
    }

    // for a given product, create an ETag from its version and return a 304 Not Modified or 200 OK with Cache-Control
    private static EntityTag tagOf(Product p) {
        return new EntityTag(Integer.toHexString(p.version()));
    }

    // for a given product, return a 304 Not Modified if the ETag matches, otherwise return 200 OK with Cache-Control
    private static Response cacheable(Product p, Object body, Request request) {
        EntityTag tag = tagOf(p);
        Response.ResponseBuilder notModified = request.evaluatePreconditions(tag);
        if (notModified != null) {
            return notModified.tag(tag).build();                         // 304 Not Modified
        }
        CacheControl cc = new CacheControl();
        cc.setPrivate(true);
        cc.setMaxAge(60);
        cc.setNoTransform(true);
        return Response.ok(body).tag(tag).cacheControl(cc).build();
    }
}