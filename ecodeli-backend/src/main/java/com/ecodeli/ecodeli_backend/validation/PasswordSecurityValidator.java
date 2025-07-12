package com.ecodeli.ecodeli_backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PasswordSecurityValidator implements ConstraintValidator<PasswordSecurity, String> {

    private PasswordSecurity passwordSecurity;

    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

    private static final List<String> COMMON_SEQUENCES = Arrays.asList(
        "123456", "654321", "abcdef", "qwerty", "azerty", "password", "123123",
        "111111", "000000", "987654", "abcd", "1234", "asdf", "zxcv"
    );

    private static final List<String> COMMON_PASSWORDS = Arrays.asList(
        "password", "password123", "123456", "123456789", "qwerty", "azerty",
        "admin", "letmein", "welcome", "monkey", "dragon", "master", "shadow",
        "football", "baseball", "superman", "batman", "trustno1", "harley",
        "robert", "matthew", "jordan", "michelle", "daniel", "anthony",
        "passw0rd", "p@ssword", "p@ssw0rd", "motdepasse", "secret", "welcome123"
    );

    @Override
    public void initialize(PasswordSecurity constraintAnnotation) {
        this.passwordSecurity = constraintAnnotation;
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        context.disableDefaultConstraintViolation();
        boolean isValid = true;
        if (password.length() < passwordSecurity.minLength()) {
            context.buildConstraintViolationWithTemplate(
                String.format("Le mot de passe doit contenir au moins %d caractères", passwordSecurity.minLength())
            ).addConstraintViolation();
            isValid = false;
        }

        if (password.length() > passwordSecurity.maxLength()) {
            context.buildConstraintViolationWithTemplate(
                String.format("Le mot de passe ne peut pas dépasser %d caractères", passwordSecurity.maxLength())
            ).addConstraintViolation();
            isValid = false;
        }
        int characterTypes = 0;
        if (LOWERCASE_PATTERN.matcher(password).find()) {
            characterTypes++;
        } else if (passwordSecurity.requireLowercase()) {
            context.buildConstraintViolationWithTemplate(
                "Le mot de passe doit contenir au moins une lettre minuscule"
            ).addConstraintViolation();
            isValid = false;
        }

        if (UPPERCASE_PATTERN.matcher(password).find()) {
            characterTypes++;
        } else if (passwordSecurity.requireUppercase()) {
            context.buildConstraintViolationWithTemplate(
                "Le mot de passe doit contenir au moins une lettre majuscule"
            ).addConstraintViolation();
            isValid = false;
        }

        if (DIGIT_PATTERN.matcher(password).find()) {
            characterTypes++;
        } else if (passwordSecurity.requireDigit()) {
            context.buildConstraintViolationWithTemplate(
                "Le mot de passe doit contenir au moins un chiffre"
            ).addConstraintViolation();
            isValid = false;
        }

        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            characterTypes++;
        } else if (passwordSecurity.requireSpecialChar()) {
            context.buildConstraintViolationWithTemplate(
                "Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&*)"
            ).addConstraintViolation();
            isValid = false;
        }

        if (characterTypes < passwordSecurity.minCharacterTypes()) {
            context.buildConstraintViolationWithTemplate(
                String.format("Le mot de passe doit contenir au moins %d types de caractères différents",
                    passwordSecurity.minCharacterTypes())
            ).addConstraintViolation();
            isValid = false;
        }

        if (passwordSecurity.prohibitCommonSequences() && containsCommonSequence(password)) {
            context.buildConstraintViolationWithTemplate(
                "Le mot de passe ne doit pas contenir de séquences communes (123456, abcdef, etc.)"
            ).addConstraintViolation();
            isValid = false;
        }

        if (passwordSecurity.prohibitCommonPasswords() && isCommonPassword(password)) {
            context.buildConstraintViolationWithTemplate(
                "Ce mot de passe est trop couramment utilisé, veuillez en choisir un autre"
            ).addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }

    private boolean containsCommonSequence(String password) {
        String lowerPassword = password.toLowerCase();

        for (String sequence : COMMON_SEQUENCES) {
            if (lowerPassword.contains(sequence)) {
                return true;
            }
        }
        return containsSequentialNumbers(password) || containsSequentialLetters(lowerPassword);
    }

    private boolean containsSequentialNumbers(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (Character.isDigit(password.charAt(i))) {
                int count = 1;
                int diff = 0;
                for (int j = i + 1; j < password.length() && Character.isDigit(password.charAt(j)); j++) {
                    int currentDiff = password.charAt(j) - password.charAt(j - 1);
                    if (count == 1) {
                        diff = currentDiff;
                    }
                    if (currentDiff == diff && Math.abs(diff) == 1) {
                        count++;
                        if (count >= 4) {
                            return true;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return false;
    }

    private boolean containsSequentialLetters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (Character.isLetter(password.charAt(i))) {
                int count = 1;
                int diff = 0;

                for (int j = i + 1; j < password.length() && Character.isLetter(password.charAt(j)); j++) {
                    int currentDiff = password.charAt(j) - password.charAt(j - 1);
                    if (count == 1) {
                        diff = currentDiff;
                    }

                    if (currentDiff == diff && Math.abs(diff) == 1) {
                        count++;
                        if (count >= 4) {
                            return true;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return false;
    }

    private boolean isCommonPassword(String password) {
        String lowerPassword = password.toLowerCase();
        return COMMON_PASSWORDS.contains(lowerPassword);
    }

    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        score += Math.min(password.length() * 2, 25);

        if (LOWERCASE_PATTERN.matcher(password).find()) score += 10;
        if (UPPERCASE_PATTERN.matcher(password).find()) score += 10;
        if (DIGIT_PATTERN.matcher(password).find()) score += 10;
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) score += 10;

        if (password.length() >= 12) score += 10;
        if (password.length() >= 16) score += 10;
        if (hasVariedCharacters(password)) score += 15;

        PasswordSecurityValidator validator = new PasswordSecurityValidator();
        if (validator.containsCommonSequence(password)) score -= 20;
        if (validator.isCommonPassword(password)) score -= 30;

        return Math.max(0, Math.min(100, score));
    }

    private static boolean hasVariedCharacters(String password) {
        return password.chars().distinct().count() >= password.length() * 0.7;
    }
}
