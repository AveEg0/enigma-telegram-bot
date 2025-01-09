package org.karmazyn.node.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;


public interface MainService {
    void processTextMessage(Update update);

    void processDocMessage(Update update);

    void processPhotoMessage(Update update);
}
