package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.validation.PasswordSecurityValidator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PasswordSecurityService {

    public PasswordValidationResult validatePassword(String password, Utilisateur user) {
        List<String> errors = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        if (password == null || password.trim().isEmpty()) {
            errors.add("Le mot de passe ne peut pas être vide");
            return new PasswordValidationResult(false, 0, errors, suggestions);
        }

        if (password.length() < 8) {
            errors.add("Le mot de passe doit contenir au moins 8 caractères");
        }

        if (password.length() > 128) {
            errors.add("Le mot de passe ne peut pas dépasser 128 caractères");
        }

        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        int characterTypes = 0;
        if (hasLower)
            characterTypes++;
        if (hasUpper)
            characterTypes++;
        if (hasDigit)
            characterTypes++;
        if (hasSpecial)
            characterTypes++;

        if (characterTypes < 3) {
            if (!hasLower) suggestions.add("Ajoutez des lettres minuscules");
            if (!hasUpper) suggestions.add("Ajoutez des lettres majuscules");
            if (!hasDigit) suggestions.add("Ajoutez des chiffres");
            if (!hasSpecial) suggestions.add("Ajoutez des caractères spéciaux (!@#$%^&*)");
        }

        if (user != null) {
            String lowerPassword = password.toLowerCase();
            if (user.getNom() != null && lowerPassword.contains(user.getNom().toLowerCase())) {
                errors.add("Le mot de passe ne doit pas contenir votre nom");
            }
            if (user.getPrenom() != null && lowerPassword.contains(user.getPrenom().toLowerCase())) {
                errors.add("Le mot de passe ne doit pas contenir votre prénom");
            }
            if (user.getEmail() != null) {
                String emailPrefix = user.getEmail().split("@")[0].toLowerCase();
                if (lowerPassword.contains(emailPrefix)) {
                    errors.add("Le mot de passe ne doit pas contenir votre adresse email");
                }
            }
        }

        int strength = PasswordSecurityValidator.calculatePasswordStrength(password);

        if (strength < 50) {
            suggestions.add("Utilisez un mot de passe plus long");
            suggestions.add("Mélangez différents types de caractères");
        } else if (strength < 75) {
            suggestions.add("Ajoutez quelques caractères pour renforcer votre mot de passe");
        }
        boolean isValid = errors.isEmpty() && strength >= 50;
        return new PasswordValidationResult(isValid, strength, errors, suggestions);
    }

    public int calculatePasswordStrength(String password) {
        return PasswordSecurityValidator.calculatePasswordStrength(password);
    }

    public String generateSecurePassword(int length) {
        if (length < 8)
            length = 12;
        if (length > 128)
            length = 128;
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        StringBuilder password = new StringBuilder();
        // Assurer au moins un caractère de chaque type
        password.append(lowercase.charAt((int) (Math.random() * lowercase.length())));
        password.append(uppercase.charAt((int) (Math.random() * uppercase.length())));
        password.append(digits.charAt((int) (Math.random() * digits.length())));
        password.append(special.charAt((int) (Math.random() * special.length())));
        // Remplir le reste
        String allChars = lowercase + uppercase + digits + special;
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt((int) (Math.random() * allChars.length())));
        }
        // Mélanger le mot de passe
        return shuffleString(password.toString());
    }

    private String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    public static class PasswordValidationResult {
        private final boolean valid;
        private final int strength;
        private final List<String> errors;
        private final List<String> suggestions;

        public PasswordValidationResult(boolean valid, int strength, List<String> errors, List<String> suggestions) {
            this.valid = valid;
            this.strength = strength;
            this.errors = errors;
            this.suggestions = suggestions;
        }

        public boolean isValid() {
            return valid;
        }

        public int getStrength() {
            return strength;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getSuggestions() {
            return suggestions;
        }

        public String getStrengthLabel() {
            if (strength < 25) return "Très faible";
            if (strength < 50) return "Faible";
            if (strength < 75) return "Moyen";
            if (strength < 90) return "Fort";
            return "Très fort";
        }
    }
}
