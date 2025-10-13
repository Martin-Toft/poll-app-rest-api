package com.restApi.demo.rabbit;

import java.util.UUID;

public class VoteEvent {
    
    public UUID pollId;
    public UUID optionId;
    public UUID userId;

    public VoteEvent() {}
    public VoteEvent(UUID pollId, UUID optionId, UUID userId) {
        this.pollId = pollId;
        this.optionId = optionId;
        this.userId = userId;
    }

}
