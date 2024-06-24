package com.WeatherWise;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Weather App!");
        int choice;
        while(true) {
            System.out.println("1. Know the weather \n2. Exit the app");
            System.out.print("Enter your choice from the above options: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character
            switch (choice) {
                case 1 -> {
                    System.out.print("Enter a city name: ");
                    String city = scanner.nextLine();
                    System.out.println("Weather data for " + city + ":");
                    displayWeatherData(city);
                }
                case 2 -> {
                    System.out.println("Exiting the app. Goodbye!");
                    return; // Exit the program
                }
                default -> System.out.println("Invalid choice. Please select 1 or 2.");
            }
        }
    }

    private static JSONObject getLocationData(String city){
        city = city.replaceAll(" ", "+");

        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + city + "&count=1&language=en&format=json";

        try{
            // 1. Fetch the API response based on API Link
            HttpURLConnection apiConnection = getApiResponse(urlString);

            // check for response status
            // 200 -> means that the connection was a success
            if (apiConnection != null && apiConnection.getResponseCode() == 200) {
                // 2. Read the response and store it in a String variable
                String jsonResponse = readApiResponse(apiConnection);

                // 3. Parse the string into a JSON Object
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(jsonResponse);

                // 4. Retrieve Location Data
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                if (locationData != null && !locationData.isEmpty()) {
                    return (JSONObject) locationData.get(0);
                } else {
                    System.out.println("Error: No location data found for " + city.replaceAll("\\+", " "));
                    return null;
                }
            }
            else{
                System.out.println("Error: Could not connect to API, Try again later!!");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static String readApiResponse(HttpURLConnection apiConnection) {
        try {
            // Create a StringBuilder to store the resulting JSON data
            StringBuilder resultJson = new StringBuilder();

            // Create a Scanner to read from the InputStream of the HttpURLConnection
            Scanner scanner = new Scanner(apiConnection.getInputStream());

            // Loop through each line in the response and append it to the StringBuilder
            while (scanner.hasNext()) {
                // Read and append the current line to the StringBuilder
                resultJson.append(scanner.nextLine());
            }

            // Close the Scanner to release resources associated with it
            scanner.close();

            // Return the JSON data as a String
            return resultJson.toString();

        } catch (IOException e) {
            // Print the exception details in case of an IOException
            e.printStackTrace();
        }

        // Return null if there was an issue reading the response
        return null;
    }

    private static HttpURLConnection getApiResponse(String urlString){
        try{
            // attempt to create connection
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // set request method to get
            connection.setRequestMethod("GET");

            return connection;
        }catch(IOException e){
            e.printStackTrace();
        }

        // could not make connection
        return null;
    }

    private static void displayWeatherData(String city) {
        // Get location data
        JSONObject cityLocationData = getLocationData(city);
        if (cityLocationData == null) {
            System.out.println("Error: Location data not available.");
            return;
        }

        double latitude = (double) cityLocationData.get("latitude");
        double longitude = (double) cityLocationData.get("longitude");

        try{
            // 1. Fetch the API response based on API Link
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude +
                    "&longitude=" + longitude + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m";
            HttpURLConnection apiConnection = getApiResponse(url);

            // check for response status
            // 200 -> means that the connection was a success
            if (apiConnection != null && apiConnection.getResponseCode() == 200) {

                // 2. Read the response and convert store String type
                String jsonResponse = readApiResponse(apiConnection);

                // 3. Parse the string into a JSON Object
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(jsonResponse);
                JSONObject currentWeatherJson = (JSONObject) jsonObject.get("current");
                //System.out.println(currentWeatherJson.toJSONString());

                // 4. Store the data into their corresponding data type
                String time = (String) currentWeatherJson.get("time");
                System.out.println("Current Time: " + time);

                double temperature = (double) currentWeatherJson.get("temperature_2m");
                System.out.println("Current Temperature (C): " + temperature);

                long relativeHumidity = (long) currentWeatherJson.get("relative_humidity_2m");
                System.out.println("Relative Humidity: " + relativeHumidity);

                double windSpeed = (double) currentWeatherJson.get("wind_speed_10m");
                System.out.println("Wind Speed: " + windSpeed);
            }
            else{
                System.out.println("Error: Could not connect to API");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}