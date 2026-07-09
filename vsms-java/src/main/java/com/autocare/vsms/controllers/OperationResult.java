package com.autocare.vsms.controllers;

/**
 * Simple (success, message) result pair returned by controller operations,
 * mirroring the pattern used throughout the application's business logic.
 */
public class OperationResult {
    public final boolean success;
    public final String message;
    public final String generatedId; // optional, used by add/create operations

    public OperationResult(boolean success, String message) {
        this(success, message, null);
    }

    public OperationResult(boolean success, String message, String generatedId) {
        this.success = success;
        this.message = message;
        this.generatedId = generatedId;
    }

    public static OperationResult ok(String message) {
        return new OperationResult(true, message);
    }

    public static OperationResult ok(String message, String generatedId) {
        return new OperationResult(true, message, generatedId);
    }

    public static OperationResult fail(String message) {
        return new OperationResult(false, message);
    }
}
