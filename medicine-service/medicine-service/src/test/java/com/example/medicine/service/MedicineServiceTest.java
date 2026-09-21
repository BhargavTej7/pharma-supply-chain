package com.example.medicine.service;

import com.example.medicine.entity.Medicine;
import com.example.medicine.exception.InsufficientStockException;
import com.example.medicine.repository.MedicineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineServiceTest {
    @Mock MedicineRepository repository;
    @InjectMocks MedicineService service;

    @Test
    void rejectsNonPositiveQuantityWithoutChangingStock() {
        assertThrows(IllegalArgumentException.class, () -> service.reduceStock(1L, 0));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsInsufficientStockWithoutReturningAnOrderableMedicine() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.reduceStock(1L, 5)).thenReturn(0);

        assertThrows(InsufficientStockException.class, () -> service.reduceStock(1L, 5));
        verify(repository, never()).findById(1L);
    }

    @Test
    void returnsMedicineAfterAtomicReduction() {
        Medicine medicine = new Medicine("A", "D", "C", 10.0, 3, false, "M");
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.reduceStock(1L, 2)).thenReturn(1);
        when(repository.findById(1L)).thenReturn(Optional.of(medicine));

        assertEquals(medicine, service.reduceStock(1L, 2));
        verify(repository).reduceStock(1L, 2);
    }
}