package com.fitnessapp.web.mapper;

import com.fitnessapp.user.model.User;
import com.fitnessapp.web.dto.UserEditRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static UserEditRequest mapUserToUserEditRequest(User user) {

        return new UserEditRequest(
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getSpecialization(),
                user.getDescription());
    }
}
