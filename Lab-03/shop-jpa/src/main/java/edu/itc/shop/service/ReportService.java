package edu.itc.shop.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderLine;
import edu.itc.shop.domain.OrderStatus;
import edu.itc.shop.repository.OrderRepository;

@Service
public class ReportService {

    private final OrderRepository orders;
    private final EntityManager em;

    public ReportService(OrderRepository orders, EntityManager em) {
        this.orders = orders;
        this.em = em;
    }


    @Transactional(readOnly = true)
    public Map<Long, Integer> linesPerPaidOrder() {
        Session session = em.unwrap(Session.class);
        int oldBatchSize = session.getFetchBatchSize();

        session.setFetchBatchSize(1);
        try {
            Map<Long, Integer> result = new LinkedHashMap<>();
            for (Order o : orders.findByStatus(OrderStatus.PAID)) {
                result.put(o.getId(), o.getLines().size());
            }
            return result;
        } finally {
            session.setFetchBatchSize(oldBatchSize);
        }
    }


    @Transactional(readOnly = true)
    public Map<Long, Integer> linesPerPaidOrderJoinFetch() {
        Map<Long, Integer> result = new LinkedHashMap<>();
        for (Order o : orders.findByStatusWithLines(OrderStatus.PAID)) {
            result.put(o.getId(), o.getLines().size());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> linesPerPaidOrderEntityGraph() {
        Map<Long, Integer> result = new LinkedHashMap<>();
        for (Order o : orders.findWithGraphByStatus(OrderStatus.PAID)) {
            result.put(o.getId(), o.getLines().size());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> linesPerPaidOrderBatched() {
        Session session = em.unwrap(Session.class);
        int oldBatchSize = session.getFetchBatchSize();
        session.setFetchBatchSize(16);
        try {
            Map<Long, Integer> result = new LinkedHashMap<>();
            for (Order o : orders.findByStatus(OrderStatus.PAID)) {
                result.put(o.getId(), o.getLines().size());
            }
            return result;
        } finally {
            session.setFetchBatchSize(oldBatchSize);
        }
    }
}