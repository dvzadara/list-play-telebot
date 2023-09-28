package com.example.telegramListPlay.youtubeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class YoutubeService {
    YoutubeClient youtubeClient;

    @Autowired
    public YoutubeService(YoutubeClient youtubeClient) {
        this.youtubeClient = youtubeClient;
    }

    public File getYoutubeVideo(String videoId) throws VideoDownloadingException {
        return youtubeClient.downloadAudio(videoId);
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
            System.out.println("video with id " + videoId + "not found");
            return true;
        } else
            return false;
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
            System.out.println("video with id " + playlistId + "not found");
            return true;
        } else
            return false;
    }

    public String linkToPlaylistId(String link)  {
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

    public boolean isYoutubeVideoId(String text) {
        String regex = "^[a-zA-Z0-9_-]{11}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

    public boolean isExistingYouTubeVideoId(String videoId) {
        if (isYoutubeVideoId(videoId)) {
            System.out.println("It is not videoId.");
            return false;
        }
        if (youtubeClient.isExistingVideo(videoId)) {
            System.out.println("video with id " + videoId + "not found");
            return true;
        } else
            return false;
    }

    public boolean isYoutubePlaylistId(String text) {
        String regex = "^[a-zA-Z0-9_-]{34}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }
}
