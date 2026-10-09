package com.platform.educational_platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    public void sendTempPassword(String toEmail, String name, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Cont creat pe platforma educațională");
        message.setText(
                "Bună ziua, " + (name != null ? name : toEmail) + ",\n\n" +
                        "Contul tău a fost creat pe platforma educațională.\n\n" +
                        "Date de autentificare:\n" +
                        "Email: " + toEmail + "\n" +
                        "Parolă temporară: " + tempPassword + "\n\n" +
                        "Te rugăm să îți schimbi parola la prima autentificare.\n\n" +
                        "Succes,\nEchipa platformei"
        );
        mailSender.send(message);
    }
}