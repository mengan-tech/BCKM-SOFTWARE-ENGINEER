package edu.itc.shop.service;

import edu.itc.shop.domain.Order;
import edu.itc.shop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
class OrderServiceSecurityTest {

    @Autowired
    private OrderService orderService;

    @MockitoBean
    private OrderRepository orderRepository;

    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void aliceCanReadOwnHistory() {
        Order order = new Order("alice", 1L, 2);
        when(orderRepository.findByCustomer("alice")).thenReturn(List.of(order));

        List<Order> result = orderService.history("alice");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCustomer()).isEqualTo("alice");
    }

    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void aliceCannotReadBobsHistory() {
        assertThatThrownBy(() -> orderService.history("bob"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanReadBobsHistory() {
        Order order = new Order("bob", 1L, 1);
        when(orderRepository.findByCustomer("bob")).thenReturn(List.of(order));

        List<Order> result = orderService.history("bob");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCustomer()).isEqualTo("bob");
    }

    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void aliceCannotShipOrder() {
        assertThatThrownBy(() -> orderService.ship(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void unauthenticatedUserCannotCallSecuredMethod() {
        assertThatThrownBy(() -> orderService.history("alice"))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }
}