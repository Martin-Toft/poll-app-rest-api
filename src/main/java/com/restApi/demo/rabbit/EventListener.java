package com.restApi.demo.rabbit;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.restApi.demo.domain.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

@Component
public class EventListener {

    private final ObjectMapper om = new ObjectMapper();
    private final DomainManager domain;

    public EventListener(DomainManager domain) {
        this.domain = domain;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onVote(String json) throws Exception {
        VoteEvent ev = om.readValue(json, VoteEvent.class);

        Optional<Poll> pollOpt = domain.getPoll(ev.pollId);
        if (pollOpt.isEmpty()){
            System.out.println("Poll not found: " + ev.pollId);
            return;
        }
        var userId = ev.userId;
        if (userId == null || domain.getUser(userId).isEmpty()) {
            User u = domain.createUser("anon-" + java.util.UUID.randomUUID().toString().substring(0,8), "anon@example.com");

            userId = u.getId();
        }

        domain.castOrChangeVote(ev.pollId, userId, ev.optionId);


        System.out.println("poll = " + ev.pollId + " optionId = " + ev.optionId + " user = " + ev.userId);
    }

}
