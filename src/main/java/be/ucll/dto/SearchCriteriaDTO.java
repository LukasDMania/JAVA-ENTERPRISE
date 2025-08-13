package be.ucll.dto;

import com.sun.jna.platform.win32.Sspi;

import java.math.BigDecimal;
import java.security.Timestamp;
import java.time.LocalDateTime;

public class SearchCriteriaDTO {
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer productCount;
    private Boolean delivered;
    private String productName;
    private String email;
    private LocalDateTime createdDate;

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

    public Boolean isDeliveredNullable() {
        return delivered;
    }
    public void setDeliveredNullable(Boolean delivered) {
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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
