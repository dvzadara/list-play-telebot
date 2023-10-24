package com.example.telegramListPlay.youtubeService;

import com.example.telegramListPlay.telegramBot.AudiosSenderInterface;
import com.example.telegramListPlay.Exceptions.PlaylistDownloadingException;
import com.example.telegramListPlay.Exceptions.VideoDownloadingException;
import com.github.kiulian.downloader.model.playlist.PlaylistInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes telegram user requests and sends responce using playlistSender
 */
@Service
public class YoutubeService {
    YoutubeClient youtubeClient;

    @Autowired
    public YoutubeService(YoutubeClient youtubeClient) {
        this.youtubeClient = youtubeClient;
    }

    public String linkToVideoId(String link) {
        try {
            URL url = new URL(link);
            if (url.getHost().endsWith("youtu.be")) {
                return link.substring(link.lastIndexOf("/") + 1, link.indexOf("?"));
            }

            String query = url.getQuery();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                String key = keyValue[0];
                String value = keyValue[1];
                if (key.equals("v")) {
                    return value;
                }
            }
            return null;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public String linkToPlaylistId(String link) {
        try {
            URL url = new URL(link);
            String query = url.getQuery();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                String key = keyValue[0];
                String value = keyValue[1];
                if (key.equals("list")) {
                    return value;
                }
            }
            return null;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public boolean isYoutubeVideoId(String text) {
        String regex = "^[a-zA-Z0-9_-]{11}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

    public boolean isYoutubePlaylistId(String text) {
        String regex = "^[a-zA-Z0-9_-]{34}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

    public boolean isYouTubeVideoLink(String link) {
        try {
            URL url = new URL(link);
            String host = url.getHost();
            String path = url.getPath();
            String query = url.getQuery();
            boolean isUrlType1 = host.endsWith("youtube.com") && query != null && query.contains("v=");
            boolean isUrlType2 = host.endsWith("youtu.be") && path.length() > 1 && query != null;
            return isUrlType1 || isUrlType2;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public boolean isYouTubePlaylistLink(String link) {
        try {
            URL url = new URL(link);
            String host = url.getHost();
            String query = url.getQuery();
            return host.endsWith("youtube.com") && query != null && query.contains("list=");
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public boolean isExistingYouTubeVideoId(String videoId) {
        if (!isYoutubeVideoId(videoId)) {
            System.out.println("It is not videoId.");
            return false;
        }
        if (youtubeClient.isExistingVideo(videoId))
            return true;
        System.out.println("video with id " + videoId + " not found");
        return false;
    }

    public boolean isExistingYouTubePlaylistId(String playlistId) {
        if (isYoutubePlaylistId(playlistId)) {
            System.out.println("It is not videoId.");
            return false;
        }
        if (youtubeClient.isExistingPlaylist(playlistId)) {
            return true;
        } else {
            System.out.println("video with id " + playlistId + "not found");
            return false;
        }
    }

    public boolean isExistingYouTubeVideoLink(String link) {
        if (!isYouTubeVideoLink(link)) {
            System.out.println(link + " is uncorrect.");
            return false;
        }
        String videoId = linkToVideoId(link);
        if (videoId == null) {
            System.out.println("videoId not found.");
            return false;
        }
        if (youtubeClient.isExistingVideo(videoId)) {
            return true;
        } else {
            System.out.println("video with id " + videoId + "not found");
            return false;
        }
    }

    public boolean isExistingYouTubePlaylistLink(String link) {
        if (!isYouTubePlaylistLink(link)) {
            System.out.println(link + " is uncorrect.");
            return false;
        }
        String playlistId = linkToPlaylistId(link);
        if (playlistId == null) {
            System.out.println("videoId not found.");
            return false;
        }
        if (youtubeClient.isExistingPlaylist(playlistId)) {
            return true;
        } else {
            System.out.println("video with id " + playlistId + "not found");
            return false;
        }
    }

    public void getYoutubeVideo(Long chatId, String videoId, AudiosSenderInterface playlistSender) {
        File file = null;
        File mp3File = null;
        try {
            if (!isExistingYouTubeVideoId(videoId))
                throw new VideoDownloadingException(
                        "Не вдалося знайти відео з таким ID, можливо його не існує або посилання було вказано неправильно.");
            file = youtubeClient.downloadAudio(videoId);
            try {
                mp3File = ToMp3Converter.mediaFileToMp3(file);
            } catch (IOException e) {
                throw new VideoDownloadingException("Не вдалося конвертувати відео у потрібний формат.", e);
            }
            playlistSender.sendAudioMessage(chatId, mp3File);
        } catch (VideoDownloadingException e) {
            playlistSender.sendVideoDownloadingError(chatId, e);
        } finally {
            if (file != null)
                file.delete();
            if (mp3File != null)
                mp3File.delete();
        }
    }

    public void getYoutubePlaylist(Long chatId, String playlistId, AudiosSenderInterface playlistSender) {
        playlistSender.sendStartDownloadingMessage(chatId);
        try {
            PlaylistInfo playlistInfo = youtubeClient.getPlaylistInfo(playlistId);
            int videoCount = playlistInfo.videos().size();
            for(int i = 0; i < videoCount; i++){
                getYoutubeVideo(chatId, playlistInfo.videos().get(i).videoId(), playlistSender);
            }
        } catch (PlaylistDownloadingException e) {
            playlistSender.sendVideoDownloadingError(chatId, e);
        }
        playlistSender.sendEndDownloadingMessage(chatId);
    }
}
