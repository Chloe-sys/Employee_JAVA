package com.NE.chloe_Java.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true); // true enables HTML content

        mailSender.send(message);
    }

    public void sendPayslipNotification(String employeeEmail, String firstName, String month,
                                        String year, String amount, String employeeId) {
        String subject = "Payslip Generated - " + month + " " + year;
        String content = String.format("""
            Dear %s,
            
            Your salary for %s/%s from Rwanda Government amounting to RWF %s 
            has been credited to your account %s successfully.
            
            Best regards,
            Payroll Management System
            """, firstName, month, year, amount, employeeId);

        try {
            sendEmail(employeeEmail, subject, content);
        } catch (MessagingException e) {
            // Log error and handle gracefully
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
}