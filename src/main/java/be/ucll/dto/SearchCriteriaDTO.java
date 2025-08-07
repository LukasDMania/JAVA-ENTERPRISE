package be.ucll.dto;

import java.math.BigDecimal;

public class SearchCriteriaDTO {
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer productCount;
    private boolean delivered;
    private String productName;
    private String email;

    public BigDecimal getMinAmount() {
        return minAmount;
    }
    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }
    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Integer getProductCount() {
        return productCount;
    }
    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }

    public boolean isDelivered() {
        return delivered;
    }
    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
