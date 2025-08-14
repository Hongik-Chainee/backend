package com.chaineeproject.chainee.dto;

import java.util.List;

public record ProjectBatchEditRequest(
        List<ProjectBatchEditItem> projects  // 전체 리스트(한 번에 업데이트)
) {}