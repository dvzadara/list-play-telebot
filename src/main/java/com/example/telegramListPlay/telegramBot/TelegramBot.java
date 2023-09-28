package com.example.telegramListPlay.telegramBot;

import com.example.telegramListPlay.Exceptions.VideoDownloadingException;
import com.example.telegramListPlay.youtubeService.YoutubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

@Component
public class TelegramBot extends TelegramLongPollingBot implements PlaylistSenderInterface {
    private YoutubeService youtubeService;
    private static final String START = "/start";
    private static final String GET_VIDEO = "/video";

    @Autowired
    public TelegramBot(@Value("${bot.token}") String botToken, YoutubeService youtubeService) {
        super(botToken);
        this.youtubeService = youtubeService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        String message = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();

        if (message.equals("/start"))
            sendStartMessage(chatId);
        else if (youtubeService.isExistingYouTubePlaylistLink(message)) {
            String playlistId = youtubeService.linkToPlaylistId(message);
            youtubeService.getYoutubePlaylist(chatId, playlistId, this);
        } else if (youtubeService.isExistingYouTubeVideoLink(message)) {
            String videoId = youtubeService.linkToVideoId(message);
            sendVideoMessage(chatId, videoId);
        } else {
            sendUncorrectRequestMessage(chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return "listPlay";
    }

    public void sendMessage(Long chatId, String text) {
        String chatIdStr = String.valueOf(chatId);
        SendMessage sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    private void sendStartMessage(Long chatId) {
        String text = """
                Добро пожаловать!
                                
                Ютуб не дает слушать ваш плейлист с музыкой или подкаст когда вы выключили телефон?(
                                
                Данный бот позволяет скачать видео или плейлист из Youtube в .mp3 формате после чего вы можете использовать аудио.
                                
                Введите ссылку на плейлист или видео которые вы хотите преобразовать в .mp3.
                """;
        sendMessage(chatId, text);
    }

    private void sendUncorrectRequestMessage(Long chatId) {
        String text = """
                Не удалось найти ссылку или id ютуб видео или плейлиста. Возможно в ссылке ошибка.
                """;
        sendMessage(chatId, text);
    }

    private void sendMessageWithFile(Long chatId, File save) throws TelegramApiException {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(new InputFile(save));
        execute(sendDocument);
    }

    private void sendVideoMessage(Long chatId, String videoId) {
        try {
            File videoFile = youtubeService.getYoutubeVideo(videoId);
            try {
                sendMessageWithFile(chatId, videoFile);
            } catch (TelegramApiException e) {
                sendMessage(chatId, "Не удалось отправить файл.");
            } finally {
                videoFile.delete();
            }
        } catch (VideoDownloadingException e) {
            sendMessage(chatId, e.getMessage());
        }
    }

    @Override
    public void sendVideoMessage(Long chatId, File videoFile) {
        try {
            sendMessageWithFile(chatId, videoFile);
        } catch (TelegramApiException e) {
            sendMessage(chatId, "Не удалось отправить файл.");
        }
    }

    @Override
    public void sendStartDownloadingMessage(Long chatId) {
        String text = """
                Загрузка плейлиста началась.
                """;
        sendMessage(chatId, text);
    }

    @Override
    public void sendEndDownloadingMessage(Long chatId) {
        String text = """
                Загрузка плейлиста завершена.
                """;
    }

    @Override
    public void sendVideoDownloadingError(Long chatId, Exception e) {
        sendMessage(chatId, e.getMessage());
    }
}
