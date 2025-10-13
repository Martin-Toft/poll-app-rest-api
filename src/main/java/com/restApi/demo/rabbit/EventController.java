package com.restApi.demo.rabbit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/events")
@CrossOrigin
public class EventController {
    
    private final RabbitTemplate template;

    public EventController(RabbitTemplate template) {
        this.template = template;
    }


    @PostMapping("/vote")
    public String publishVote(@RequestBody VoteEvent ev) {
        String routingKey = "poll." + ev.pollId + ".vote";
        try {
            String json = new ObjectMapper().writeValueAsString(ev);
            template.convertAndSend(RabbitConfig.EXCHANGE, routingKey, json);
            return "sent";
        }catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
