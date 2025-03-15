package com.fitnessapp.web;

import com.fitnessapp.payment.model.Payment;
import com.fitnessapp.payment.model.PaymentProductType;
import com.fitnessapp.payment.service.PaymentService;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final UserService userService;
    private final PaymentService paymentService;

    public PaymentController(UserService userService,
                             PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }


    @GetMapping("/history")
    @PreAuthorize("hasRole('CLIENT')")
    public ModelAndView getPaymentsHistoryPage(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                               @RequestParam(name = "type", required = false) PaymentProductType type) {

        User user = userService.getById(customUserDetails.getUserId());
        List<Payment> allUserPayments = user.getPayments();

        if (type != null) {
            allUserPayments = allUserPayments
                    .stream()
                    .filter(p -> p.getType().equals(type))
                    .collect(Collectors.toList());
        }

        ModelAndView modelAndView = new ModelAndView("payment-history");
        modelAndView.addObject("userPayments", allUserPayments);
        modelAndView.addObject("type", type);

        return modelAndView;
    }

    @GetMapping("/{id}/receipt")
    @PreAuthorize("hasRole('CLIENT')")
    public ModelAndView getPaymentReceipt(@PathVariable UUID id) {

        Payment payment = paymentService.getById(id);

        ModelAndView modelAndView = new ModelAndView("payment-receipt");
        modelAndView.addObject("payment", payment);

        return modelAndView;
    }
}
