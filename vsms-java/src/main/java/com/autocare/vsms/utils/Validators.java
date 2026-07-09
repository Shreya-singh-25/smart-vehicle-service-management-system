package com.autocare.vsms.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Centralized input validation used across all forms in the application.
 * Every method returns a ValidationResult(valid, message).
 */
public final class Validators {

    public static final Pattern MOBILE_REGEX = Pattern.compile("^[6-9]\\d{9}$");
    public static final Pattern EMAIL_REGEX = Pattern.compile("^[\\w.\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$");
    public static final Pattern VEHICLE_NUMBER_REGEX = Pattern.compile("^[A-Za-z0-9\\- ]{4,15}$");

    private Validators() { }

    public static class ValidationResult {
        public final boolean valid;
        public final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, "");
    }

    public static ValidationResult fail(String message) {
        return new ValidationResult(false, message);
    }

    public static ValidationResult notEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fail(fieldName + " cannot be empty.");
        }
        return ok();
    }

    public static ValidationResult validateName(String value, String fieldName) {
        ValidationResult r = notEmpty(value, fieldName);
        if (!r.valid) return r;
        String trimmed = value.trim();
        if (trimmed.length() < 2) {
            return fail(fieldName + " must be at least 2 characters.");
        }
        for (char ch : value.toCharArray()) {
            if (!Character.isLetter(ch) && !Character.isWhitespace(ch) && ch != '.') {
                return fail(fieldName + " should contain only letters and spaces.");
            }
        }
        return ok();
    }

    public static ValidationResult validateMobile(String value) {
        ValidationResult r = notEmpty(value, "Mobile number");
        if (!r.valid) return r;
        if (!MOBILE_REGEX.matcher(value.trim()).matches()) {
            return fail("Enter a valid 10-digit mobile number (starting 6-9).");
        }
        return ok();
    }

    public static ValidationResult validateEmail(String value, boolean required) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            return required ? fail("Email cannot be empty.") : ok();
        }
        if (!EMAIL_REGEX.matcher(trimmed).matches()) {
            return fail("Enter a valid email address.");
        }
        return ok();
    }

    public static ValidationResult validateVehicleNumber(String value) {
        ValidationResult r = notEmpty(value, "Vehicle number");
        if (!r.valid) return r;
        if (!VEHICLE_NUMBER_REGEX.matcher(value.trim()).matches()) {
            return fail("Enter a valid vehicle registration number.");
        }
        return ok();
    }

    public static ValidationResult validateYear(String value) {
        ValidationResult r = notEmpty(value, "Manufacturing year");
        if (!r.valid) return r;
        int year;
        try {
            year = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fail("Manufacturing year must be a number.");
        }
        int currentYear = LocalDate.now().getYear();
        if (year < 1980 || year > currentYear + 1) {
            return fail("Enter a valid year between 1980 and " + (currentYear + 1) + ".");
        }
        return ok();
    }

    public static ValidationResult validatePositiveNumber(String value, String fieldName, boolean allowZero) {
        ValidationResult r = notEmpty(value, fieldName);
        if (!r.valid) return r;
        double num;
        try {
            num = Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fail(fieldName + " must be a number.");
        }
        if (allowZero && num < 0) {
            return fail(fieldName + " cannot be negative.");
        }
        if (!allowZero && num <= 0) {
            return fail(fieldName + " must be greater than zero.");
        }
        return ok();
    }

    public static ValidationResult validateInteger(String value, String fieldName, int minValue, int maxValue) {
        ValidationResult r = notEmpty(value, fieldName);
        if (!r.valid) return r;
        int num;
        try {
            num = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fail(fieldName + " must be a whole number.");
        }
        if (num < minValue) {
            return fail(fieldName + " must be at least " + minValue + ".");
        }
        if (num > maxValue) {
            return fail(fieldName + " must be at most " + maxValue + ".");
        }
        return ok();
    }

    public static ValidationResult validateDate(String value, String fieldName) {
        ValidationResult r = notEmpty(value, fieldName);
        if (!r.valid) return r;
        try {
            LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return fail(fieldName + " must be in YYYY-MM-DD format.");
        }
        return ok();
    }

    public static ValidationResult validatePassword(String value, int minLength) {
        ValidationResult r = notEmpty(value, "Password");
        if (!r.valid) return r;
        if (value.length() < minLength) {
            return fail("Password must be at least " + minLength + " characters.");
        }
        return ok();
    }
}
