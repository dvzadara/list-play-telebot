package com.example.telegramListPlay.youtubeService;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.*;

public class ToMp3Converter {
    public static File mediaFileToMp3(File mediaFile) throws IOException {
        String mediaFilePath = mediaFile.getPath();
        String mp3FilePath = mediaFile.getParent() + changeFileExtension(mediaFile.getName(), "mp3");
//        FFmpeg ffmpeg = new FFmpeg();
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(mediaFilePath)
                .addOutput(mp3FilePath)
                .done();

        FFmpegExecutor executor = new FFmpegExecutor();
        executor.createJob(builder).run();
        return new File(mp3FilePath);
    }

    public static String changeFileExtension(String fileName, String newExtension) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            return fileName.substring(0, lastDotIndex) + "." + newExtension;
        } else {
            return fileName + "." + newExtension;
        }
    }
}
