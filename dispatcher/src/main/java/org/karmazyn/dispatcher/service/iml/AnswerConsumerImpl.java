package org.karmazyn.dispatcher.service.iml;

import lombok.extern.slf4j.Slf4j;
import org.karmazyn.dispatcher.controller.UpdateController;
import org.karmazyn.dispatcher.service.AnswerConsumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import static org.karmazyn.model.RabbitQueue.*;

@Service
@Slf4j
public class AnswerConsumerImpl implements AnswerConsumer {
    private final UpdateController updateController;

    public AnswerConsumerImpl(UpdateController updateController) {
        this.updateController = updateController;
    }

    @Override
    @RabbitListener(queues = ANSWER_MESSAGE)
    public void consume(SendMessage sendMessage) {
        log.info("Received a send message: {}", sendMessage.getText());
        updateController.setView(sendMessage);
    }
}
