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

			Product p3 = new Product();
			p3.setName("Keyboard");
			p3.setDescription("Mechanical RGB keyboard");
			p3.setPrice(new BigDecimal("89.50"));
			entityManager.persist(p3);

			Product p4 = new Product();
			p4.setName("Monitor");
			p4.setDescription("27 inch 4K monitor");
			p4.setPrice(new BigDecimal("379.00"));
			entityManager.persist(p4);

			Product p5 = new Product();
			p5.setName("USB Hub");
			p5.setDescription("4-port USB 3.0 hub");
			p5.setPrice(new BigDecimal("19.99"));
			entityManager.persist(p5);

			Product p6 = new Product();
			p6.setName("Desk Lamp");
			p6.setDescription("LED adjustable desk lamp");
			p6.setPrice(new BigDecimal("34.75"));
			entityManager.persist(p6);

			Order order1 = new Order();
			order1.setCustomerNumber("CUST-001");
			order1.setDelivered(false);
			order1.setProducts(List.of(p1, p2));
			order1.setTotalPrice(p1.getPrice().add(p2.getPrice()));
			entityManager.persist(order1);

			Order order2 = new Order();
			order2.setCustomerNumber("CUST-002");
			order2.setDelivered(true);
			order2.setProducts(List.of(p3, p4, p5));
			order2.setTotalPrice(p3.getPrice().add(p4.getPrice()).add(p5.getPrice()));
			entityManager.persist(order2);

			Order order3 = new Order();
			order3.setCustomerNumber("CUST-003");
			order3.setDelivered(false);
			order3.setProducts(List.of(p6));
			order3.setTotalPrice(p6.getPrice());
			entityManager.persist(order3);

			Order order4 = new Order();
			order4.setCustomerNumber("CUST-004");
			order4.setDelivered(true);
			order4.setProducts(List.of(p1, p3, p4, p6));
			order4.setTotalPrice(
					p1.getPrice().add(p3.getPrice()).add(p4.getPrice()).add(p6.getPrice())
			);
			entityManager.persist(order4);

			return null;
		});
	}
}