package de.uni.scrumboard.service;

import de.uni.scrumboard.application.ResetForm;
import de.uni.scrumboard.application.ResetPassword;
import de.uni.scrumboard.application.ResetResult;
import de.uni.scrumboard.data.Account;
import de.uni.scrumboard.data.AccountRepository;
import de.uni.scrumboard.data.PasswordResetToken;
import de.uni.scrumboard.data.ResetTokenRepository;
import de.uni.scrumboard.data.PasswordUtil;

import java.time.LocalDateTime;
import java.util.Optional;

public class PasswordResetService {
    
    private final AccountRepository accountRepository;
    private final ResetTokenRepository resetTokenRepository;
   // private final PasswordUtil passwordUtil;
    
    public PasswordResetService(AccountRepository accountRepository,
                                ResetTokenRepository resetTokenRepository) {
        this.accountRepository = accountRepository;
        this.resetTokenRepository = resetTokenRepository;
    }
    

    // Passwort-Reset ANFORDERN
  
    public ResetResult requestPasswordReset(ResetForm form) {
        // Account finden (per Email oder Benutzername)
        Optional<Account> accountOpt = accountRepository.findByEmail(form.getUsernameEmail());
        if (accountOpt.isEmpty()) {
            accountOpt = accountRepository.findByUsername(form.getUsernameEmail());
        }
        
        if (accountOpt.isEmpty()) {
            return new ResetResult(false, "Wenn dieser Account existiert, hast du gleich eine Email bekommen.");
        }
        
        Account account = accountOpt.get();
        
        // Token generieren
        String tokenValue = TokenGenerator.generateToken();
        
        // Token-Objekt erstellen
        PasswordResetToken resetToken = new PasswordResetToken();
        // Achtung: Record verwendet accountId(), nicht getAccountId()!
        resetToken.setAccountId((long) account.accountId());  // int zu Long konvertieren
        resetToken.setToken(tokenValue);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);
        
        // Alte Tokens löschen, neuen speichern
        resetTokenRepository.deleteByAccountId((long) account.accountId());
        resetTokenRepository.save(resetToken);
        
        // Email senden (Record verwendet email(), nicht getEmail()!)
        sendResetEmail(account.email(), tokenValue);
        
        return new ResetResult(true, "Wir haben dir einen Link zum Zurücksetzen deines Passworts gesendet.");
    }
    
    // Passwort zurücksetzen

    public ResetResult resetPassword(ResetPassword resetPassword) {
        // Token suchen
        Optional<PasswordResetToken> tokenOpt = resetTokenRepository.findByToken(resetPassword.getToken());
        
        if (tokenOpt.isEmpty()) {
            return new ResetResult(false, "Ungültiger oder abgelaufener Reset-Link.");
        }
        
        PasswordResetToken token = tokenOpt.get();
        
        if (token.isUsed()) {
            return new ResetResult(false, "Dieser Reset-Link wurde bereits verwendet.");
        }
        
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            return new ResetResult(false, "Dieser Reset-Link ist abgelaufen.");
        }
        
        // Account finden - konvertiere Long zu int
        Optional<Account> accountOpt = accountRepository.findById(token.getAccountId().intValue());
        if (accountOpt.isEmpty()) {
            return new ResetResult(false, "Account nicht gefunden.");
        }
        
        Account account = accountOpt.get();
        
        // Neues Passwort hashen
        String hashedPassword = PasswordUtil.hash(resetPassword.getNewPassword());
        
        Account updatedAccount = new Account(
            account.accountId(),
            account.email(),
            hashedPassword,
            account.username()
        );
        

        accountRepository.updateAccount(updatedAccount);
        
        // Token als verwendet markieren
        token.setUsed(true);
        resetTokenRepository.update(token);
        
        return new ResetResult(true, "Passwort erfolgreich zurückgesetzt!");
    }
    
    // ============================================================
    // Hilfsmethode: Email versenden (Konsole)
    // ============================================================
    private void sendResetEmail(String email, String token) {
        String resetLink = "http://localhost:8000/reset-password?token=" + token;
        System.out.println("========================================");
        System.out.println("Passwort-Reset für: " + email);
        System.out.println("Link: " + resetLink);
        System.out.println("========================================");
    }
}