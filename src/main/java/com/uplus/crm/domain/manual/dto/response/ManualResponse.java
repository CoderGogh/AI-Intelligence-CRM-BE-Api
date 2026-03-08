package com.uplus.crm.domain.manual.dto.response;

import java.time.LocalDateTime;

import com.uplus.crm.domain.manual.entity.Manual;

public record ManualResponse(
	    Integer manualId,
	    String categoryCode,
	    String categoryName,  // [대 > 중 > 소] 형식의 전체 카테고리명
	    String title,
	    String content,
	    Boolean isActive,
	    String empName,   // 실제 작성자 이름 (Employee.name) ⭐
	    LocalDateTime updatedAt
	) {
	    public static ManualResponse from(Manual manual) {
	        // 카테고리 정책 엔티티
	        var policy = manual.getCategoryPolicy();
	        
	        return new ManualResponse(
	            manual.getManualId(),
	            policy.getCategoryCode(),
	            String.format("[%s > %s > %s]", 
	                policy.getLargeCategory(), 
	                policy.getMediumCategory(), 
	                policy.getSmallCategory()),
	            manual.getTitle(),
	            manual.getContent(),
	            manual.getIsActive(),
	            manual.getEmployee().getName(),
	            manual.getUpdatedAt()
	        );
	    }
}