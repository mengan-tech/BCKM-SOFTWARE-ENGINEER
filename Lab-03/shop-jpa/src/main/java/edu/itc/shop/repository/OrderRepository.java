package edu.itc.shop.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderStatus;
import edu.itc.shop.web.dto.OrderSummary;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerEmailAndStatusOrderByPlacedAtDesc(String email, OrderStatus status);

    long countByStatus(OrderStatus status);

    Order findFirstByCustomerIdOrderByPlacedAtDesc(Long customerId);

    Page<OrderSummary> findByStatus(OrderStatus status, Pageable pageable);

    @Query("""
            select distinct o from Order o
              join fetch o.lines l
              join fetch l.product
             where o.customer.email = :email
             order by o.placedAt desc
            """)
    List<Order> findWithLinesByCustomerEmail(@Param("email") String email);

    @Query("select count(l) from OrderLine l where l.order.status = :status")
    long countLinesByStatus(@Param("status") OrderStatus status);

    List<Order> findByStatus(OrderStatus status);

    @Query("select distinct o from Order o join fetch o.lines where o.status = :s")
    List<Order> findByStatusWithLines(@Param("s") OrderStatus status);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "lines", "lines.product" })
    List<Order> findWithGraphByStatus(OrderStatus status);

}