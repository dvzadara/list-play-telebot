package com.example.telegramListPlay.youtubeService;

import com.example.telegramListPlay.Exceptions.VideoDownloadingException;
//import net.bramp.ffmpeg.FFmpeg;
//import net.bramp.ffmpeg.FFmpegExecutor;
//import net.bramp.ffmpeg.FFprobe;
//import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.*;
import java.util.Arrays;

public class ToMp3Converter {
    static String ffmpegPath = "/src/main/resources/ffmpeg-linux/ffmpeg";
    public static File mediaFileToMp3(File mediaFile) throws IOException, VideoDownloadingException {
        try {
            if (!mediaFile.exists()) {
                throw new VideoDownloadingException("Не вдалося знайти завантажений файл.");
            }
            String mediaFilePath = mediaFile.getPath();
            String mp3FilePath = mediaFile.getParent() + "/" + changeFilenameExtension(mediaFile.getName(), "mp3");

            String currentDir = System.getProperty("user.dir");
            String ffmpegPath = currentDir + "/target/classes/ffmpeg-linux/ffmpeg";
            String[] ffmpegCommand = {ffmpegPath, "-i", mediaFilePath, "-vn", "-ar", "44100", "-ac", "2", "-b:a", "192k", mp3FilePath};

            ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand);
            // Start the process
            Process process = processBuilder.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder builder = new StringBuilder();
            String line = null;
            while ( (line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            System.out.println(Arrays.toString(ffmpegCommand));
            System.out.println(result);
            File outputFile = new File(mp3FilePath);
            if (!outputFile.exists()) {
                throw new VideoDownloadingException("Неможливо конвертувати файл.");
            }
            return outputFile;
        } catch (IOException | InterruptedException e) {
            throw new VideoDownloadingException("Неможливо конвертувати файл.", e);
        }
    }

    public static String changeFilenameExtension(String fileName, String newExtension) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            return fileName.substring(0, lastDotIndex) + "." + newExtension;
        } else {
            return fileName + "." + newExtension;
        }
    }
}
