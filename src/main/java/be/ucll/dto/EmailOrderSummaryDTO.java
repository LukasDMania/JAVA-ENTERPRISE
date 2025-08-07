package be.ucll.dto;

import java.io.Serializable;
import java.util.List;

public class EmailOrderSummaryDTO implements Serializable {
    private String email;
    private List<Long> orderIds;

    public EmailOrderSummaryDTO() {

    }

    public EmailOrderSummaryDTO(String email, List<Long> orderIds) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public List<Long> getOrderIds() {
        return orderIds;
    }
    public void setOrderIds(List<Long> orderIds) {
        this.orderIds = orderIds;
    }
}
