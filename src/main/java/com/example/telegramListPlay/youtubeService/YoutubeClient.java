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
                .renameTo(transliterate(name))
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
            throw new PlaylistDownloadingException("Неможливо завантажити плейлист за посиланням.");
        return infoResponse.data();
    }

    public static String transliterate(String message){
        char[] abcCyr =   {' ','а','б','в','г','д','е','ё', 'ж','з','и','й','к','л','м','н','о','п','р','с','т','у','ф','х', 'ц','ч', 'ш','щ','ъ','ы','ь','э', 'ю','я','А','Б','В','Г','Д','Е','Ё', 'Ж','З','И','Й','К','Л','М','Н','О','П','Р','С','Т','У','Ф','Х', 'Ц', 'Ч','Ш', 'Щ','Ъ','Ы','Ь','Э','Ю','Я','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
        String[] abcLat = {"_","a","b","v","g","d","e","e","zh","z","i","y","k","l","m","n","o","p","r","s","t","u","f","h","ts","ch","sh","sch", "","i", "","e","ju","ja","A","B","V","G","D","E","E","Zh","Z","I","Y","K","L","M","N","O","P","R","S","T","U","F","H","Ts","Ch","Sh","Sch", "","I", "","E","Ju","Ja","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            for (int x = 0; x < abcCyr.length; x++ ) {
                if (message.charAt(i) == abcCyr[x]) {
                    builder.append(abcLat[x]);
                }
            }
            if ('0' <= message.charAt(i) && message.charAt(i) <= '9' || message.charAt(i) == '-' ||
                    message.charAt(i) == '_' || message.charAt(i) == '.')
                builder.append(message.charAt(i));
        }
        return builder.toString();
    }

}
