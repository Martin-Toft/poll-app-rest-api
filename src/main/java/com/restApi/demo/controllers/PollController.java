package com.restApi.demo.controllers;

import com.restApi.demo.domain.DomainManager;
import com.restApi.demo.domain.Poll;
import com.restApi.demo.domain.VoteOption;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/polls")
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
  public Poll create(@RequestBody CreatePollRequest request) {
    UUID owner = UUID.fromString(request.getOwnerId());
    var options = new ArrayList<VoteOption>();
    for (CreatePollOption option : request.getOptions()) {
      options.add(new VoteOption(null, null, option.getCaption(), option.getPresentationOrder()));
    }
    return domainManager.createPoll(owner, request.getQuestion(), request.getPublishedAt(), request.getValidUntil(), options);
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