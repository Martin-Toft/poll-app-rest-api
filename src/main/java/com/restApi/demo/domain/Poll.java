package com.restApi.demo.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "polls")
public class Poll {

    @Id
    @GeneratedValue
    private UUID id;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false)
    private String question;

    private Instant publishedAt;
    private Instant validUntil;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("presentationOrder ASC")
    private List<VoteOption> options = new ArrayList<>();


    public VoteOption addVoteOption(String caption) {
        VoteOption o = new VoteOption();
        o.setCaption(caption);
        o.setPresentationOrder(options.size());
        o.setPoll(this);
        options.add(o);
        return o;
    }

    @JsonProperty("ownerId")
    @Transient
    public UUID getOwnerId() {
        return createdBy != null ? createdBy.getId() : null;
    }
}