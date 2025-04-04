package com.fitnessapp.payment.repository;

import com.fitnessapp.payment.model.Payment;
import com.fitnessapp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByClient(User client);
}
