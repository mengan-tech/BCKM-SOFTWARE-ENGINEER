package edu.itc.shop.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.itc.shop.config.ShopProperties;
import edu.itc.shop.domain.Product;
import edu.itc.shop.service.ProductService;
import edu.itc.shop.web.dto.CreateProduct;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductPageController {

    private static final Logger log = LoggerFactory.getLogger(ProductPageController.class);

    private final ProductService service;
    private final ShopProperties shopProps;

    public ProductPageController(ProductService service, ShopProperties shopProps) {
        this.service = service;
        this.shopProps = shopProps;
    }

    public record ProductView(
            long id,
            String name,
            BigDecimal price,
            BigDecimal gross,
            int stock) {
        // JavaBeans getters for JSTL EL (${p.name},${p.price}, ${p.gross},${p.stock})
        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public BigDecimal getGross() {
            return gross;
        }

        public int getStock() {
            return stock;
        }
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        BigDecimal multiplier = BigDecimal.ONE.add(shopProps.taxRate());
        List<ProductView> products = service.search(q).stream()
                .map(p -> new ProductView(
                        p.id(),
                        p.name(),
                        p.price(),
                        p.price().multiply(multiplier).setScale(2, RoundingMode.HALF_UP),
                        p.stock()))
                .toList();

        model.addAttribute("products", products);
        model.addAttribute("q", q);
        model.addAttribute("currency", shopProps.currency());
        model.addAttribute("threshold", shopProps.lowStockThreshold());
        return "products/list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("product", new CreateProduct("", null, 0));
        model.addAttribute("currency", shopProps.currency());
        return "products/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("product") CreateProduct form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.warn("Product form validation failed with {} error(s): {}",
                    bindingResult.getErrorCount(), bindingResult.getAllErrors());
            model.addAttribute("errors", bindingResult);
            model.addAttribute("currency", shopProps.currency());
            return "products/form";
        }

        Product created = service.create(form.name(), form.price(), form.stock());
        redirectAttributes.addFlashAttribute("message", "Created product: " + created.name());
        return "redirect:/products";
    }
}