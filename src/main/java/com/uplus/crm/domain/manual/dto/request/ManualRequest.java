package com.uplus.crm.domain.manual.dto.request;

//작성 및 수정을 위한 요청 DTO
public record ManualRequest(
 String categoryCode,
 String title,
 String content
) {}