package be.ucll.repositories;

import be.ucll.entities.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    List<Order> findAll();
    Optional<Order> findById(Long id);
    List<Order> findBySearchCriteria(String name);
}
