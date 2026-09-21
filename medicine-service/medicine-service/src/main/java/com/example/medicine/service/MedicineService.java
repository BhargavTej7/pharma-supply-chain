package com.example.medicine.service;

import com.example.medicine.entity.Medicine;
import com.example.medicine.exception.InsufficientStockException;
import com.example.medicine.exception.MedicineNotFoundException;
import com.example.medicine.repository.MedicineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicineService {
    private final MedicineRepository medicineRepository;

    public MedicineService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Transactional
    public Medicine reduceStock(Long id, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (!medicineRepository.existsById(id)) {
            throw new MedicineNotFoundException("Medicine not found");
        }
        if (medicineRepository.reduceStock(id, quantity) != 1) {
            throw new InsufficientStockException("Insufficient stock");
        }
        return medicineRepository.findById(id).orElseThrow(() -> new MedicineNotFoundException("Medicine not found"));
    }
}