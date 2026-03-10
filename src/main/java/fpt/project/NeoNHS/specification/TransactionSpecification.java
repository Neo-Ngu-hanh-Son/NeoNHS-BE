package fpt.project.NeoNHS.specification;

import fpt.project.NeoNHS.entity.Order;
import fpt.project.NeoNHS.entity.OrderDetail;
import fpt.project.NeoNHS.entity.TicketCatalog;
import fpt.project.NeoNHS.entity.Transaction;
import fpt.project.NeoNHS.enums.TransactionStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class TransactionSpecification {

    public static Specification<Transaction> hasUserId(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            Join<Transaction, Order> orderJoin = root.join("order");
            return criteriaBuilder.equal(orderJoin.get("user").get("id"), userId);
        };
    }

    public static Specification<Transaction> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            try {
                TransactionStatus transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
                return criteriaBuilder.equal(root.get("status"), transactionStatus);
            } catch (IllegalArgumentException e) {
                // If invalid status, return nothing or ignore? Usually safer to return empty
                // result for invalid filter
                return criteriaBuilder.disjunction();
            }
        };
    }

    public static Specification<Transaction> hasType(String type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null || type.isEmpty() || type.equalsIgnoreCase("ALL")) {
                return criteriaBuilder.conjunction();
            }

            // Join to OrderDetails to check the type
            Join<Transaction, Order> orderJoin = root.join("order");
            ListJoin<Order, OrderDetail> orderDetailsJoin = orderJoin.joinList("orderDetails");

            // To ensure we get distinct transactions since we are joining a list
            query.distinct(true);

            String typeUpper = type.toUpperCase();

            // WORKSHOP logic: OrderDetail has a workshopSession
            if ("WORKSHOP".equals(typeUpper)) {
                return criteriaBuilder.isNotNull(orderDetailsJoin.get("workshopSession"));
            }

            // EVENT logic: OrderDetail has a ticketCatalog AND ticketCatalog.event is not
            // null
            if ("EVENT".equals(typeUpper)) {
                Join<OrderDetail, TicketCatalog> ticketCatalogJoin = orderDetailsJoin.join("ticketCatalog",
                        JoinType.INNER);
                return criteriaBuilder.isNotNull(ticketCatalogJoin.get("event"));
            }

            // ENTRANCE logic: OrderDetail has a ticketCatalog AND ticketCatalog.attraction
            // is not null
            // (or event is null, depending on your business rule. Using attraction check is
            // safer if schema ensures it)
            if ("ENTRANCE".equals(typeUpper)) {
                Join<OrderDetail, TicketCatalog> ticketCatalogJoin = orderDetailsJoin.join("ticketCatalog",
                        JoinType.INNER);
                return criteriaBuilder.isNotNull(ticketCatalogJoin.get("attraction"));
            }

            return criteriaBuilder.disjunction();
        };
    }
}
