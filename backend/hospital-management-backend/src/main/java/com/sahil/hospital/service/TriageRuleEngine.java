package com.sahil.hospital.service;

import com.sahil.hospital.dto.symptom.SymptomAssessmentRequest;
import com.sahil.hospital.entity.AssessmentUrgency;
import com.sahil.hospital.entity.SymptomDuration;
import com.sahil.hospital.entity.SymptomSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TriageRuleEngine {

    public static final String CLINICAL_SAFETY_DISCLAIMER =
            "This assessment is an informational triage-support tool and does not provide a medical diagnosis, disease likelihood, or medication prescription. "
            + "This red-flag questionnaire covers select critical emergency indicators but is not comprehensive. "
            + "A negative response does not rule out serious illness or a medical emergency. "
            + "If you feel critically ill, experience severe distress, or believe you may have an emergency, seek emergency medical care immediately "
            + "(call 112 or visit the nearest emergency department) regardless of questionnaire results.";

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriageResult {
        private AssessmentUrgency urgencyLevel;
        private String suggestedSpecialty;
        private String triageSummary;
        private String safetyGuidance;
        private String recommendedAction;
        private String clinicalSafetyDisclaimer;
        private boolean redFlagsReported;
    }

    public TriageResult evaluate(SymptomAssessmentRequest request) {
        boolean redFlagsReported = Boolean.TRUE.equals(request.getHasCrushingChestPainOrPressure())
                || Boolean.TRUE.equals(request.getHasSevereShortnessOfBreath())
                || Boolean.TRUE.equals(request.getHasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty())
                || Boolean.TRUE.equals(request.getHasSevereUncontrolledBleeding())
                || Boolean.TRUE.equals(request.getHasSuddenLossOfConsciousness());

        boolean isCriticalSeverity = request.getSeverity() == SymptomSeverity.CRITICAL;

        AssessmentUrgency urgency;
        String specialty = determineSpecialty(request);
        String summary;
        String guidance;
        String action;

        // 1. EMERGENCY: CRITICAL severity OR any structured red flag is an absolute override
        if (isCriticalSeverity || redFlagsReported) {
            urgency = AssessmentUrgency.EMERGENCY;
            if (specialty.equals("General Medicine")) {
                specialty = "Emergency Medicine";
            }
            summary = "Critical red-flag indicators or critical symptom severity reported requiring immediate emergency medical evaluation.";
            guidance = "Do not drive yourself. Stay calm, sit or lie down in a comfortable position, and avoid physical exertion. Inform someone nearby immediately.";
            action = "Immediately call 112 (National Emergency Helpline) or proceed to the nearest hospital emergency department.";
        }
        // 2. URGENT: SEVERE severity OR MODERATE with extended duration (> 7 days)
        else if (request.getSeverity() == SymptomSeverity.SEVERE
                || (request.getSeverity() == SymptomSeverity.MODERATE
                    && (request.getDuration() == SymptomDuration.ONE_TO_TWO_WEEKS || request.getDuration() == SymptomDuration.MORE_THAN_TWO_WEEKS))) {
            urgency = AssessmentUrgency.URGENT;
            summary = "Significant symptom severity or prolonged symptoms identified requiring prompt clinical evaluation.";
            guidance = "Monitor symptoms closely for any worsening or appearance of red-flag symptoms. If breathing difficulty, severe chest pain, or sudden weakness develops, seek emergency care immediately.";
            action = "Seek prompt medical evaluation from a specialist within 24 to 48 hours.";
        }
        // 3. ROUTINE: MODERATE (<= 7 days) OR MILD with extended duration (> 3 days)
        else if (request.getSeverity() == SymptomSeverity.MODERATE
                || (request.getSeverity() == SymptomSeverity.MILD
                    && request.getDuration() != SymptomDuration.LESS_THAN_A_DAY
                    && request.getDuration() != SymptomDuration.ONE_TO_THREE_DAYS)) {
            urgency = AssessmentUrgency.ROUTINE;
            summary = "Symptoms of mild to moderate severity identified that warrant a standard outpatient physician consultation.";
            guidance = "Keep a daily log of symptoms and temperature. Ensure adequate rest and fluid intake. Seek prompt care if symptoms deteriorate.";
            action = "Schedule a standard consultation with an outpatient physician for assessment.";
        }
        // 4. SELF_CARE: MILD severity and short duration (<= 3 days) with zero red flags
        else {
            urgency = AssessmentUrgency.SELF_CARE;
            summary = "Mild, short-duration symptoms with zero red-flag indicators reported.";
            guidance = "Ensure adequate physical rest and maintain adequate oral hydration with clean fluids. Monitor your symptoms closely; if symptoms worsen, persist, or new symptoms develop, seek medical evaluation.";
            action = "Practice rest and self-monitoring. Consult a healthcare provider if symptoms do not improve within a few days.";
        }

        return TriageResult.builder()
                .urgencyLevel(urgency)
                .suggestedSpecialty(specialty)
                .triageSummary(summary)
                .safetyGuidance(guidance)
                .recommendedAction(action)
                .clinicalSafetyDisclaimer(CLINICAL_SAFETY_DISCLAIMER)
                .redFlagsReported(redFlagsReported)
                .build();
    }

    private String determineSpecialty(SymptomAssessmentRequest request) {
        String combined = (request.getPrimarySymptom() + " " + request.getSymptomsDescription() + " "
                + (request.getSelectedSymptoms() != null ? String.join(" ", request.getSelectedSymptoms()) : ""))
                .toLowerCase(Locale.ROOT);

        if (Boolean.TRUE.equals(request.getHasCrushingChestPainOrPressure())
                || combined.contains("chest pain") || combined.contains("heart") || combined.contains("palpitation")) {
            return "Cardiology";
        }
        if (Boolean.TRUE.equals(request.getHasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty())
                || combined.contains("headache") || combined.contains("dizziness") || combined.contains("seizure")
                || combined.contains("migraine") || combined.contains("numbness") || combined.contains("paralysis")) {
            return "Neurology";
        }
        if (Boolean.TRUE.equals(request.getHasSevereShortnessOfBreath())
                || combined.contains("breath") || combined.contains("cough") || combined.contains("wheez")
                || combined.contains("asthma") || combined.contains("respiratory")) {
            return "Pulmonology";
        }
        if (combined.contains("stomach") || combined.contains("abdomen") || combined.contains("abdominal")
                || combined.contains("vomit") || combined.contains("nausea") || combined.contains("diarrhea")
                || combined.contains("acidity") || combined.contains("gastric") || combined.contains("jaundice")) {
            return "Gastroenterology";
        }
        if (combined.contains("joint") || combined.contains("bone") || combined.contains("fracture")
                || combined.contains("knee") || combined.contains("back pain") || combined.contains("sprain")
                || combined.contains("ortho") || combined.contains("spine")) {
            return "Orthopedics";
        }
        if (combined.contains("skin") || combined.contains("rash") || combined.contains("itching")
                || combined.contains("hive") || combined.contains("acne") || combined.contains("dermat")) {
            return "Dermatology";
        }
        if (combined.contains("ear") || combined.contains("nose") || combined.contains("throat")
                || combined.contains("sinus") || combined.contains("tonsil") || combined.contains("cold")) {
            return "ENT";
        }
        if (combined.contains("eye") || combined.contains("vision") || combined.contains("blur")
                || combined.contains("cornea")) {
            return "Ophthalmology";
        }
        if (combined.contains("urinary") || combined.contains("urine") || combined.contains("kidney")
                || combined.contains("burning urination") || combined.contains("bladder")) {
            return "Urology";
        }

        return "General Medicine";
    }
}
