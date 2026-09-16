package com.movieticket.util;

import com.movieticket.dao.BookingDAO;
import com.movieticket.dao.PaymentDAO;
import com.movieticket.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class PaymentService {
    
    private final PaymentDAO paymentDAO;
    private final BookingDAO bookingDAO;
    
    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
        this.bookingDAO = new BookingDAO();
    }
    
    public String generateTransactionId() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6).toUpperCase();
        return "CBPAY-" + datePart + "-" + randomPart;
    }
    
    public Payment initializePayment(int bookingId, BigDecimal amount, String method) {
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setTransactionId(generateTransactionId());
        payment.setPaymentStatus("PENDING");
        
        boolean created = paymentDAO.createPayment(payment);
        return created ? payment : null;
    }
    
    public boolean completePayment(Payment payment, String status) {
        // Update payment status
        boolean pUpdated = paymentDAO.updatePaymentStatus(payment.getTransactionId(), status);
        
        if (pUpdated && "SUCCESS".equals(status)) {
            // Update booking to CONFIRMED
            bookingDAO.updateBookingStatus(payment.getBookingId(), "CONFIRMED");
            payment.setPaymentStatus("SUCCESS");
            return true;
        } else if (pUpdated) {
            // FAILED or CANCELLED
            bookingDAO.updateBookingStatus(payment.getBookingId(), status);
            payment.setPaymentStatus(status);
            return true;
        }
        return false;
    }
    
    public java.util.Optional<Payment> getPaymentByBookingId(int bookingId) {
        return paymentDAO.getPaymentByBookingId(bookingId);
    }
}
