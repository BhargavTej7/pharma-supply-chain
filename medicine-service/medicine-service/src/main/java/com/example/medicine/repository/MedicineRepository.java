package com.example.medicine.repository;

import com.example.medicine.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
	@Modifying
	@Query("update Medicine m set m.stock = m.stock - :quantity where m.id = :id and m.stock >= :quantity")
	int reduceStock(@Param("id") Long id, @Param("quantity") int quantity);
}