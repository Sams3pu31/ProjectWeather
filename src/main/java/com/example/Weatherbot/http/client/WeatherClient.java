package com.example.Weatherbot.http.client;
import com.example.Weatherbot.http.service.GeoLocatorServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherClient {
    // Ключ API для доступа к OpenWeatherMap.
    private final String apiKey = "033694b0bd5207aeaa89dff0d262efa2";

    // RestTemplate используется для выполнения HTTP-запросов.
    private final RestTemplate restTemplate = new RestTemplate();
    // ObjectMapper используется для преобразования JSON-ответов в объекты Java.
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Метод для получения погоды по координатам (широта и долгота).
    public String getWeather(float latitude, float longitude) {
        // Формируем URL для запроса к OpenWeatherMap API с заданными координатами и ключом API.
        String url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric&lang=ru", latitude, longitude, apiKey);

        try {
            // Выполняем GET-запрос к API и получаем ответ.
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            // Парсим ответ и возвращаем результат.
            return parseWeatherResponse(response.getBody());
        } catch (HttpClientErrorException e) {
            // Обработка ошибки, если ключ API недействителен.
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("Invalid API key for OpenWeatherMap API");
            } else {
                throw e;
            }
        }
    }

    // Метод для получения погоды по адресу.
    public String getWeather(String address) {
        // Используем GeoLocatorService для получения координат по адресу.
        GeoLocatorServiceImpl geoLocatorService = new GeoLocatorServiceImpl();
        float[] coordinates = geoLocatorService.getCoordinates(address);
        // Получаем погоду по координатам.
        return getWeather(coordinates[0], coordinates[1]);
    }

    // Метод для парсинга ответа с погодными данными.
    private String parseWeatherResponse(String response) {
        try {
            // Читаем JSON-ответ и извлекаем необходимые данные.
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode mainNode = rootNode.path("main");
            double temperature = mainNode.path("temp").asDouble();
            String condition = rootNode.path("weather").get(0).path("description").asText();
            String city = rootNode.path("name").asText();
            String advice = getOutfitAdvice(temperature);

            // Формируем строку с информацией о погоде и советом по одежде.
            return "Температура в " + city + ": " + String.format("%.1f", temperature) + "°C\n" + condition + "\n" + advice;
        } catch (Exception e) {
            e.printStackTrace();
            // Возвращаем сообщение об ошибке, если что-то пошло не так.
            return "Произошла ошибка при получении погоды для указанного города.";
        }
    }

    // Метод для получения совета по одежде на основе температуры.
    private String getOutfitAdvice(double temperature) {
        if (temperature <= -25) {
            return "Совет по одежде: Очень холодно! Наденьте много теплой одежды, шарф, перчатки и теплую обувь.";
        } else if (temperature > -25 && temperature <= -15) {
            return "Совет по одежде: Очень холодно. Рекомендуется надеть теплую куртку, шапку и теплую обувь.";
        } else if (temperature > -15 && temperature <= -5) {
            return "Совет по одежде: Холодно. Рекомендуется надеть теплую куртку, шарф и теплую обувь.";
        } else if (temperature <= 0) {
            return "Совет по одежде: Холодно. Рекомендуется надеть свитер, теплую куртку и шапку!";
        } else if (temperature > 0 && temperature <= 15) {
            return "Совет по одежде: Прохладно. Рекомендуется надеть свитер или кофту.";
        } else if (temperature > 15 && temperature <= 25) {
            return "Совет по одежде: Приятная температура. Легкая одежда подойдет для комфортной прогулки.";
        } else {
            return "Совет по одежде: Жарко. Оденьтесь легко и не забудьте пить воду. Проводите время в тени.";
        }
    }
}
