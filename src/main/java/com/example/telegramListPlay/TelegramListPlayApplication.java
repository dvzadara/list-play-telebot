package com.example.telegramListPlay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TelegramListPlayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramListPlayApplication.class, args);
//		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TelegramBotConfiguration.class);
//		YoutubeService youtubeService = context.getBean("youtubeService", YoutubeService.class);
//		System.out.println(youtubeService.getYoutubeVideo());
	}

}
