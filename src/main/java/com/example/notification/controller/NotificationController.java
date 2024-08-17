package com.example.notification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    @Autowired
    private SseController sseController;

    @PostMapping("/send-notification")
    public void sendNotification(@RequestParam String clientId, @RequestParam String message) {
        sseController.notifySubscribers(clientId, message);
    }
}
