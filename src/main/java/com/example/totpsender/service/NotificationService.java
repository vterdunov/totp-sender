package com.example.totpsender.service;

public interface NotificationService {

    void sendCode(String destination, String code);

    String getChannelName();

    boolean isAvailable();
}
