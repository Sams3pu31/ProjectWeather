package com.example.Weatherbot.http.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
@Service
public class GeoLocatorServiceImpl {
    // Ключ API для доступа к OpenWeatherMap.
    private final String apiKey = "033694b0bd5207aeaa89dff0d262efa2";

    // RestTemplate используется для выполнения HTTP-запросов.
    private final RestTemplate restTemplate = new RestTemplate();
    // ObjectMapper используется для преобразования JSON-ответов в объекты Java.
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Метод для получения координат (широта и долгота) по адресу.
    public float[] getCoordinates(String address) {
        // Формируем URL для запроса к OpenWeatherMap API с заданным адресом и ключом API.
        String url = String.format("http://api.openweathermap.org/geo/1.0/direct?q=%s&limit=1&appid=%s", address, apiKey);

        try {
            // Выполняем GET-запрос к API и получаем ответ.
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            // Парсим ответ и возвращаем координаты.
            return parseCoordinates(response.getBody());
        } catch (HttpClientErrorException e) {
            // Обработка ошибки, если ключ API недействителен.
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("Неверный ключ АПИ");
            } else {
                throw e;
            }
        }
    }

    // Метод для парсинга ответа с координатами.
    private float[] parseCoordinates(String response) {
        try {
            // Читаем JSON-ответ и извлекаем координаты.
            JsonNode rootNode = objectMapper.readTree(response).get(0);
            float latitude = rootNode.path("lat").floatValue();
            float longitude = rootNode.path("lon").floatValue();
            // Возвращаем широту и долготу как массив.
            return new float[]{latitude, longitude};
        } catch (Exception e) {
            e.printStackTrace();
            // Возвращаем null, если что-то пошло не так.
            return null;
        }
    }
}
