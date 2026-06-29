package com.example.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// #RabbitMQConfig

@Configuration
public class RabbitMQConfig {

    @Value("${order.rabbitmq.exchange}")
    private String exchange;

    @Value("${order.rabbitmq.queue}")
    private String queue;

    @Value("${order.rabbitmq.routingkey}")
    private String routingKey;

    @Bean
    public Queue orderQueue() {
        return new Queue(queue, true);
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Binding binding(Queue orderQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
