package com.chaineeproject.chainee.dto;

public record ProjectBatchEditItem(
        Long projectId,
        boolean main,    // 체크박스: 메인
        boolean stored   // 체크박스: 보관(=비공개). true면 isPublic=false
) {}