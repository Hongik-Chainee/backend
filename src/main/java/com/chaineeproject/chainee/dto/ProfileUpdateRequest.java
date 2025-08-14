package com.chaineeproject.chainee.dto;

import java.util.List;

public record ProfileUpdateRequest(
        String name,
        String profileImageUrl,
        List<String> positions,
        String from,
        String inLocation,
        String website,
        String introductionHeadline,
        String introductionContent
) {}
