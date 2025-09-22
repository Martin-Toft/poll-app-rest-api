package com.restApi.demo.controllers;

import com.restApi.demo.domain.*;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public PollController(DomainManager domainManager){
        this.domainManager = domainManager;
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
        return domainManager.createPoll(
            owner,
            request.getQuestion(),
            request.getPublishedAt(),
            request.getValidUntil(),
            options
        );
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
}
