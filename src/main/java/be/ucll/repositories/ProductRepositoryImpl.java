package be.ucll.repositories;

import be.ucll.entities.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

public class ProductRepositoryImpl implements ProductRepository{
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Product> findAll() {
        return em.createQuery("FROM Product p", Product.class).getResultList();
    }

    @Override
    public List<Product> findByNameStartsWithIgnoreCase(String namePrefix) {
        return em.createQuery("FROM Product p WHERE LOWER(p.name) LIKE :name", Product.class)
                .setParameter("name", namePrefix.toLowerCase() + "%")
                .getResultList();
    }
}
