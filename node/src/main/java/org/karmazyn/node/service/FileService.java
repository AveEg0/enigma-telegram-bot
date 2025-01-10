package org.karmazyn.node.service;

import org.karmazyn.jpa.entity.AppDocument;
import org.karmazyn.jpa.entity.AppPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
}
