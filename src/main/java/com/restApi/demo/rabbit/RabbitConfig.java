package com.restApi.demo.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "polls";
    public static final String QUEUE = "polls.demo";

    @Bean
    TopicExchange pollsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    Queue demoQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding votesBinding(Queue demoQueue, TopicExchange pollsExchange) {
        return BindingBuilder.bind(demoQueue).to(pollsExchange).with("poll.*.vote");
    }

}
