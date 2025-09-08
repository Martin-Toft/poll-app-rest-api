package com.restApi.demo.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteOption {
    private UUID id = UUID.randomUUID();
    private UUID pollId;
    private String caption;
    private int presentationOrder;
}