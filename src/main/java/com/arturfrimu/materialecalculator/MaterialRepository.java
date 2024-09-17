package com.arturfrimu.materialecalculator;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class MaterialRepository {
    private static final Map<Long, MaterialsEntity> map = new HashMap<>();

    public void addMaterials(MaterialsEntity material) {
        map.put(material.getId(), material);
    }

    public Optional<MaterialsEntity> findById(Long id) {
        return Optional.ofNullable(map.get(id));
    }

    public List<MaterialsEntity> findAll() {
        return map.values().stream().toList();
    }
}
