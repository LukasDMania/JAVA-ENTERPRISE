package be.ucll.services;

import be.ucll.dto.SearchCriteriaDTO;
import be.ucll.entities.Order;
import be.ucll.repositories.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager em;

    public List<Order> searchOrders(SearchCriteriaDTO searchCriteriaDTO) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> query = cb.createQuery(Order.class);
        Root<Order> order = query.from(Order.class);

        order.fetch("products", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        if (searchCriteriaDTO.getMinAmount() != null) {
            predicates.add(cb.ge(order.get("totalPrice"), searchCriteriaDTO.getMinAmount()));
        }
        if (searchCriteriaDTO.getMaxAmount() != null) {
            predicates.add(cb.le(order.get("totalPrice"), searchCriteriaDTO.getMaxAmount()));
        }

        if (searchCriteriaDTO.isDeliveredNullable() != null) {
            if (searchCriteriaDTO.isDeliveredNullable()) {
                predicates.add(cb.isTrue(order.get("delivered")));
            } else {
                predicates.add(cb.isFalse(order.get("delivered")));
            }
        }

        if (searchCriteriaDTO.getProductName() != null && !searchCriteriaDTO.getProductName().isBlank()) {
            Join<Object, Object> productJoin = order.join("products", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(productJoin.get("name")),
                    "%" + searchCriteriaDTO.getProductName().toLowerCase() + "%"));
            query.distinct(true);
        }

        if (searchCriteriaDTO.getProductCount() != null) {
            predicates.add(cb.equal(cb.size(order.get("products")), searchCriteriaDTO.getProductCount()));
        }

        query.select(order).where(predicates.toArray(new Predicate[0]));

        return em.createQuery(query).getResultList();
    }

    @Transactional
    public Optional<Order> getOrderWithProducts(Long id) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        orderOpt.ifPresent(order -> order.getProducts().size());
        return orderOpt;
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }


    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}
