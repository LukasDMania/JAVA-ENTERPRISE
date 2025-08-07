package be.ucll.services;

import be.ucll.dto.SearchCriteriaDTO;
import be.ucll.entities.Order;
import be.ucll.repositories.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
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

        List<Predicate> predicates = new ArrayList<>();
        
        if (searchCriteriaDTO.getMinAmount() != null) {
            predicates.add(cb.ge(order.get("totalPrice"), searchCriteriaDTO.getMinAmount()));
        }
        if (searchCriteriaDTO.getMaxAmount() != null) {
            predicates.add(cb.le(order.get("totalPrice"), searchCriteriaDTO.getMaxAmount()));
        }

        if (searchCriteriaDTO.isDelivered()) {
            predicates.add(cb.isTrue(order.get("delivered")));
        }

        // Add product name predicate
        if (searchCriteriaDTO.getProductName() != null && !searchCriteriaDTO.getProductName().isBlank()) {
            Join<Object, Object> productJoin = order.join("products");
            predicates.add(cb.like(cb.lower(productJoin.get("name")), "%" + searchCriteriaDTO.getProductName().toLowerCase() + "%"));
            query.distinct(true);
        }

        if (searchCriteriaDTO.getProductCount() != null) {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Order> subOrder = subquery.from(Order.class);
            subquery.select(subOrder.get("id"))
                    .where(cb.equal(cb.size(subOrder.get("products")), searchCriteriaDTO.getProductCount()));

            predicates.add(order.get("id").in(subquery));
        }

        query.select(order).where(predicates.toArray(new Predicate[0]));

        return em.createQuery(query).getResultList();
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }


    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}
