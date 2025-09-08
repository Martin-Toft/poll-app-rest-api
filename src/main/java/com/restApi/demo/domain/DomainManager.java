package com.restApi.demo.domain;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DomainManager {

    private final Map<UUID, User> users = new ConcurrentHashMap<>();
    private final Map<UUID, Poll> polls = new ConcurrentHashMap<>();
    private final Map<UUID, VoteOption> options = new ConcurrentHashMap<>();
    private final Map<String, Vote> latestVoteByUserInPoll = new ConcurrentHashMap<>();

    private static String key(UUID pollId, UUID userId){
        return pollId + ":" + userId;
    }

    // CRUD operations for users
    public User createUser(String username, String email){
        var user = new User(UUID.randomUUID(), username, email);
        users.put(user.getId(), user);
        return user;
    }

    public List<User> listUsers() {
        return List.copyOf(users.values());
    }
    public Optional<User> getUser(UUID id){
        return Optional.ofNullable(users.get(id));
    }

    public User updateUser(UUID id, String username, String email) {
        var user = users.get(id);
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }

    public void deleteUser(UUID id) { users.remove(id); }

    // CRUD operations for polls
    public Poll createPoll(UUID ownerId, String question, Instant publishedAt, Instant validUntil, List<VoteOption> incomingOptions) {
        var poll = new Poll(UUID.randomUUID(), ownerId, question, publishedAt, validUntil, new ArrayList<>());
        for (VoteOption option : incomingOptions) {
            var opt = new VoteOption(UUID.randomUUID(), poll.getId(), option.getCaption(), option.getPresentationOrder());
            options.put(opt.getId(), opt);
            poll.getOptions().add(opt);
        }
        polls.put(poll.getId(), poll);
        return poll;
    }

    public List<Poll> listPolls(){
        return List.copyOf(polls.values());
    }
    public Optional<Poll> getPoll(UUID id){
        return Optional.ofNullable(polls.get(id));
    }

    public void deletePoll(UUID id) {
        var poll = polls.remove(id);
        if (poll == null) return;

        // remove all options belonging to this poll
        if (poll.getOptions() != null) {
            for (var option : poll.getOptions()) {
                options.remove(option.getId());
            }
        }
        //remove latest vote by all users on this poll
        latestVoteByUserInPoll.keySet().removeIf(k -> k.startsWith(id.toString() + ":"));
    }

    //CRUD operations for vote options
    public List<VoteOption> listOptions(UUID pollId) {
        return polls.get(pollId).getOptions();
    }

    public VoteOption addOption(UUID pollId, String caption, int order) {
        var poll = polls.get(pollId);
        var opt = new VoteOption(UUID.randomUUID(), pollId, caption, order);
        options.put(opt.getId(), opt);
        poll.getOptions().add(opt);
        return opt;
    }

    public VoteOption updateOption(UUID pollId, UUID optionId, String caption, int order) {
        var option = options.get(optionId);
        option.setCaption(caption);
        option.setPresentationOrder(order);
        return option;
    }

    public void deleteOption(UUID pollId, UUID optionId) {
        options.remove(optionId);
        polls.get(pollId).getOptions().removeIf(o -> o.getId().equals(optionId));
    }

    // CRUD operations for votes
    public Vote castOrChangeVote(UUID pollId, UUID userId, UUID optionId) {
        var k = key(pollId, userId);
        var now = Instant.now();
        var vote = latestVoteByUserInPoll.getOrDefault(k, new Vote(UUID.randomUUID(), pollId, userId, optionId, now));
        vote.setOptionId(optionId);
        vote.setPublishedAt(now);
        latestVoteByUserInPoll.put(k, vote);
        return vote;
    }

    public List<Vote> listMostRecentVotes(UUID pollId) {
        List<Vote> result = new ArrayList<>();
        for (Vote vote : latestVoteByUserInPoll.values()) {
            if (vote.getPollId().equals(pollId)) {
                result.add(vote);
            }
        }
        return result;
    }
}