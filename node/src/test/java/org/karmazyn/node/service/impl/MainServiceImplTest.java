package org.karmazyn.node.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.karmazyn.node.dao.RawDataDao;
import org.karmazyn.node.entity.RawData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest
public class MainServiceImplTest {
    @Autowired
    private RawDataDao rawDataDao;

    @Test
    public void testSaveRawData() {
        Update update = new Update();
        Message message = new Message();
        message.setText("Test");
        update.setMessage(message);

        RawData rawData = RawData.builder()
                .event(update)
                .build();
        Set<RawData> testData = new HashSet<>();
        testData.add(rawData);
        rawDataDao.save(rawData);
        Assertions.assertTrue(testData.contains(rawData), "Entity not found in the set");
    }
}
