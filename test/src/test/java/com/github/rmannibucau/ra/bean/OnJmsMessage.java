package com.github.rmannibucau.ra.bean;

public class OnJmsMessage {
    private final String message;

    public OnJmsMessage(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
