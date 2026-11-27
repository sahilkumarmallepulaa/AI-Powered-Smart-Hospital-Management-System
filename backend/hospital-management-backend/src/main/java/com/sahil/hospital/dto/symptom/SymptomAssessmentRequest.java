package com.sahil.hospital.dto.symptom;

import com.sahil.hospital.entity.SymptomDuration;
import com.sahil.hospital.entity.SymptomSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymptomAssessmentRequest {

    @NotBlank(message = "Primary symptom is required")
    @Size(max = 200, message = "Primary symptom must not exceed 200 characters")
    private String primarySymptom;

    @NotBlank(message = "Symptoms description is required")
    @Size(max = 2000, message = "Symptoms description must not exceed 2000 characters")
    private String symptomsDescription;

    private List<String> selectedSymptoms;

    @NotNull(message = "Symptom duration is required")
    private SymptomDuration duration;

    @NotNull(message = "Symptom severity is required")
    private SymptomSeverity severity;

    // Structured Red-Flag Checklist
    @NotNull(message = "Chest pain or pressure screening response is required")
    private Boolean hasCrushingChestPainOrPressure;

    @NotNull(message = "Shortness of breath screening response is required")
    private Boolean hasSevereShortnessOfBreath;

    @NotNull(message = "Stroke signs screening response (facial droop, weakness, speech difficulty) is required")
    private Boolean hasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty;

    @NotNull(message = "Uncontrolled bleeding screening response is required")
    private Boolean hasSevereUncontrolledBleeding;

    @NotNull(message = "Loss of consciousness screening response is required")
    private Boolean hasSuddenLossOfConsciousness;
}
