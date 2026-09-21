package com.example.medicine.controller;

import com.example.medicine.entity.Medicine;
import com.example.medicine.repository.MedicineRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import com.example.medicine.exception.MedicineNotFoundException;
import com.example.medicine.service.MedicineService;

import java.util.List;

@RestController
@RequestMapping("/medicines")
public class MedicineController {

    private final MedicineRepository medicineRepository;
    private final MedicineService medicineService;

    public MedicineController(MedicineRepository medicineRepository, MedicineService medicineService) {
        this.medicineRepository = medicineRepository;
        this.medicineService = medicineService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Medicine createMedicine(@Valid @RequestBody Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    @GetMapping
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    @GetMapping("/{id}")
    public Medicine getMedicineById(@PathVariable Long id) {
        return medicineRepository.findById(id).orElseThrow(() -> new MedicineNotFoundException("Medicine not found"));
    }

    @PutMapping("/{id}")
    public Medicine updateMedicine(@PathVariable Long id,
                                   @Valid @RequestBody Medicine medicine) {

        Medicine existingMedicine = getMedicineById(id);

        existingMedicine.setName(medicine.getName());
        existingMedicine.setDescription(medicine.getDescription());
        existingMedicine.setCategory(medicine.getCategory());
        existingMedicine.setPrice(medicine.getPrice());
        existingMedicine.setStock(medicine.getStock());
        existingMedicine.setPrescriptionRequired(medicine.isPrescriptionRequired());
        existingMedicine.setManufacturer(medicine.getManufacturer());

        return medicineRepository.save(existingMedicine);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedicine(@PathVariable Long id) {
        getMedicineById(id);
        medicineRepository.deleteById(id);
    }

    @PostMapping("/{id}/stock/reduce")
    public Medicine reduceStock(@PathVariable Long id, @RequestParam int quantity) {
        return medicineService.reduceStock(id, quantity);
    }
}