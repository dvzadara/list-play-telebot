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
                Ласкаво просимо!
                
                Ютуб не дає слухати ваш плейлист з музикою або подкаст, коли ви вимкнули телефон?
                
                Цей бот дозволяє завантажити відео або плейлист з Youtube в форматі .mp3 після чого ви можете використовувати аудіо.
                
                Введіть посилання на плейлист або відео, які ви хочете перетворити на .mp3(Приклад: https://www.youtube.com/watch?v=dQw4w9WgXcQ).
                """;
        sendMessage(chatId, text);
    }

    /**
     * Sending a message that the request was not recognized.
     * @param chatId
     */
    private void sendUncorrectRequestMessage(Long chatId) {
        String text = """
                Не вдалося знайти посилання на відео або плейлист. Можливо в посиланні помилка.
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
            sendMessage(chatId, "Не вдалося відправити файл.");
        }
    }

    @Override
    public void sendStartDownloadingMessage(Long chatId) {
        String text = """
                Завантаження плейлиста розпочалося.
                """;
        sendMessage(chatId, text);
    }

    @Override
    public void sendEndDownloadingMessage(Long chatId) {
        String text = """
                Завантаження плейлиста завершено.
                """;
    }

    @Override
    public void sendVideoDownloadingError(Long chatId, Exception e) {
        logger.log(Level.SEVERE, "Send downloading error to user", e.getCause());
        sendMessage(chatId, e.getMessage());
    }
}
