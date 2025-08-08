package be.ucll.jms.listeners;

import be.ucll.dto.EmailOrderSummaryDTO;
import be.ucll.entities.Order;
import be.ucll.repositories.OrderRepository;
import be.ucll.services.EmailSenderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderEmailListener {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    @JmsListener(destination = "email-queue")
    public void onMessage(String jsonMessage) {
        try {
            EmailOrderSummaryDTO dto = objectMapper.readValue(jsonMessage, EmailOrderSummaryDTO.class);
            if (dto.getOrderIds() == null || dto.getOrderIds().isEmpty()) {
                System.err.println("No order IDs provided in message — skipping email send.");
                return;
            }
            List<Order> orders = orderRepository.findAllById(dto.getOrderIds());
            emailSenderService.sendOrderSummary(dto.getEmail(), orders);
        } catch (MessagingException e) {
            System.err.println("Could not send summary email: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error processing JMS message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
