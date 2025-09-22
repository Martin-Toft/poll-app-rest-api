package com.restApi.demo.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "vote_options")
public class VoteOption {

    @Id
    @GeneratedValue
    private UUID id;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id")
    private Poll poll;

    @Column(nullable = false)
    private String caption;

    @Column(nullable = false)
    private int presentationOrder;

    @JsonProperty("pollId")
    @Transient
    public UUID getPollId() {
        return poll != null ? poll.getId() : null;
    }
}