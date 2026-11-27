package com.sahil.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "symptom_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "primary_symptom", nullable = false, length = 200)
    private String primarySymptom;

    @Column(name = "symptoms_description", nullable = false, columnDefinition = "TEXT")
    private String symptomsDescription;

    @Column(name = "selected_symptoms", columnDefinition = "TEXT")
    private String selectedSymptoms;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration", nullable = false)
    private SymptomDuration duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private SymptomSeverity severity;

    @Column(name = "has_crushing_chest_pain_or_pressure", nullable = false)
    private boolean hasCrushingChestPainOrPressure;

    @Column(name = "has_severe_shortness_of_breath", nullable = false)
    private boolean hasSevereShortnessOfBreath;

    @Column(name = "has_sudden_weakness_or_numbness_or_facial_droop_or_speech_difficulty", nullable = false)
    private boolean hasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty;

    @Column(name = "has_severe_uncontrolled_bleeding", nullable = false)
    private boolean hasSevereUncontrolledBleeding;

    @Column(name = "has_sudden_loss_of_consciousness", nullable = false)
    private boolean hasSuddenLossOfConsciousness;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level", nullable = false)
    private AssessmentUrgency urgencyLevel;

    @Column(name = "suggested_specialty", length = 100)
    private String suggestedSpecialty;

    @Column(name = "triage_summary", columnDefinition = "TEXT", nullable = false)
    private String triageSummary;

    @Column(name = "safety_guidance", columnDefinition = "TEXT", nullable = false)
    private String safetyGuidance;

    @Column(name = "recommended_action", columnDefinition = "TEXT", nullable = false)
    private String recommendedAction;

    @Column(name = "clinical_safety_disclaimer", columnDefinition = "TEXT", nullable = false)
    private String clinicalSafetyDisclaimer;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
