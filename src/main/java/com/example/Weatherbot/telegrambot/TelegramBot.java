package com.example.Weatherbot.telegrambot;

import com.example.Weatherbot.config.BotConfig;
import com.example.Weatherbot.http.client.WeatherClient;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public final class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final WeatherClient weatherClient;

    @Autowired
    public TelegramBot(BotConfig botConfig, WeatherClient weatherClient) {
        this.botConfig = botConfig;
        this.weatherClient = weatherClient;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();

            // Логируем текст сообщения для отладки
            System.out.println("Получено сообщение: " + message.getText());

            // Проверка на наличие текста в сообщении и упоминание бота
            if (message.hasText()) {
                String messageText = message.getText().trim();
                boolean isMentioned = false;

                // Проверяем, упомянут ли бот в сообщении (если сообщение из группы)
                if (message.isGroupMessage() || message.isSuperGroupMessage()) {
                    List<MessageEntity> entities = message.getEntities();
                    if (entities != null) {
                        for (MessageEntity entity : entities) {
                            if (entity.getType().equals("mention") && messageText.substring(entity.getOffset(), entity.getLength()).equalsIgnoreCase("@" + getBotUsername())) {
                                isMentioned = true;
                                messageText = messageText.replace("@" + getBotUsername(), "").trim(); // Убираем упоминание из текста
                                break;
                            }
                        }
                    }
                }

                // Убираем вводные фразы
                messageText = cleanInput(messageText);

                // Обработка команд и сообщений
                if (messageText.equals("/start") && !message.isGroupMessage()) {
                    startCommandReceived(chatId, message.getChat().getFirstName());
                } else if (isMentioned || !message.isGroupMessage()) {
                    if (messageText.equals("Погода по координатам")) {
                        sendMessage(chatId, "Введите координаты (широта и долгота через запятую)");
                    } else if (messageText.equals("Погода по адресу")) {
                        sendMessage(chatId, "Введите адрес в следующем формате: Город ");
                    } else if (messageText.contains(",")) {
                        try {
                            String[] numbers = messageText.split(",");
                            float latitude = Float.parseFloat(numbers[0].trim());
                            float longitude = Float.parseFloat(numbers[1].trim());
                            String weather = weatherClient.getWeather(latitude, longitude);
                            sendMessage(chatId, weather);
                        } catch (NumberFormatException e) {
                            sendMessage(chatId, "Неправильный формат координат. Пожалуйста, введите широту и долготу через запятую.");
                        } catch (RuntimeException e) {
                            sendMessage(chatId, e.getMessage());
                        }
                    } else {
                        try {
                            String weather = weatherClient.getWeather(messageText);
                            sendMessage(chatId, weather);
                        } catch (RuntimeException e) {
                            sendMessage(chatId, e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "Привет, " + name + ", добро пожаловать в бот с погодой!" + "\n" +
                "Выберите, что вам нужно сделать";
        sendMessageWithKeyboard(chatId, answer);
    }

    private void sendMessageWithKeyboard(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setReplyMarkup(getMainKeyboard());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup getMainKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Погода по адресу"));
        row1.add(new KeyboardButton("Погода по координатам"));
        keyboardRows.add(row1);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }

    private void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String cleanInput(String input) {
        // Убираем вводные фразы
        input = input.replaceAll("(?i)какая погода в городе", "").trim();

        // Убираем все кроме букв, пробелов и запятых
        input = input.replaceAll("[^a-zA-Zа-яА-Я0-9,\\s]", "").trim();

        return input;
    }
}
