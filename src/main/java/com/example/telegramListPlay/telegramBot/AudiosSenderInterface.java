package com.example.telegramListPlay.telegramBot;

import java.io.File;

/**
 * Created for use by services to send messages to the user.
 */
public interface AudiosSenderInterface {
    void sendAudioMessage(Long chatId, File videoFile);

    void sendStartDownloadingMessage(Long chatId);

    void sendEndDownloadingMessage(Long chatId);

    void sendVideoDownloadingError(Long chatId, Exception e);
}
