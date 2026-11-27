package com.sahil.hospital.service;

import com.sahil.hospital.dto.symptom.SymptomAssessmentRequest;
import com.sahil.hospital.entity.AssessmentUrgency;
import com.sahil.hospital.entity.SymptomDuration;
import com.sahil.hospital.entity.SymptomSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TriageRuleEngineTest {

    private TriageRuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        ruleEngine = new TriageRuleEngine();
    }

    private SymptomAssessmentRequest.SymptomAssessmentRequestBuilder baseRequestBuilder() {
        return SymptomAssessmentRequest.builder()
                .primarySymptom("Headache")
                .symptomsDescription("Mild throbbing headache")
                .duration(SymptomDuration.ONE_TO_THREE_DAYS)
                .severity(SymptomSeverity.MILD)
                .hasCrushingChestPainOrPressure(false)
                .hasSevereShortnessOfBreath(false)
                .hasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty(false)
                .hasSevereUncontrolledBleeding(false)
                .hasSuddenLossOfConsciousness(false);
    }

    @Test
    @DisplayName("CRITICAL severity unconditionally triggers EMERGENCY even if all red flags are false")
    void testCriticalSeverityTriggersEmergencyRegardlessOfRedFlags() {
        SymptomAssessmentRequest request = baseRequestBuilder()
                .primarySymptom("General malaise")
                .symptomsDescription("Feeling extremely unwell")
                .severity(SymptomSeverity.CRITICAL)
                .hasCrushingChestPainOrPressure(false)
                .hasSevereShortnessOfBreath(false)
                .hasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty(false)
                .hasSevereUncontrolledBleeding(false)
                .hasSuddenLossOfConsciousness(false)
                .build();

        TriageRuleEngine.TriageResult result = ruleEngine.evaluate(request);

        assertThat(result.getUrgencyLevel()).isEqualTo(AssessmentUrgency.EMERGENCY);
        assertThat(result.getRecommendedAction()).contains("112");
        assertThat(result.getClinicalSafetyDisclaimer()).contains("informational triage-support tool");
    }

    @Test
    @DisplayName("Crushing chest pain red-flag triggers EMERGENCY and Cardiology specialty")
    void testCrushingChestPainTriggersEmergencyAndCardiology() {
        SymptomAssessmentRequest request = baseRequestBuilder()
                .primarySymptom("Chest pain")
                .symptomsDescription("Heavy pressure on chest")
                .severity(SymptomSeverity.MODERATE)
                .hasCrushingChestPainOrPressure(true)
                .build();

        TriageRuleEngine.TriageResult result = ruleEngine.evaluate(request);

        assertThat(result.getUrgencyLevel()).isEqualTo(AssessmentUrgency.EMERGENCY);
        assertThat(result.isRedFlagsReported()).isTrue();
        assertThat(result.getSuggestedSpecialty()).isEqualTo("Cardiology");
        assertThat(result.getRecommendedAction()).contains("112");
    }

    @Test
    @DisplayName("Stroke symptoms (weakness, numbness, facial droop, speech difficulty) trigger EMERGENCY and Neurology")
    void testStrokeSignsTriggerEmergencyAndNeurology() {
        SymptomAssessmentRequest request = baseRequestBuilder()
                .primarySymptom("Facial droop and numbness")
                .symptomsDescription("Difficulty speaking and weakness on right arm")
                .severity(SymptomSeverity.MILD)
                .hasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty(true)
                .build();

        TriageRuleEngine.TriageResult result = ruleEngine.evaluate(request);

        assertThat(result.getUrgencyLevel()).isEqualTo(AssessmentUrgency.EMERGENCY);
        assertThat(result.isRedFlagsReported()).isTrue();
        assertThat(result.getSuggestedSpecialty()).isEqualTo("Neurology");
    }

    @Test
    @DisplayName("Severe shortness of breath triggers EMERGENCY and Pulmonology")
    void testShortnessOfBreathTriggersEmergencyAndPulmonology() {
        SymptomAssessmentRequest request = baseRequestBuilder()
                .primarySymptom("Breathlessness")
                .symptomsDescription("Struggling to breathe at rest")
                .severity(SymptomSeverity.MODERATE)
                .hasSevereShortnessOfBreath(true)
                .build();

        TriageRuleEngine.TriageResult result = ruleEngine.evaluate(request);

        assertThat(result.getUrgencyLevel()).isEqualTo(AssessmentUrgency.EMERGENCY);
        assertThat(result.isRedFlagsReported()).isTrue();
        assertThat(result.getSuggestedSpecialty()).isEqualTo("Pulmonology");
    }

    @Test
    @DisplayName("Severe uncontrolled bleeding triggers EMERGENCY")
    void testSevereUncontrolledBleedingTriggersEmergency() {
        SymptomAssessmentRequest request = baseRequestBuilder()
                .primarySymptom("Bleeding wound")
                .symptomsDescription("Continuous heavy blood flow")
                .hasSevereUncontrolledBleeding(true)
                .build();

        TriageRuleEngine.TriageResult result = ruleEngine.evaluate(request);

        assertThat(result.getUrgencyLevel()).isEqualTo(AssessmentUrgency.EMERGENCY);
        assertThat(result.isRedFlagsReported()).isTrue();
    }

    @Test
    @DisplayName("Sudden loss of consciousness triggers EMERGENCY")
    void testLossOfConsciousnessTriggersEmergency() {
        SymptomAssessmentRequest request = baseRequestBuilder()
                .primarySymptom("Fainting")
                .symptomsDescription("Sudden blackout and collapse")
                .hasSuddenLossOfConsciousness(true)
                .build();

        TriageRuleEngine.TriageResult result = ruleEngine.evaluate(request);

        assertThat(result.getUrgencyLevel()).isEqualTo(AssessmentUrgency.EMERGENCY);
        assertThat(result.isRedFlagsReported()).isTrue();
    }

    @Test
    @DisplayName("SEVERE severity without red flags triggers URGENT")
    void testSevereSeverityTriggersUrgent() {
        SymptomAssessmentRequest request = baseRequestBuilder()
                .primarySymptom("High fever")
                .symptomsDescription("Persistent high fever and shivering")
                .severity(SymptomSeverity.SEVERE)
                .duration(SymptomDuration.ONE_TO_THREE_DAYS)
                .build();

        TriageRuleEngine.TriageResult result = ruleEngine.evaluate(request);

        assertThat(result.getUrgencyLevel()).isEqualTo(AssessmentUrgency.URGENT);
        assertThat(result.isRedFlagsReported()).isFalse();
        assertThat(result.getRecommendedAction()).contains("24 to 48 hours");
    }

    @Test
    @DisplayName("MODERATE severity with extended duration (> 7 days) triggers URGENT")
    void testModerateWithExtendedDurationTriggersUrgent() {
        SymptomAssessmentRequest request = baseRequestBuilder()
                .primarySymptom("Abdominal pain")
                .symptomsDescription("Persistent stomach cramps")
                .severity(SymptomSeverity.MODERATE)
                .duration(SymptomDuration.ONE_TO_TWO_WEEKS)
                .build();

        TriageRuleEngine.TriageResult result = ruleEngine.evaluate(request);

        assertThat(result.getUrgencyLevel()).isEqualTo(AssessmentUrgency.URGENT);
        assertThat(result.getSuggestedSpecialty()).isEqualTo("Gastroenterology");
    }

    @Test
    @DisplayName("MODERATE severity with short duration (<= 7 days) triggers ROUTINE")
    void testModerateWithShortDurationTriggersRoutine() {
        SymptomAssessmentRequest request = baseRequestBuilder()
                .primarySymptom("Knee pain")
                .symptomsDescription("Mild swelling and joint pain")
                .severity(SymptomSeverity.MODERATE)
                .duration(SymptomDuration.FOUR_TO_SEVEN_DAYS)
                .build();

        TriageRuleEngine.TriageResult result = ruleEngine.evaluate(request);

        assertThat(result.getUrgencyLevel()).isEqualTo(AssessmentUrgency.ROUTINE);
        assertThat(result.getSuggestedSpecialty()).isEqualTo("Orthopedics");
    }

    @Test
    @DisplayName("MILD severity with duration > 3 days triggers ROUTINE")
    void testMildWithExtendedDurationTriggersRoutine() {
        SymptomAssessmentRequest request = baseRequestBuilder()
                .primarySymptom("Skin rash")
                .symptomsDescription("Mild itching and redness")
                .severity(SymptomSeverity.MILD)
                .duration(SymptomDuration.FOUR_TO_SEVEN_DAYS)
                .build();

        TriageRuleEngine.TriageResult result = ruleEngine.evaluate(request);

        assertThat(result.getUrgencyLevel()).isEqualTo(AssessmentUrgency.ROUTINE);
        assertThat(result.getSuggestedSpecialty()).isEqualTo("Dermatology");
    }

    @Test
    @DisplayName("MILD severity with short duration (<= 3 days) and no red flags triggers SELF_CARE with conservative guidance")
    void testMildShortDurationTriggersSelfCare() {
        SymptomAssessmentRequest request = baseRequestBuilder()
                .primarySymptom("Sore throat")
                .symptomsDescription("Mild scratchy throat after cold drink")
                .severity(SymptomSeverity.MILD)
                .duration(SymptomDuration.ONE_TO_THREE_DAYS)
                .build();

        TriageRuleEngine.TriageResult result = ruleEngine.evaluate(request);

        assertThat(result.getUrgencyLevel()).isEqualTo(AssessmentUrgency.SELF_CARE);
        assertThat(result.isRedFlagsReported()).isFalse();
        assertThat(result.getSuggestedSpecialty()).isEqualTo("ENT");
        assertThat(result.getSafetyGuidance()).contains("rest");
        assertThat(result.getSafetyGuidance()).contains("hydration");
        // Ensure no medication advice or prescription is provided
        assertThat(result.getSafetyGuidance()).doesNotContain("tablet");
        assertThat(result.getSafetyGuidance()).doesNotContain("mg");
        assertThat(result.getSafetyGuidance()).doesNotContain("antibiotic");
    }

    @Test
    @DisplayName("Specialty heuristic correctly maps Urology for urinary symptoms")
    void testSpecialtyMappingUrology() {
        SymptomAssessmentRequest request = baseRequestBuilder()
                .primarySymptom("Burning urination")
                .symptomsDescription("Pain during urination and frequent urination")
                .severity(SymptomSeverity.MILD)
                .duration(SymptomDuration.ONE_TO_THREE_DAYS)
                .build();

        TriageRuleEngine.TriageResult result = ruleEngine.evaluate(request);

        assertThat(result.getSuggestedSpecialty()).isEqualTo("Urology");
    }
}
