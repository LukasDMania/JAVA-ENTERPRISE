package be.ucll.services;

import be.ucll.dto.EmailOrderSummaryDTO;
import be.ucll.entities.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import jakarta.jms.Queue;
import java.util.stream.Collectors;

@Service
public class EmailQueueProducerService {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Queue emailQueue;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendOrderSummaryEmail(String email, List<Order> currentOrders) {
        List<Long> orderIds = currentOrders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        EmailOrderSummaryDTO  messageDto = new EmailOrderSummaryDTO(email, orderIds);

        try {
            String jsonMessage = objectMapper.writeValueAsString(messageDto);
            jmsTemplate.convertAndSend(emailQueue, jsonMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize EmailOrderSummaryDTO to JSON", e);
        }
    }
}
