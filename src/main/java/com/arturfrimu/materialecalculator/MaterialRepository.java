package com.arturfrimu.materialecalculator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialsEntity, Long> {
    // Poți adăuga metode personalizate aici, dacă este necesar
}
