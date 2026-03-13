package com.uplus.crm.domain.extraction.dto.response;

import com.uplus.crm.domain.extraction.entity.SelectionStatus;
import java.time.LocalDateTime;

public record EvaluationListResponse(
    Long consultId,          // 1. e.consultId (Long)
    String categoryName,     // 2. p.smallCategory (String)
    String counselorName,    // 3. emp.name (String)
    Integer score,           // 4. e.score (Integer)
    String title,            // 5. a.rawSummary (String)
    SelectionStatus selectionStatus, // 6. e.selectionStatus (Enum)
    LocalDateTime createdAt  // 7. e.createdAt (LocalDateTime)
) {}