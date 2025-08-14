package be.ucll.repositories;

import be.ucll.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findNameByNameStartingWithIgnoreCase(String prefix);
}