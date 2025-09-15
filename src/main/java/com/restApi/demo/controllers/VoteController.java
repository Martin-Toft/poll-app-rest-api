package com.restApi.demo.controllers;

import com.restApi.demo.domain.DomainManager;
import com.restApi.demo.domain.Vote;
import com.restApi.demo.domain.VoteOption;

import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/polls/{pollId}")
@CrossOrigin(
  origins = { "http://localhost:5173", "http://127.0.0.1:5173" },
  methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS },
  allowedHeaders = "*"
)
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
  
  // Return a readable vote object
  private Map<String, Object> toView(Vote v) {
    var poll = domainManager.getPoll(v.getPollId()).get();
    var user = domainManager.getUser(v.getUserId()).get();
    VoteOption option = domainManager.listOptions(poll.getId()).stream().filter(o -> o.getId().equals(v.getOptionId())).findFirst().get();

    return Map.of(
        "id", v.getId(),
        "poll", Map.of("id", poll.getId(),"question", poll.getQuestion()),
        "user", Map.of("id", user.getId(),"username", user.getUsername()),
        "option", Map.of("id", option.getId(),"caption", option.getCaption()),
        "publishedAt", v.getPublishedAt()
    );
  }

  @PostMapping("/votes")
  public Map<String, Object> upsert(@PathVariable UUID pollId, @RequestBody VoteReq req) {
    var v = domainManager.castOrChangeVote(pollId, UUID.fromString(req.getUserId()), UUID.fromString(req.getOptionId()));
    return toView(v);
  }

  @GetMapping("/votes")
  public List<Map<String, Object>> list(@PathVariable UUID pollId) {
    return domainManager.listMostRecentVotes(pollId).stream().map(this::toView).collect(Collectors.toList());
  }
}