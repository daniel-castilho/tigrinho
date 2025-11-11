package com.tigrinho.slot.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for RabbitMQ.
 * This class sets up the necessary beans for RabbitMQ messaging,
 * including queues, exchanges, bindings, and message converters.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.name}")
    private String queue;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    /**
     * Configures and provides the RabbitMQ Queue bean.
     * The queue name is retrieved from application properties.
     *
     * @return A {@link Queue} instance.
     */
    @Bean
    public Queue queue() {
        return new Queue(queue);
    }

    /**
     * Configures and provides the RabbitMQ TopicExchange bean.
     * The exchange name is retrieved from application properties.
     *
     * @return A {@link TopicExchange} instance.
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    /**
     * Configures and provides the RabbitMQ Binding bean.
     * This binds the queue to the exchange with a specific routing key.
     *
     * @param queue The {@link Queue} bean to bind.
     * @param exchange The {@link TopicExchange} bean to bind to.
     * @return A {@link Binding} instance.
     */
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(routingKey);
    }

    /**
     * Configures and provides a {@link MessageConverter} for RabbitMQ.
     * Uses {@link Jackson2JsonMessageConverter} to serialize and deserialize
     * messages as JSON.
     *
     * @return A {@link MessageConverter} instance.
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
