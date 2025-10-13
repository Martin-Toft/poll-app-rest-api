package com.restApi.demo.controllers;

import com.restApi.demo.domain.*;
import com.restApi.demo.redis.*;

import lombok.Data;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.amqp.core.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/polls")
@CrossOrigin(
origins = { "http://localhost:5173", "http://127.0.0.1:5173" },
methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS },
allowedHeaders = "*"
)
public class PollController {

    @Data
    public static class CreatePollOption {
        private String caption;
        private int presentationOrder;
    }

    @Data
    public static class CreatePollRequest {
        private String ownerId;
        private String question;
        private Instant publishedAt;
        private Instant validUntil;
        private List<CreatePollOption> options;
    }

    private final DomainManager domainManager;

    private final AmqpAdmin amqpAdmin;
    private final TopicExchange pollsExchange;

    public PollController(DomainManager domainManager, AmqpAdmin amqpAdmin, TopicExchange pollsExchange){
        this.domainManager = domainManager;
        this.amqpAdmin = amqpAdmin;
        this.pollsExchange = pollsExchange;
    }

    @PostMapping
    @CrossOrigin(
        origins = { "http://localhost:5173", "http://127.0.0.1:5173" },
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS },
        allowedHeaders = "*"
    )
    public Poll create(@RequestBody CreatePollRequest request) {
        UUID owner = UUID.fromString(request.getOwnerId());
        var options = new ArrayList<VoteOption>();
        for (CreatePollOption option : request.getOptions()) {
            VoteOption vo = new VoteOption();
            vo.setCaption(option.getCaption());
            vo.setPresentationOrder(option.getPresentationOrder());
            options.add(vo);
        }
        Poll poll = domainManager.createPoll(
            owner,
            request.getQuestion(),
            request.getPublishedAt(),
            request.getValidUntil(),
            options
        );
        String qName = "poll." + poll.getId();
        Queue q = QueueBuilder.durable(qName).build();
        amqpAdmin.declareQueue(q);
        amqpAdmin.declareBinding(BindingBuilder.bind(q).to(pollsExchange).with("poll." + poll.getId() + ".*"));

        return poll;
    }

    @GetMapping
    public List<Poll> list(){
        return domainManager.listPolls();
    }

    @GetMapping("{id}")
    public Poll get(@PathVariable UUID id){
        return domainManager.getPoll(id).get();
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable UUID id){
        domainManager.deletePoll(id);
    }

    //redis
    @GetMapping("{id}/results")
    public ResponseEntity<Map<String, Long>> getResults(@PathVariable UUID id) {
        Map<String, Long> cached = VoteCountCache.get(id.toString());
        if (cached != null) {
            return ResponseEntity.ok().header("X_Cache", "HIT").body(cached);
        }
        var options = domainManager.listOptions(id);
        var votes = domainManager.listMostRecentVotes(id);

        Map<String, Long> counts = new LinkedHashMap<>();
        for (var option : options) {
            long c = votes.stream().filter(v -> v.getOptionId().equals(option.getId())).count();
            counts.put(option.getId().toString(), c);
        }
        
        VoteCountCache.put(id.toString(), counts);

        return ResponseEntity.ok().header("X-Cache", "MISS").body(counts);
    }


}
