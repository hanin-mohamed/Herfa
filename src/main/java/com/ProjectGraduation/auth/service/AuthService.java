package com.ProjectGraduation.auth.service;
import com.ProjectGraduation.auth.entity.Merchant;
import com.ProjectGraduation.auth.entity.repo.MerchantRepo;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    private static  final int OTP_EXPIRATION_MINUTES = 10 ;
    private static final int OTP_LENGTH = 6 ;
    @Autowired
    private MerchantRepo merchantRepo;

    @Autowired
    private JavaMailSender mailSender;
    @Autowired EncryptionService encryptionService ;

    private final Random random = new Random();
    private String generateOtp(){
        return String.format("%06d",random.nextInt(999999));
    }

    private void sendOtpEmail (String email , String subject , String text){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("mf7373057@gmail.com");
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
    public void generateAndSendOtp(String email) {
        Merchant merchant = merchantRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));

        // Generate 6-digit OTP
        String otp = generateOtp();

        // Store OTP with expiration time (5 minutes)
        merchant.setOtp(otp);
        merchant.setOtpExpiration(LocalDateTime.now().plusMinutes(5));
        merchantRepo.save(merchant);

        sendOtpEmail(email, "Your OTP Code", "Your OTP code is: " + otp + "\nIt expires in 5 minutes.");
    }

//    private void sendOtpEmail(String email, String otp) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("mf7373057@gmail.com");  // 🔹 Add this line
//        message.setTo(email);
//        message.setSubject("Your OTP Code");
//        message.setText("Your OTP code is: " + otp + ". It will expire in 5 minutes.");
//
//        mailSender.send(message);
//    }


    public boolean verifyOtp(String email, String otp) {
        Optional<Merchant> merchantOpt = merchantRepo.findByEmailIgnoreCase(email);

        if (merchantOpt.isEmpty()) return false;

        Merchant merchant = merchantOpt.get();

        // Check OTP expiration and validity
        if (merchant.getOtp() == null || merchant.getOtpExpiration().isBefore(LocalDateTime.now())) {
            return false; // OTP expired
        }

        if (!merchant.getOtp().equals(otp)) {
            return false; // Invalid OTP
        }

        // Mark email as verified and clear OTP
        merchant.setVerified(true);
        merchant.setOtp(otp);
//        merchant.setOtpExpiration();
        merchantRepo.save(merchant);

        return true;
    }
    public void sendPasswordResetOtp(String email) {
        Merchant merchant = merchantRepo.findByEmailIgnoreCase(email)
                .orElseThrow(()->new IllegalArgumentException("Email , not found !!")) ;

        String otp = generateOtp() ;
        merchant.setResetToken(otp);
        merchant.setResetTokenExpiration(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        merchantRepo.save(merchant);
        sendOtpEmail(email, "Password Reset OTP", "Your OTP for password reset is: " + otp + "\nExpires in 10 minutes.");

    }

    public boolean verifyResetOtp(String email , String otp) {
        Optional<Merchant> merchantOtp = merchantRepo.findByEmailIgnoreCase(email) ;
        if (merchantOtp.isEmpty())return false ;
        Merchant merchant = merchantOtp.get() ;
        return merchant.getResetToken() != null &&
                merchant.getResetToken().equals(otp)&&
                merchant.getResetTokenExpiration().isAfter(LocalDateTime.now());
    }

    public boolean updatePasswordWithOtp (String email , String otp , String newPassword){

        if (!verifyResetOtp(email, otp))return false ;

        Merchant merchant = merchantRepo.findByEmailIgnoreCase(email)
                .orElseThrow(()->new IllegalArgumentException("Invaild email !!")) ;

        merchant . setPassword(encryptionService.encryptPassword(newPassword));
        merchant.setResetToken(otp);
//        merchant.setResetTokenExpiration(null);
        merchantRepo.save(merchant);
        return true;
    }

    public void regenerateOtp(String email) {
        Merchant merchant = merchantRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));

        if (merchant.getOtpExpiration() != null && merchant.getOtpExpiration().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("OTP is still valid. Please use the current OTP.");
        }

        // Generate new OTP
        String newOtp = generateOtp();
        merchant.setOtp(newOtp);
        merchant.setOtpExpiration(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        merchantRepo.save(merchant);

        // Send new OTP via email
        sendOtpEmail(email, "New OTP Code", "Your new OTP code is: " + newOtp + "\nIt expires in " + OTP_EXPIRATION_MINUTES + " minutes.");
    }

}
