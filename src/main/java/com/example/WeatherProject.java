package com.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Properties;

public class WeatherProject {
    
    private static final String BASE_URL = "http://api.weatherapi.com/v1/current.json";

    public static void main(String[] args) {
        Scanner kb = new Scanner(System.in);
        System.out.print("Enter city: ");
        String city = kb.nextLine(); // You can replace this with user input
        String weatherData = getWeatherData(city);
        if (weatherData != null) {
            generateHtmlPage(weatherData);
        }
        kb.close();
    }

    private static String getWeatherData(String city) {
        Properties properties = new Properties();
        try(FileInputStream input = new FileInputStream("config.properties")){
            properties.load(input);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        final String API_KEY = properties.getProperty("apiKey");
        String url = BASE_URL + "?key=" + API_KEY + "&q=" + city;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toString(entity);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void generateHtmlPage(String weatherData) {
        JsonObject jsonObject = JsonParser.parseString(weatherData).getAsJsonObject();
        JsonObject current = jsonObject.getAsJsonObject("current");
        JsonObject location = jsonObject.getAsJsonObject("location");

        String locationName = location.get("name").getAsString();
        String region = location.get("region").getAsString();
        String country = location.get("country").getAsString();
        double tempF = current.get("temp_f").getAsDouble();
        String condition = current.getAsJsonObject("condition").get("text").getAsString();
        String icon = current.getAsJsonObject("condition").get("icon").getAsString();

        String htmlContent = "<!DOCTYPE html>\n<html>\n<head>\n<title>Weather App</title>\n</head>\n<body>\n" +
                "<h1>Weather in " + locationName + ", " + region + ", " + country + "</h1>\n" +
                "<p><b>Temperature: " + tempF + "&deg;F</b></p>\n" +
                "<p>Condition: " + condition + "</p>\n" +
                "<img src=\"" + icon + "\" alt=\"" + condition + "\"/>\n" +
                "</body>\n</html>";

        try (FileWriter fileWriter = new FileWriter("src/main/resources/index.html")) {
            fileWriter.write(htmlContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}