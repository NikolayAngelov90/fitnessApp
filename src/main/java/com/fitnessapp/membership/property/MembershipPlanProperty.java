package com.fitnessapp.membership.property;

import com.fitnessapp.membership.model.MembershipPlanStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "subscriptions")
public class MembershipPlanProperty {

    private MembershipPlanStatus defaultStatus;
}
