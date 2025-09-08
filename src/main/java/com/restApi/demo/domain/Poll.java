package com.restApi.demo.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Poll {
    private UUID id = UUID.randomUUID();
    private UUID ownerId;
    private String question;
    private Instant publishedAt;
    private Instant validUntil;
    private List<VoteOption> options = new ArrayList<>();
}