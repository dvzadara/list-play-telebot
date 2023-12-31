package com.example.telegramListPlay.youtubeService;

import com.example.telegramListPlay.Exceptions.PlaylistDownloadingException;
import com.example.telegramListPlay.Exceptions.VideoDownloadingException;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestPlaylistInfo;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.playlist.PlaylistInfo;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.Format;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class YoutubeClient {
    private final YoutubeDownloader downloader;
    public YoutubeClient(){
        this.downloader = new YoutubeDownloader();
    }

    public File downloadAudio(String videoId) throws VideoDownloadingException {
        RequestVideoInfo infoRequest = new RequestVideoInfo(videoId);
        Response<VideoInfo> infoResponse = downloader.getVideoInfo(infoRequest);
        if (!infoResponse.ok())
            throw new VideoDownloadingException("Не удалось скачать видео по ссылке.");
        VideoInfo videoInfo = infoResponse.data();
        Format format = videoInfo.bestAudioFormat();
        String name = videoInfo.details().title();
        RequestVideoFileDownload audioRequest = new RequestVideoFileDownload(format)
                .renameTo(name)
                .overwriteIfExists(true)
                .saveTo(new File("tmp/"));
        Response<File> response = downloader.downloadVideoFile(audioRequest);
        if (!response.ok())
            throw new VideoDownloadingException("Не удалось скачать видео по ссылке.");
        if (!response.data().exists())
            throw new VideoDownloadingException("Не удалось создать файл.");
        return response.data();
    }

    public boolean isExistingVideo(String videoId) {
        RequestVideoInfo infoRequest = new RequestVideoInfo(videoId);
        Response<VideoInfo> infoResponse = downloader.getVideoInfo(infoRequest);
        return infoResponse.ok();
    }

    public boolean isExistingPlaylist(String playlistId) {
        RequestPlaylistInfo infoRequest = new RequestPlaylistInfo(playlistId);
        Response<PlaylistInfo> infoResponse = downloader.getPlaylistInfo(infoRequest);
        return infoResponse.ok();
    }

    public PlaylistInfo getPlaylistInfo(String playlistId) throws PlaylistDownloadingException {
        RequestPlaylistInfo infoRequest = new RequestPlaylistInfo(playlistId);
        Response<PlaylistInfo> infoResponse = downloader.getPlaylistInfo(infoRequest);
        if (!infoResponse.ok())
            throw new PlaylistDownloadingException("Не удалось скачать плейлист по ссылке.");
        return infoResponse.data();
    }
}
