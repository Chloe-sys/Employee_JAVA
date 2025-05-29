package com.NE.chloe_Java.service;

import com.NE.chloe_Java.dto.deduction.DeductionRequest;
import com.NE.chloe_Java.dto.deduction.DeductionResponse;
import com.NE.chloe_Java.entity.Deduction;
import com.NE.chloe_Java.exception.ResourceNotFoundException;
import com.NE.chloe_Java.repository.DeductionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeductionService {

    private final DeductionRepository deductionRepository;

    @Transactional
    public DeductionResponse createDeduction(DeductionRequest request) {
        if (deductionRepository.existsByDeductionName(request.getDeductionName())) {
            throw new IllegalArgumentException("Deduction with this name already exists");
        }

        Deduction deduction = new Deduction();
        deduction.setDeductionName(request.getDeductionName());
        deduction.setPercentage(request.getPercentage());

        Deduction savedDeduction = deductionRepository.save(deduction);
        return mapToResponse(savedDeduction);
    }

    @Transactional(readOnly = true)
    public List<DeductionResponse> getDeductionsByEmployee(String employeeCode) {
        // This method needs to be implemented based on your employee-deduction relationship
        // For now, returning all deductions
        return deductionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeductionResponse getDeductionById(String id) {
        Deduction deduction = deductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found with id: " + id));
        return mapToResponse(deduction);
    }

    @Transactional
    public DeductionResponse updateDeduction(String id, DeductionRequest request) {
        Deduction deduction = deductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found with id: " + id));

        // Check if the new name conflicts with another deduction
        if (!deduction.getDeductionName().equals(request.getDeductionName()) &&
                deductionRepository.existsByDeductionName(request.getDeductionName())) {
            throw new IllegalArgumentException("Deduction with this name already exists");
        }

        deduction.setDeductionName(request.getDeductionName());
        deduction.setPercentage(request.getPercentage());

        Deduction updatedDeduction = deductionRepository.save(deduction);
        return mapToResponse(updatedDeduction);
    }

    @Transactional
    public void deleteDeduction(String id) {
        Deduction deduction = deductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found with id: " + id));
        deductionRepository.delete(deduction);
    }

    @Transactional(readOnly = true)
    public List<DeductionResponse> getActiveDeductions() {
        return deductionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DeductionResponse mapToResponse(Deduction deduction) {
        return DeductionResponse.builder()
                .code(deduction.getCode())
                .deductionName(deduction.getDeductionName())
                .percentage(deduction.getPercentage())
                .build();
    }
}