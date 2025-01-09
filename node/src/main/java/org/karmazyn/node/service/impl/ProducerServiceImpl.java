package org.karmazyn.node.service.impl;

import org.karmazyn.node.service.ProducerService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import static org.karmazyn.model.RabbitQueue.*;
@Service
public class ProducerServiceImpl implements ProducerService {
    private final RabbitTemplate rabbitTemplate;

    public ProducerServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    @Override
    public void produceAnswer(SendMessage message) {
        rabbitTemplate.convertAndSend(ANSWER_MESSAGE, message);
    }
}
