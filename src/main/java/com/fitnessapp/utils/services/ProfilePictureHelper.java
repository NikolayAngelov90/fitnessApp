package com.fitnessapp.utils.services;

import com.fitnessapp.user.model.User;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class ProfilePictureHelper {

    public String resolveProfilePicture(User user) {
        if (user != null && user.getProfilePicture() != null) {
            return "data:image/jpeg;base64," +
                    Base64.getEncoder().encodeToString(user.getProfilePicture());
        }
        return "/images/Basic_Ui_(186).jpg";
    }
}
