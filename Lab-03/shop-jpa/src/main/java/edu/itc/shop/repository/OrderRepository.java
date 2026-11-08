package edu.itc.shop.repository;

import java.time.Instant;
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

    @Query(value = """
        select distinct o.id as id, o.status as status, o.placedAt as placedAt, c as customer
        from Order o join o.customer c
        where o.status = :status
        """, countQuery = "select count(o) from Order o where o.status = :status")
    Page<OrderSummary> findByStatus(@Param("status") OrderStatus status, Pageable pageable);

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
}