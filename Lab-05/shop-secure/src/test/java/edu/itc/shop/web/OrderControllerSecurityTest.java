package edu.itc.shop.web;

import edu.itc.shop.config.SecurityConfig;
import edu.itc.shop.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void anonymousCannotShipReturns401() throws Exception {
        mvc.perform(post("/api/orders/1/ship"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotShipReturns403() throws Exception {
        mvc.perform(post("/api/orders/1/ship"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanShipReturns204() throws Exception {
        mvc.perform(post("/api/orders/1/ship"))
                .andExpect(status().isNoContent());

        verify(orderService).ship(1L);
    }
}