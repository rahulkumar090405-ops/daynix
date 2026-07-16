package com.daynix.app.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AllowedIntervalMinutesValidator implements ConstraintValidator<AllowedIntervalMinutes, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == null || value == 15 || value == 30 || value == 60;
    }
}
