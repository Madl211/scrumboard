package de.uni.scrumboard.service;

import de.uni.scrumboard.application.ResetForm;
import de.uni.scrumboard.application.ResetPassword;
import de.uni.scrumboard.application.ResetResult;
import de.uni.scrumboard.data.Account;
import de.uni.scrumboard.data.AccountRepository;
import de.uni.scrumboard.data.ResetToken;
import de.uni.scrumboard.data.ResetTokenRepository;
import de.uni.scrumboard.data.PasswordUtil;

import java.time.LocalDateTime;

public class PasswordResetService {
    
    private final AccountRepository accountRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final PasswordUtil passwordUtil;
    
    public PasswordResetService(AccountRepository accountRepository, 
                                ResetTokenRepository resetTokenRepository,
                                PasswordUtil passwordUtil) {
        this.accountRepository = accountRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordUtil = passwordUtil;
    }
    
    // Passwort-Reset anfordern
    public ResetResult requestPasswordReset(ResetForm form) {
        // Account finden (per Email oder Benutzername)
        Account account = accountRepository.findByEmail(form.getUsernameEmail());
        if (account == null) {
            account = accountRepository.findByUsername(form.getUsernameEmail());
        }
        
        if (account == null) {
            return new ResetResult(false, "Wenn dieser Account existiert, hast du gleich eine Email bekommen.");
        }
        
        // Token generieren
        String tokenValue = TokenGenerator.generateToken();
        
        // Token erstellen
        ResetToken resetToken = new ResetToken();
        resetToken.setAccountId(account.getId());
        resetToken.setToken(tokenValue);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);
        
        // Alte Tokens löschen und neuen speichern
        resetTokenRepository.deleteByAccountId(account.getId());
        resetTokenRepository.save(resetToken);
        
        // Email senden
        sendResetEmail(account.getEmail(), tokenValue);
        
        return new ResetResult(true, "Wir haben dir einen Link zum Zurücksetzen deines Passworts gesendet.");
    }
    
    // Passwort zurücksetzen
    public ResetResult resetPassword(ResetPassword resetPassword) {
        // Token suchen
        ResetToken token = resetTokenRepository.findByToken(resetPassword.getToken())
            .orElse(null);
        
        // Token validieren
        if (token == null) {
            return new ResetResult(false, "Ungültiger oder abgelaufener Reset-Link.");
        }
        
        if (token.isUsed()) {
            return new ResetResult(false, "Dieser Reset-Link wurde bereits verwendet.");
        }
        
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            return new ResetResult(false, "Dieser Reset-Link ist abgelaufen.");
        }
        
        // Account finden
        Account account = accountRepository.findById(token.getAccountId());
        if (account == null) {
            return new ResetResult(false, "Account nicht gefunden.");
        }
        
        // Neues Passwort hashen und speichern
        String hashedPassword = passwordUtil.hashPassword(resetPassword.getNewPassword());
        account.setPasswordHash(hashedPassword);
        accountRepository.save(account);
        
        // Token als verwendet markieren
        token.setUsed(true);
        resetTokenRepository.save(token);
        
        return new ResetResult(true, "Passwort erfolgreich zurückgesetzt!");
    }
    
    // Email versenden (vorerst nur Konsole)
    private void sendResetEmail(String email, String token) {
        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        System.out.println("========================================");
        System.out.println("Passwort-Reset für: " + email);
        System.out.println("Link: " + resetLink);
        System.out.println("========================================");
    }
}