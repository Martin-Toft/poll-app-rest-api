package com.restApi.demo.controllers;

import com.restApi.demo.domain.*;

import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/polls/{pollId}/options")
@CrossOrigin(
    origins = { "http://localhost:5173", "http://127.0.0.1:5173" },
    methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS },
    allowedHeaders = "*"
)
public class VoteOptionController {

    @Data
    public static class OptionReq {
        private String caption;
        private Integer presentationOrder;
    }

    private final DomainManager domainManager;

    public VoteOptionController(DomainManager domainManager){
        this.domainManager = domainManager;
    }

    @GetMapping
    public List<VoteOption> list(@PathVariable UUID pollId){
        return domainManager.listOptions(pollId);
    }

    @PostMapping
    public VoteOption add(@PathVariable UUID pollId, @RequestBody OptionReq request) {
        return domainManager.addOption(pollId, request.getCaption(), request.getPresentationOrder());
    }

    @PutMapping("{optionId}")
    public VoteOption update(@PathVariable UUID pollId, @PathVariable UUID optionId, @RequestBody OptionReq request) {
        return domainManager.updateOption(pollId, optionId, request.getCaption(), request.getPresentationOrder());
    }

    @DeleteMapping("{optionId}")
    public void delete(@PathVariable UUID pollId, @PathVariable UUID optionId) {
        domainManager.deleteOption(pollId, optionId);
    }
}