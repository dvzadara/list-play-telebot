package com.example.telegramListPlay.Exceptions;

public class VideoDownloadingException extends Exception{
    public VideoDownloadingException(String message){
        super(message);
    }
    public VideoDownloadingException(String message, Throwable err){
        super(message, err);
    }
}

