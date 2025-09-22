package com.restApi.demo.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;
import com.fasterxml.jackson.annotation.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "votes")
public class Vote {

    @Id
    @GeneratedValue
    private UUID id;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private VoteOption votesOn;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cast_by_id")
    private User castBy;

    @Column(nullable = false)
    private Instant publishedAt = Instant.now();

    @JsonProperty("pollId")
    @Transient
    public UUID getPollId() {
        return (votesOn != null && votesOn.getPoll() != null) ? votesOn.getPoll().getId() : null;
    }

    @JsonProperty("userId")
    @Transient
    public UUID getUserId() {
        return (castBy != null) ? castBy.getId() : null;
    }

    @JsonProperty("optionId")
    @Transient
    public UUID getOptionId() {
        return (votesOn != null) ? votesOn.getId() : null;
    }
}