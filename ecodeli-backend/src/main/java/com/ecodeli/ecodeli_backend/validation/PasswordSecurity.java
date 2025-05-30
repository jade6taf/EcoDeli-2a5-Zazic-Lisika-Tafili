package com.ecodeli.ecodeli_backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordSecurityValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordSecurity {
    String message() default "Le mot de passe ne respecte pas les critères de sécurité";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int minLength() default 8;

    int maxLength() default 128;

    int minCharacterTypes() default 3;

    boolean requireLowercase() default true;

    boolean requireUppercase() default true;

    boolean requireDigit() default true;

    boolean requireSpecialChar() default false;

    boolean prohibitCommonSequences() default true;

    boolean prohibitCommonPasswords() default true;

    boolean prohibitPersonalInfo() default true;
}
