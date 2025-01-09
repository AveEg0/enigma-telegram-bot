package org.karmazyn.node.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.karmazyn.jpa.dao.AppUserDao;
import org.karmazyn.jpa.entity.AppUser;
import org.karmazyn.jpa.entity.enums.UserState;
import org.karmazyn.node.dao.RawDataDao;
import org.karmazyn.node.entity.RawData;
import org.karmazyn.node.service.MainService;
import org.karmazyn.node.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Objects;

import static org.karmazyn.jpa.entity.enums.UserState.BASIC_STATE;
import static org.karmazyn.jpa.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static org.karmazyn.node.service.enums.ServiceCommands.*;

@Service
@Slf4j
public class MainServiceImpl implements MainService {
   private final RawDataDao rawDataDao;
   private final AppUserDao appUserDao;
   private final ProducerService producerService;

    public MainServiceImpl(RawDataDao rawDataDao, AppUserDao appUserDao, ProducerService producerService) {
        this.rawDataDao = rawDataDao;
        this.appUserDao = appUserDao;
        this.producerService = producerService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        if (CANCEL.equals(text)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            //TODO add registration
        } else {
            log.error("Unknown state: {}", userState);
            output = "Unknown error! Enter '/cancel' and try again.";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);


    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowedToSendContent(chatId, appUser)){
            return;
        }

        //TODO add doc saving
        var answer = "Document saved successfully! Url for uploading : http://test.com/get-doc/666";
        sendAnswer(answer, chatId);
    }



    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowedToSendContent(chatId, appUser)){
            return;
        }

        //TODO add doc saving
        var answer = "Photo saved successfully! Url for uploading : http://test.com/get-photo/666";
        sendAnswer(answer, chatId);
    }
    private boolean isNotAllowedToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();
        if (!appUser.getIsActive()) {
            var error = "Register or activate your account first!";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Cancel current command for sending files";
            sendAnswer(error, chatId);
            return true;
        }

        return false;
    }
    private void sendAnswer(String output, Long chatId) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
       if (REGISTRATION.equals(cmd)) {
           //TODO add registration
           return "Temporary unavailable";
       } else if (HELP.equals(cmd)) {
           return help();
       } else if (START.equals(cmd)) {
       return "Enter '/help' to view all available commands.";
       } else {
           return "Unknown command! Enter '/help' to view all available commands.";
       }
    }

    private String help() {
        return "List of all available commands\n"
                + "/cancel - cancel current command\n"
                + "/registration - register new user\n";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDao.save(appUser);
        return "Command cancelled!";
    }

    private AppUser findOrSaveAppUser(Update update) {
        var telegramUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserDao.findAppUsersByTelegramUserId(telegramUser.getId());
        if (Objects.isNull(persistentAppUser)) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO change isActive status after adding registration
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDao.save(transientAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDao.save(rawData);
    }
}
