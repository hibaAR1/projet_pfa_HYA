package com.exemple.pfa.repository;

import com.exemple.pfa.model.OcrResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OcrResultRepository extends JpaRepository<OcrResult, Long> {
    List<OcrResult> findByFileNameContaining(String fileName);
}
