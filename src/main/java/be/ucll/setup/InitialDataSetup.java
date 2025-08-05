package be.ucll.setup;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import be.ucll.entities.Order;
import be.ucll.entities.Product;
import be.ucll.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class InitialDataSetup {

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PersistenceContext
	private EntityManager entityManager;

	@PostConstruct
	public void setup() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
		transactionTemplate.execute(e -> {

			// User
			User user = new User();
			user.setUsername("test");
			//user.setPassword("test");
			user.setPassword(passwordEncoder.encode("test"));
			user.setEmail("test@example.com");
			entityManager.persist(user);

			// Products
			Product p1 = new Product();
			p1.setName("Laptop");
			p1.setDescription("Powerful 15 inch laptop");
			p1.setPrice(new BigDecimal("1299.99"));
			entityManager.persist(p1);

			Product p2 = new Product();
			p2.setName("Mouse");
			p2.setDescription("Ergonomic wireless mouse");
			p2.setPrice(new BigDecimal("25.99"));
			entityManager.persist(p2);

			// Order
			Order order = new Order();
			order.setCustomerNumber("CUST-001");
			order.setDelivered(false);
			order.setTotalPrice(p1.getPrice().add(p2.getPrice()));
			order.setProducts(List.of(p1, p2));
			entityManager.persist(order);

			return null;
		});
	}
}