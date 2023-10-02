package com.example.telegramListPlay.telegramBot;

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
import java.util.logging.*;

/**
 * Сlass for processing messages from the Telegram user, and with functions for sending messages.
 */
@Component
public class TelegramBot extends TelegramLongPollingBot implements AudiosSenderInterface {
    private YoutubeService youtubeService;
    private static final String START = "/start";
    private static final String GET_VIDEO = "/video";
    private static final Logger logger = Logger.getLogger(TelegramBot.class.getName());

    @Autowired
    public TelegramBot(@Value("${bot.token}") String botToken, YoutubeService youtubeService) {
        super(botToken);
        this.youtubeService = youtubeService;
    }

    /**
     * Processes requests from telegram user.
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        String message = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();

        if (message.equals("/start"))
            sendWelcomeMessage(chatId);
        else if (youtubeService.isExistingYouTubePlaylistLink(message)) {
            String playlistId = youtubeService.linkToPlaylistId(message);
            youtubeService.getYoutubePlaylist(chatId, playlistId, this);
        } else if (youtubeService.isExistingYouTubeVideoLink(message)) {
            String videoId = youtubeService.linkToVideoId(message);
            youtubeService.getYoutubeVideo(chatId, videoId, this);
        } else {
            sendUncorrectRequestMessage(chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return "listPlay";
    }

    /**
     * Send text to telegram user.
     * @param chatId
     * @param text Message text.
     */
    public void sendMessage(Long chatId, String text) {
        String chatIdStr = String.valueOf(chatId);
        SendMessage sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Send welcome message to user.
     * @param chatId
     */
    private void sendWelcomeMessage(Long chatId) {
        String text = """
                Добро пожаловать!
                                
                Ютуб не дает слушать ваш плейлист с музыкой или подкаст когда вы выключили телефон?(
                                
                Данный бот позволяет скачать видео или плейлист из Youtube в .mp3 формате после чего вы можете использовать аудио.
                                
                Введите ссылку на плейлист или видео которые вы хотите преобразовать в .mp3.
                """;
        sendMessage(chatId, text);
    }

    /**
     * Sending a message that the request was not recognized.
     * @param chatId
     */
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

    @Override
    public void sendAudioMessage(Long chatId, File videoFile) {
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
        logger.log(Level.SEVERE, "Send downloading error to user", e.getStackTrace());
        sendMessage(chatId, e.getMessage());
    }
}
