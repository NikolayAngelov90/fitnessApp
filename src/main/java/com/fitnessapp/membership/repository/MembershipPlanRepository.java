package com.fitnessapp.membership.repository;

import com.fitnessapp.membership.model.MembershipPlan;
import com.fitnessapp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, UUID> {

    List<MembershipPlan> findByClient(User client);

    List<MembershipPlan> findByEndDateBefore(LocalDate endDateBefore);
}
