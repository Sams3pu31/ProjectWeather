package com.example.Weatherbot.telegrambot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotInitializer {

    // Ссылка на основной класс бота Telegram.
    private final TelegramBot telegramBot;

    // Внедрение зависимости TelegramBot через конструктор.
    @Autowired
    public BotInitializer(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    // Метод, помеченный @EventListener, будет вызван при возникновении события ContextRefreshedEvent,
    // что происходит после инициализации контекста Spring.
    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        // Создание экземпляра TelegramBotsApi с использованием DefaultBotSession.
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            // Регистрация бота в API Telegram.
            telegramBotsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            // Вывод стека вызовов в случае возникновения ошибки при регистрации бота.
            e.printStackTrace();
        }
    }
}
