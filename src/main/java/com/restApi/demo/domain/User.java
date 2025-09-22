package com.restApi.demo.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public Poll createPoll(String question) {
        Poll p = new Poll();
        p.setQuestion(question);
        p.setCreatedBy(this);
        return p;
    }

    public Vote voteFor(VoteOption option) {
        Vote v = new Vote();
        v.setVotesOn(option);
        v.setCastBy(this);
        return v;
    }
}