package com.fitnessapp.web;

import com.fitnessapp.payment.model.Payment;
import com.fitnessapp.payment.model.PaymentProductType;
import com.fitnessapp.payment.service.PaymentService;
import com.fitnessapp.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    @GetMapping("/history")
    public ModelAndView getPaymentsHistoryPage(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                               @RequestParam(name = "type", required = false) PaymentProductType type) {

        List<Payment> allUserPayments = paymentService.getAllUserPayments(customUserDetails.getUserId());

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
}
