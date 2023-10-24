package com.example.telegramListPlay;

import com.example.telegramListPlay.telegramBot.TelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class TelegramListPlayApplication {
	public static void main(String[] args) {
		// check ffmpeg work
		String currentDir = System.getProperty("user.dir");
		String ffmpegPath = currentDir + "/target/classes/ffmpeg-linux/ffmpeg";
		String ffmpegCommand = ffmpegPath + " -version";

		ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand.split(" "));
		Process process = null;
		try {
			process = processBuilder.start();

			// Wait for the process to complete
			int exitCode = process.waitFor();
			System.out.println("Process exited with code " + exitCode);
		} catch (IOException | InterruptedException e) {
			System.out.println(e.getMessage());
			System.out.println(currentDir);
		}

		SpringApplication.run(TelegramListPlayApplication.class, args);
	}

}
