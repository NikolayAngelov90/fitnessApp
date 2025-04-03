package com.fitnessapp.utils;

import com.fitnessapp.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ProfilePictureHelperUTest {

    private static final String DEFAULT_IMAGE_PATH = "/images/Basic_Ui_(186).jpg";

    private ProfilePictureHelper profilePictureHelper;

    @BeforeEach
    void setUp() {
        profilePictureHelper = new ProfilePictureHelper();
    }

    @Test
    void givenUserWithProfilePicture_whenResolveProfilePicture_thenReturnDataUri() {
        // Given
        User user = new User();

        byte[] pictureBytes = "тестови-данни-за-снимка".getBytes();
        user.setProfilePicture(pictureBytes);

        String expectedBase64 = Base64.getEncoder().encodeToString(pictureBytes);
        String expectedDataUri = "data:image/jpeg;base64," + expectedBase64;

        String actualDataUri = profilePictureHelper.resolveProfilePicture(user);

        // Then
        assertEquals(expectedDataUri, actualDataUri);
    }

    @Test
    void givenUserWithoutProfilePicture_whenResolveProfilePicture_thenReturnDefaultPath() {
        // Given
        User user = new User();
        user.setProfilePicture(null);

        // When
        String actualPath = profilePictureHelper.resolveProfilePicture(user);

        // Then
        assertEquals(DEFAULT_IMAGE_PATH, actualPath);
    }

    @Test
    void givenNullUser_whenResolveProfilePicture_thenReturnDefaultPath() {
        // When
        String actualPath = profilePictureHelper.resolveProfilePicture(null);

        // Then
        assertEquals(DEFAULT_IMAGE_PATH, actualPath);
    }

    @Test
    void givenUserWithEmptyProfilePicture_whenResolveProfilePicture_thenReturnDataUriWithEmptyBase64() {
        // Given
        User user = new User();
        byte[] pictureBytes = new byte[0];
        user.setProfilePicture(pictureBytes);

        String expectedBase64 = Base64.getEncoder().encodeToString(pictureBytes);
        assertEquals("", expectedBase64);

        String expectedDataUri = "data:image/jpeg;base64," + expectedBase64;

        // When
        String actualDataUri = profilePictureHelper.resolveProfilePicture(user);

        // Then
        assertEquals(expectedDataUri, actualDataUri);
    }
}
