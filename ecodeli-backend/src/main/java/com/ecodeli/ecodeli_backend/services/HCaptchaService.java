package com.ecodeli.ecodeli_backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class HCaptchaService {

    @Value("${hcaptcha.secret-key}")
    private String secretKey;

    private static final String HCAPTCHA_VERIFY_URL = "https://hcaptcha.com/siteverify";

    private final WebClient webClient;

    public HCaptchaService() {
        this.webClient = WebClient.builder().build();
    }

    public boolean validateCaptcha(String token, String remoteIp) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("secret", secretKey);
            formData.add("response", token);
            if (remoteIp != null && !remoteIp.trim().isEmpty()) {
                formData.add("remoteip", remoteIp);
            }

            Mono<HCaptchaResponse> responseMono = webClient.post()
                    .uri(HCAPTCHA_VERIFY_URL)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(HCaptchaResponse.class);

            HCaptchaResponse response = responseMono.block();

            if (response != null) {
                return response.isSuccess();
            }

            return false;
        } catch (Exception e) {
            System.err.println("Erreur lors de la validation hCaptcha: " + e.getMessage());
            return false;
        }
    }

    public static class HCaptchaResponse {

        @JsonProperty("success")
        private boolean success;

        @JsonProperty("challenge_ts")
        private String challengeTs;

        @JsonProperty("hostname")
        private String hostname;

        @JsonProperty("credit")
        private boolean credit;

        @JsonProperty("error-codes")
        private List<String> errorCodes;

        @JsonProperty("score")
        private Double score;

        @JsonProperty("score_reason")
        private List<String> scoreReason;

        public HCaptchaResponse() {}

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getChallengeTs() {
            return challengeTs;
        }

        public void setChallengeTs(String challengeTs) {
            this.challengeTs = challengeTs;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public boolean isCredit() {
            return credit;
        }

        public void setCredit(boolean credit) {
            this.credit = credit;
        }

        public List<String> getErrorCodes() {
            return errorCodes;
        }

        public void setErrorCodes(List<String> errorCodes) {
            this.errorCodes = errorCodes;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public List<String> getScoreReason() {
            return scoreReason;
        }

        public void setScoreReason(List<String> scoreReason) {
            this.scoreReason = scoreReason;
        }

        @Override
        public String toString() {
            return "HCaptchaResponse{" +
                    "success=" + success +
                    ", challengeTs='" + challengeTs + '\'' +
                    ", hostname='" + hostname + '\'' +
                    ", credit=" + credit +
                    ", errorCodes=" + errorCodes +
                    ", score=" + score +
                    ", scoreReason=" + scoreReason +
                    '}';
        }
    }
}
