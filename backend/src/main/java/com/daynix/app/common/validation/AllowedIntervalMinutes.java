package com.daynix.app.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = AllowedIntervalMinutesValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface AllowedIntervalMinutes {

    String message() default "Interval minutes must be 15, 30, or 60";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
