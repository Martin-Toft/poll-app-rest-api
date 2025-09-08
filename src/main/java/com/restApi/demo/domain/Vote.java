package com.restApi.demo.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vote {
    private UUID id = UUID.randomUUID();
    private UUID pollId;
    private UUID userId;
    private UUID optionId;
    private Instant publishedAt = Instant.now();
}