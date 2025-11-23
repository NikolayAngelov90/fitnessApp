package com.fitnessapp.web;

import com.fitnessapp.payment.model.Payment;
import com.fitnessapp.payment.model.PaymentProductType;
import com.fitnessapp.payment.service.PaymentService;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentControllerUTest {

    private UserService userService;
    private PaymentService paymentService;
    private PaymentController controller;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        paymentService = mock(PaymentService.class);
        controller = new PaymentController(userService, paymentService);
    }

    private static Payment payment(PaymentProductType type, String status) {
        return Payment.builder()
                .amount(BigDecimal.ONE)
                .dateTime(LocalDateTime.now())
                .type(type)
                .status(status)
                .build();
    }

    @Test
    void getPaymentsHistoryPage_returnsAll_whenTypeNull() {
        UUID uid = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(uid, "e@e.com", "pwd", UserRole.CLIENT, false);
        User user = User.builder().id(uid).email("e@e.com").build();
        user.setPayments(List.of(
                payment(PaymentProductType.WORKOUT, "SUCCESS"),
                payment(PaymentProductType.SUBSCRIPTION, "SUCCESS")
        ));
        when(userService.getById(uid)).thenReturn(user);

        ModelAndView mv = controller.getPaymentsHistoryPage(cud, null);
        assertEquals("payment-history", mv.getViewName());
        List<?> list = (List<?>) mv.getModel().get("userPayments");
        assertEquals(2, list.size());
        assertNull(mv.getModel().get("type"));
    }

    @Test
    void getPaymentsHistoryPage_filtersByType_whenProvided() {
        UUID uid = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(uid, "e@e.com", "pwd", UserRole.CLIENT, false);
        User user = User.builder().id(uid).email("e@e.com").build();
        user.setPayments(List.of(
                payment(PaymentProductType.WORKOUT, "SUCCESS"),
                payment(PaymentProductType.SUBSCRIPTION, "SUCCESS")
        ));
        when(userService.getById(uid)).thenReturn(user);

        ModelAndView mv = controller.getPaymentsHistoryPage(cud, PaymentProductType.WORKOUT);
        assertEquals("payment-history", mv.getViewName());
        List<?> list = (List<?>) mv.getModel().get("userPayments");
        assertEquals(1, list.size());
        assertEquals(PaymentProductType.WORKOUT, mv.getModel().get("type"));
    }

    @Test
    void getPaymentReceipt_returnsViewWithPayment() {
        UUID id = UUID.randomUUID();
        Payment p = payment(PaymentProductType.SUBSCRIPTION, "SUCCESS");
        when(paymentService.getById(id)).thenReturn(p);

        ModelAndView mv = controller.getPaymentReceipt(id);
        assertEquals("payment-receipt", mv.getViewName());
        assertEquals(p, mv.getModel().get("payment"));
    }
}
