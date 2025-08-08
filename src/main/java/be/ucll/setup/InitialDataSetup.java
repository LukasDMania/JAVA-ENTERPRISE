package be.ucll.setup;

import be.ucll.entities.Order;
import be.ucll.entities.Product;
import be.ucll.entities.User;
import com.github.javafaker.Faker;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class InitialDataSetup {

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PersistenceContext
	private EntityManager entityManager;

	private final Faker faker = new Faker();
	private final Random random = new Random();

	@PostConstruct
	public void setup() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
		transactionTemplate.execute(e -> {
			User user = new User();
			user.setUsername("test");
			user.setPassword(passwordEncoder.encode("test"));
			user.setEmail("test@example.com");
			entityManager.persist(user);

			List<Product> products = generateProducts(5000);
			products.forEach(entityManager::persist);

			List<Order> orders = generateOrders(400, products);
			orders.forEach(entityManager::persist);

			return null;
		});
	}

	private List<Product> generateProducts(int count) {
		List<Product> products = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Product p = new Product();
			p.setName(faker.commerce().productName());
			p.setDescription(faker.lorem().sentence());
			p.setPrice(randomPrice(5, 9000));
			products.add(p);
		}
		return products;
	}

	private List<Order> generateOrders(int count, List<Product> products) {
		List<Order> orders = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Order order = new Order();
			order.setCustomerNumber("CUST-" + String.format("%03d", i + 1));
			order.setDelivered(random.nextBoolean());

			List<Product> orderProducts = random
					.ints(random.nextInt(10) + 1, 0, products.size())
					.distinct()
					.mapToObj(products::get)
					.toList();

			order.setProducts(orderProducts);

			BigDecimal total = orderProducts.stream()
					.map(Product::getPrice)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			order.setTotalPrice(total);

			orders.add(order);
		}
		return orders;
	}

	private BigDecimal randomPrice(int min, int max) {
		double price = min + (max - min) * random.nextDouble();
		return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
	}
}
