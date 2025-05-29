package com.NE.chloe_Java.dto.deduction;


import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeductionResponse {
    private String code;
    private String deductionName;
    private BigDecimal percentage;
}
