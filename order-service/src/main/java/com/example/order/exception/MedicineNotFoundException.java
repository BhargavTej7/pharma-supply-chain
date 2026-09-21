package com.example.order.exception;

public class MedicineNotFoundException extends RuntimeException {
    public MedicineNotFoundException(String message) { super(message); }
}
