package be.ucll.services;

import be.ucll.entities.Product;
import be.ucll.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<String> autocompleteProductNames(String prefix) {
        List<Product> productList = productRepository.findNameByNameStartingWithIgnoreCase(prefix);
        List<String> produktNameList = new ArrayList<>();
        for (Product product : productList) {
            produktNameList.add(product.getName());
        }
        return produktNameList;
    }
}
