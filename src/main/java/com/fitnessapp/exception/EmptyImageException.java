package com.fitnessapp.exception;

public class EmptyImageException extends ImageUploadException {

    public EmptyImageException() {
        super("Please choice profile picture");
    }
}


