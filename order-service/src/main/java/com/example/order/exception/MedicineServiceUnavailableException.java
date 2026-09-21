package com.example.order.exception;

public class MedicineServiceUnavailableException extends RuntimeException {
    public MedicineServiceUnavailableException(String message) { super(message); }
}
