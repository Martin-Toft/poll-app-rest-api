package com.restApi.demo.controllers;

import com.restApi.demo.domain.DomainManager;
import com.restApi.demo.domain.Vote;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/polls/{pollId}")
public class VoteController {

  @Data
  public static class VoteReq {
    private String userId;
    private String optionId;
  }

  private final DomainManager domainManager;

  public VoteController(DomainManager domainManager){
    this.domainManager = domainManager;
  }

  @PostMapping("/votes")
  public Vote upsert(@PathVariable UUID pollId, @RequestBody VoteReq request) {
    return domainManager.castOrChangeVote(pollId,UUID.fromString(request.getUserId()),UUID.fromString(request.getOptionId()));
  }

  @GetMapping("/votes")
  public List<Vote> list(@PathVariable UUID pollId) {
    return domainManager.listMostRecentVotes(pollId);
  }
}