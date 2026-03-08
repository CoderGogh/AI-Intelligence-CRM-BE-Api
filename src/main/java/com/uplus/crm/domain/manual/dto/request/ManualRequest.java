package com.uplus.crm.domain.manual.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ManualRequest(
    @NotBlank(message = "카테고리 코드는 필수입니다.")
    String categoryCode,

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이내여야 합니다.")
    String title,

    @NotBlank(message = "내용은 필수입니다.")
    String content
) {}