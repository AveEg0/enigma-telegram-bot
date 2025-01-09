package org.karmazyn.dispatcher.service.iml;

import lombok.extern.slf4j.Slf4j;
import org.karmazyn.dispatcher.service.UpdateProducer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
public class UpdateProducerImpl implements UpdateProducer {

    private final RabbitTemplate rabbitTemplate;

    public UpdateProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void produce(String rabbitQueue, Update update) {
        log.info(update.getMessage().getText());
        rabbitTemplate.convertAndSend(rabbitQueue, update);
    }

}
