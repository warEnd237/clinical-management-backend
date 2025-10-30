package com.clinical.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "doctors", indexes = {
    @Index(name = "idx_doctors_user_id", columnList = "user_id"),
    @Index(name = "idx_doctors_specialty", columnList = "specialty")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(nullable = false)
    private String specialty;
    
    @Column(name = "license_number", nullable = false, unique = true, length = 100)
    private String licenseNumber;
    
    @Column(length = 50)
    private String room;
    
    @Column(length = 50)
    private String phone;
    
    @Column(name = "consultation_fee_cents", nullable = false)
    @Builder.Default
    private Integer consultationFeeCents = 0;
    
    @Column(name = "available_from")
    private LocalTime availableFrom;
    
    @Column(name = "available_to")
    private LocalTime availableTo;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
