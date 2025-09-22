package com.restApi.demo.domain;


import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DomainManager {

    // in-memory storing
    private final Map<UUID, User> users   = new ConcurrentHashMap<>();
    private final Map<UUID, Poll> polls   = new ConcurrentHashMap<>();
    private final Map<UUID, VoteOption> options = new ConcurrentHashMap<>();
    private final Map<UUID, Vote> votes   = new ConcurrentHashMap<>();

    //users
    public User createUser(String username, String email) {
        User u = new User(username, email);
        if (u.getId() == null) u.setId(UUID.randomUUID());
        users.put(u.getId(), u);
        return u;
        }

        public Optional<User> getUser(UUID id) {
            return Optional.ofNullable(users.get(id));
        }

        public List<User> listUsers() {
            return users.values().stream()
                .sorted(Comparator.comparing(User::getUsername, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
        }

    public User updateUser(UUID id, String username, String email) {
        User u = users.get(id);
        if (u == null) throw new NoSuchElementException("user not found: " + id);
        u.setUsername(username);
        u.setEmail(email);
        return u;
    }

    public void deleteUser(UUID id) {
        User u = users.remove(id);
        if (u == null) return;

        // remove polls created by user
        var toRemove = polls.values().stream()
            .filter(p -> p.getCreatedBy() != null && id.equals(p.getCreatedBy().getId()))
            .map(Poll::getId)
            .collect(Collectors.toList());
        toRemove.forEach(this::deletePoll);

        // remove votes cast by user
        var voteIds = votes.values().stream()
            .filter(v -> v.getCastBy() != null && id.equals(v.getCastBy().getId()))
            .map(Vote::getId)
            .collect(Collectors.toList());
        voteIds.forEach(votes::remove);
        }

    //polls
    public Poll createPoll(UUID ownerId, String question, Instant publishedAt, Instant validUntil, List<VoteOption> initialOptions) {
        User owner = users.get(ownerId);
        if (owner == null) throw new NoSuchElementException("owner not found: " + ownerId);

        Poll p = new Poll();
        p.setQuestion(question);
        p.setCreatedBy(owner);
        p.setPublishedAt(publishedAt);
        p.setValidUntil(validUntil);
        if (p.getId() == null) p.setId(UUID.randomUUID());
        if (p.getOptions() == null) p.setOptions(new ArrayList<>());

        polls.put(p.getId(), p);

        if (initialOptions != null) {
            int idx = 0;
            for (VoteOption in : initialOptions) {
                VoteOption o = new VoteOption();
                o.setCaption(in.getCaption());
                o.setPresentationOrder(idx++);
                o.setPoll(p);
                if (o.getId() == null) o.setId(UUID.randomUUID());
                options.put(o.getId(), o);
                p.getOptions().add(o);
            }
        }

        return p;
    }

    public Optional<Poll> getPoll(UUID id) {
        return Optional.ofNullable(polls.get(id));
    }

    public List<Poll> listPolls() {
        return polls.values().stream()
            .sorted(Comparator
                .comparing(Poll::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(p -> p.getId().toString()))
            .collect(Collectors.toList());
    }

    public void deletePoll(UUID pollId) {
        Poll p = polls.remove(pollId);
        if (p == null) return;

        // remove options
        var optIds = options.values().stream()
            .filter(o -> o.getPoll() != null && pollId.equals(o.getPoll().getId()))
            .map(VoteOption::getId)
            .collect(Collectors.toList());
        optIds.forEach(options::remove);

        // remove votes for this poll
        var voteIds = votes.values().stream()
            .filter(v -> v.getVotesOn() != null
                && v.getVotesOn().getPoll() != null
                && pollId.equals(v.getVotesOn().getPoll().getId()))
            .map(Vote::getId)
            .collect(Collectors.toList());
        voteIds.forEach(votes::remove);
        }


    public VoteOption addOption(UUID pollId, String caption, int presentationOrder) {
        Poll p = polls.get(pollId);
        if (p == null) throw new NoSuchElementException("poll not found: " + pollId);
        if (p.getOptions() == null) p.setOptions(new ArrayList<>());

        VoteOption o = new VoteOption();
        o.setCaption(caption);
        if (presentationOrder < 0) {
            presentationOrder = p.getOptions().size();
        }
        o.setPresentationOrder(presentationOrder);
        o.setPoll(p);

        if (o.getId() == null) o.setId(UUID.randomUUID());
        options.put(o.getId(), o);
        p.getOptions().add(o);
        p.getOptions().sort(Comparator.comparingInt(VoteOption::getPresentationOrder));

        return o;
    }

    public VoteOption updateOption(UUID pollId, UUID optionId, String caption, int presentationOrder) {
        Poll p = polls.get(pollId);
        VoteOption o = options.get(optionId);
        if (p == null) throw new NoSuchElementException("poll not found: " + pollId);
        if (o == null || o.getPoll() == null || !pollId.equals(o.getPoll().getId()))
        throw new NoSuchElementException("option not found in poll");

        o.setCaption(caption);
        o.setPresentationOrder(presentationOrder);
        if (p.getOptions() != null) {
            p.getOptions().sort(Comparator.comparingInt(VoteOption::getPresentationOrder));
        }
        return o;
    }

    public void deleteOption(UUID pollId, UUID optionId) {
        VoteOption o = options.get(optionId);
        if (o == null) return;
        if (o.getPoll() == null || !pollId.equals(o.getPoll().getId())) return;

        // remove option
        options.remove(optionId);
        Poll p = polls.get(pollId);
        if (p != null && p.getOptions() != null) {
            p.getOptions().removeIf(vo -> optionId.equals(vo.getId()));
        }

        // remove votes on this option
        var toDelete = votes.values().stream()
            .filter(v -> v.getVotesOn() != null && optionId.equals(v.getVotesOn().getId()))
            .map(Vote::getId)
            .collect(Collectors.toList());
        toDelete.forEach(votes::remove);
    }

    //votes
    public Vote castOrChangeVote(UUID pollId, UUID userId, UUID optionId) {
        User user = users.get(userId);
        VoteOption opt = options.get(optionId);
        if (user == null) throw new NoSuchElementException("user not found: " + userId);
        if (opt == null) throw new NoSuchElementException("option not found: " + optionId);
        if (opt.getPoll() == null || !pollId.equals(opt.getPoll().getId()))
        throw new IllegalArgumentException("option does not belong to poll");

        // remove existing vote by this user on this poll
        var existingIds = votes.values().stream()
            .filter(v ->
                v.getCastBy() != null && userId.equals(v.getCastBy().getId()) &&
                v.getVotesOn() != null && v.getVotesOn().getPoll() != null &&
                pollId.equals(v.getVotesOn().getPoll().getId()))
            .map(Vote::getId)
            .collect(Collectors.toList());
        existingIds.forEach(votes::remove);

        // create new
        Vote v = new Vote();
        v.setCastBy(user);
        v.setVotesOn(opt);
        v.setPublishedAt(Instant.now());
        if (v.getId() == null) v.setId(UUID.randomUUID());
        votes.put(v.getId(), v);
        return v;
    }


    public List<Vote> listMostRecentVotes(UUID pollId) {
        List<Vote> allForPoll = votes.values().stream()
            .filter(v -> v.getVotesOn() != null
                && v.getVotesOn().getPoll() != null
                && pollId.equals(v.getVotesOn().getPoll().getId()))
            .sorted(Comparator
                .comparing(Vote::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(v -> v.getId().toString()))
            .collect(Collectors.toList());


        Map<UUID, Vote> newestPerUser = new LinkedHashMap<>();
        for (Vote v : allForPoll) {
            UUID uid = (v.getCastBy() != null) ? v.getCastBy().getId() : null;
            if (uid == null) continue;
            newestPerUser.putIfAbsent(uid, v);
        }
        return new ArrayList<>(newestPerUser.values());
    }

    public List<VoteOption> listOptions(UUID pollId) {
        Poll p = polls.get(pollId);
        if (p == null || p.getOptions() == null) return List.of();
            return p.getOptions().stream()
            .sorted(Comparator.comparingInt(VoteOption::getPresentationOrder))
            .collect(Collectors.toList());

    }
}