package com.sahil.hospital.dto.symptom;

import com.sahil.hospital.entity.AssessmentUrgency;
import com.sahil.hospital.entity.SymptomDuration;
import com.sahil.hospital.entity.SymptomSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymptomAssessmentResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private String primarySymptom;
    private String symptomsDescription;
    private List<String> selectedSymptoms;
    private SymptomDuration duration;
    private SymptomSeverity severity;
    private boolean redFlagsReported;
    private AssessmentUrgency urgencyLevel;
    private String suggestedSpecialty;
    private String triageSummary;
    private String safetyGuidance;
    private String recommendedAction;
    private String clinicalSafetyDisclaimer;
    private LocalDateTime createdAt;
}
