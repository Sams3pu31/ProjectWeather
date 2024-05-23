package com.example.Weatherbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class BotConfig {

    @Value("${bot.name}")
    private String botName; // Название бота, которое будет использоваться в Telegram.

    @Value("${bot.token}")
    private String token; // Токен для доступа к API Telegram.
}
