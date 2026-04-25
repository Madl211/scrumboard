package de.uni.scrumboard.data.entity;

import java.time.LocalDateTime;

public class ResetToken {
    private Long id;
    private Long accountId;
    private String token;
    private LocalDateTime expiryDate;
    private boolean used;
    
}