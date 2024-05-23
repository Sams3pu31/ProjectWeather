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
    private final String apiKey = "033694b0bd5207aeaa89dff0d262efa2"; // ваш ключ API

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getWeather(float latitude, float longitude) {
        String url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric&lang=ru", latitude, longitude, apiKey);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            System.out.println("Ответ от API погоды: " + response.getBody()); // Логируем ответ API погоды
            return parseWeatherResponse(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("Invalid API key for OpenWeatherMap API");
            } else {
                throw new RuntimeException("HTTP ошибка при попытке получить погоду: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Произошла ошибка при получении погоды.");
        }
    }

    public String getWeather(String address) {
        GeoLocatorServiceImpl geoLocatorService = new GeoLocatorServiceImpl();
        float[] coordinates = geoLocatorService.getCoordinates(address);
        if (coordinates == null) {
            return "Не удалось получить координаты для указанного адреса.";
        }
        return getWeather(coordinates[0], coordinates[1]);
    }

    private String parseWeatherResponse(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode mainNode = rootNode.path("main");
            double temperature = mainNode.path("temp").asDouble();
            String condition = rootNode.path("weather").get(0).path("description").asText();
            String city = rootNode.path("name").asText();
            String advice = getOutfitAdvice(temperature);

            return "Температура в городе " + city + ": " + String.format("%.1f", temperature) + "°C\n" + condition +
                    "\n" + advice;
        } catch (Exception e) {
            e.printStackTrace();
            return "Произошла ошибка при получении погоды для указанного города.";
        }
    }

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
