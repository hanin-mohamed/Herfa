package com.ProjectGraduation.auth.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.auth.exception.OtpStillValidException;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int OTP_EXPIRATION_MINUTES = 10;
    private static final int OTP_LENGTH = 6;

    private final UserRepo repo;

    private final JavaMailSender mailSender;

    private final EncryptionService encryptionService;

    private final Random random = new Random();

    private String generateOtp() {
        return String.format("%0" + OTP_LENGTH + "d", random.nextInt((int) Math.pow(10, OTP_LENGTH)));
    }

    private void sendOtpEmail(String email, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("Herfa App <mf7373057@gmail.com>");
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setReplyTo("mf7373057@gmail.com"); // جديد

        String htmlContent = "<html><body>"
                + "<h2>Verification Code</h2>"
                + "<p>Your OTP code is:</p>"
                + "<p style='font-size: 22px; font-weight: bold; color: #333;'>" + text + "</p>"
                + "<p>This code will expire in 10 minutes.</p>"
                + "</body></html>";

        helper.setText(htmlContent, true);

        // تحسين الهيدر
        message.addHeader("X-Mailer", "Spring Boot Mailer");
        message.addHeader("Importance", "High");
        // message.addHeader("Precedence", "bulk"); // احذفي دي خالص

        mailSender.send(message);
    }


    public void generateAndSendOtp(String email) {
        User user = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        repo.save(user);

        try {
            sendOtpEmail(email, "Your Verification Code", otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    public boolean verifyOtp(String email, String otp) {
        User user = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.getOtp() == null || user.getOtpExpiration().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (!user.getOtp().equals(otp)) {
            return false;
        }

        user.setVerified(true);
        user.setOtp(null);
        repo.save(user);
        return true;
    }

    public void sendPasswordResetOtp(String email) {
        User user = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Email not found: " + email));

        String otp = generateOtp();
        user.setResetOtp(otp);
        user.setResetOtpExpiration(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        repo.save(user);

        try {
            sendOtpEmail(email, "Password Reset OTP", "Your OTP for password reset is: " + otp + "\nExpires in " + OTP_EXPIRATION_MINUTES + " minutes.");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifyResetOtp(String email, String otp) {
        User user = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.getResetOtp() == null || user.getResetOtpExpiration().isBefore(LocalDateTime.now())) {
            return false;
        }

        return user.getResetOtp().equals(otp);
    }

    public boolean updatePasswordWithOtp(String email, String otp, String newPassword) {
        User user = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.getResetOtp() == null || user.getResetOtpExpiration().isBefore(LocalDateTime.now()) || !user.getResetOtp().equals(otp)) {
            return false;
        }

        user.setPassword(encryptionService.encryptPassword(newPassword));
        user.setResetOtp(null);
        user.setResetOtpExpiration(null);
        repo.save(user);
        return true;
    }

    public void regenerateOtp(String email) {
        User user = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.getOtpExpiration() != null && user.getOtpExpiration().isAfter(LocalDateTime.now())) {
            throw new OtpStillValidException("Current OTP is still valid until " + user.getOtpExpiration());
        }

        String newOtp = generateOtp();
        user.setOtp(newOtp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        repo.save(user);

        try {
            sendOtpEmail(email, "New OTP Code", "Your new OTP code is: " + newOtp + "\nIt expires in " + OTP_EXPIRATION_MINUTES + " minutes.");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}