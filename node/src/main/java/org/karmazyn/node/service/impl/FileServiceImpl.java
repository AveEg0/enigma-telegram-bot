package org.karmazyn.node.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.karmazyn.jpa.dao.AppDocumentDao;
import org.karmazyn.jpa.dao.AppPhotoDao;
import org.karmazyn.jpa.dao.BinaryContentDao;
import org.karmazyn.jpa.entity.AppDocument;
import org.karmazyn.jpa.entity.AppPhoto;
import org.karmazyn.jpa.entity.BinaryContent;
import org.karmazyn.node.exception.UploadFileException;
import org.karmazyn.node.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;


@Service
@Slf4j
public class FileServiceImpl implements FileService {
    @Value("${token}")
    private String token;
    @Value("${service.file_info.url}")
    private String fileInfoUrl;
    @Value("${service.file_storage.url}")
    private String fileStorageUrl;
    private final AppDocumentDao appDocumentDao;
    private final BinaryContentDao binaryContentDao;
    private final AppPhotoDao appPhotoDao;

    public FileServiceImpl(AppDocumentDao appDocumentDao, BinaryContentDao binaryContentDao, AppPhotoDao appPhotoDao) {
        this.appDocumentDao = appDocumentDao;
        this.binaryContentDao = binaryContentDao;
        this.appPhotoDao = appPhotoDao;
    }

    @Override
    public AppDocument processDoc(Message telegramMessage) {
        String fileId = telegramMessage.getDocument().getFileId();
        ResponseEntity<String> response = getTelegramFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistantBinaryContent(response);
            Document telegramDoc = telegramMessage.getDocument();
            AppDocument trasientAppDocument = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
            return appDocumentDao.save(trasientAppDocument);
        } else {
            log.error("Error downloading file, response: {}", response);
            throw new UploadFileException("Bad response from telegram service " + response);
        }
    }

    private BinaryContent getPersistantBinaryContent(ResponseEntity<String> response) {
        String filePath = getFilePath(response);
        byte[] fileInByte = downloadFile(filePath);
        var transientBinaryContent = BinaryContent.builder()
                .fileAsArrayOfBytes(fileInByte)
                .build();
        return binaryContentDao.save(transientBinaryContent);
    }

    private static String getFilePath(ResponseEntity<String> response) {
        JSONObject jsonObject = new JSONObject(response.getBody());
        return String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
    }

    @Override
    public AppPhoto processPhoto(Message telegramMessage) {
       //TODO add processing multiple photos
        PhotoSize telegramPhoto = telegramMessage.getPhoto().getFirst();
        String fileId = telegramPhoto.getFileId();
        ResponseEntity<String> response = getTelegramFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistantBinaryContent(response);
            AppPhoto transientAppPhoto = buildTransientAppPhoto(telegramPhoto, persistentBinaryContent);
            return appPhotoDao.save(transientAppPhoto);
        } else {
            log.error("Error downloading file, response: {}", response);
            throw new UploadFileException("Bad response from telegram service " + response);
        }
    }

    private AppPhoto buildTransientAppPhoto(PhotoSize telegramPhoto, BinaryContent persistentBinaryContent) {
        return AppPhoto.builder()
                .telegramFileId(telegramPhoto.getFileId())
                .binaryContent(persistentBinaryContent)
                .fileSize(telegramPhoto.getFileSize())
                .build();
    }

    private AppDocument buildTransientAppDoc(Document telegramDoc, BinaryContent persistentBinaryContent) {
        return AppDocument.builder()
                .telegramFileId(telegramDoc.getFileId())
                .docName(telegramDoc.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDoc.getMimeType())
                .fileSize(telegramDoc.getFileSize())
                .build();
    }

    private byte[] downloadFile(String filePath) {
        String fullPath = fileStorageUrl.replace("{token}", token)
                .replace("{filePath}", filePath);
        URL urlObj;
        try {
            urlObj = URI.create(fullPath).toURL();
        } catch (MalformedURLException e) {
            throw new UploadFileException(e);
        }

        try(InputStream is = urlObj.openStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192]; // 8 KB buffer size
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            return baos.toByteArray();
        } catch (IOException e) {
            throw new UploadFileException(urlObj.toExternalForm(), e);
        }
    }

    private ResponseEntity<String> getTelegramFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        try {
          return   restTemplate.exchange(fileInfoUrl, HttpMethod.GET, request, String.class, token, fileId);
        } catch (Throwable e) {
            log.error("Error getting file path", e);
            throw new UploadFileException(e);
        }
    }
}
