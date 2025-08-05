package be.ucll.repositories;

import be.ucll.entities.Product;

import java.util.List;

public interface ProductRepository {
    List<Product> findAll();
    List<Product> findByNameStartsWithIgnoreCase(String namePrefix);
}
