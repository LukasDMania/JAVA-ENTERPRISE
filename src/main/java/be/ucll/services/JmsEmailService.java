package be.ucll.services;

import be.ucll.dto.EmailOrderSummaryDTO;
import be.ucll.entities.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import jakarta.jms.Queue;
import java.util.stream.Collectors;

@Service
public class JmsEmailService {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Queue emailQueue;


    public void sendOrderSummaryEmail(String email, List<Order> currentOrders) {
        List<Long> orderIds = currentOrders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        EmailOrderSummaryDTO  message = new EmailOrderSummaryDTO(email, orderIds);

        jmsTemplate.convertAndSend(emailQueue, message);
    }
}
