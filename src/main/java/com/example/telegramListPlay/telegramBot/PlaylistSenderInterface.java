package com.example.telegramListPlay.telegramBot;

import java.io.File;

public interface PlaylistSenderInterface {
    void sendVideoMessage(Long chatId, File videoFile);

    void sendStartDownloadingMessage(Long chatId);

    void sendEndDownloadingMessage(Long chatId);

    void sendVideoDownloadingError(Long chatId, Exception e);
}
