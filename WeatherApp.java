/**
 * Utilization of ChatGPT for Assistance:
 *
 * During the development of the WeatherApp project, ChatGPT was instrumental
 * in providing valuable assistance and guidance. Whenever faced with challenges
 * or uncertainties regarding various aspects of the project, including code
 * implementation, best practices, and troubleshooting, ChatGPT was consulted
 * to offer insights and solutions.
 *
 * The interactions with ChatGPT ranged from seeking explanations of complex
 * concepts to requesting code suggestions and explanations. By leveraging the
 * natural language processing capabilities of ChatGPT, I was able to articulate
 * my questions and receive clear and concise responses, facilitating a smoother
 * development process.
 *
 * Furthermore, ChatGPT helped in refining the design and architecture of the
 * WeatherApp by providing recommendations on class responsibilities, division
 * of labor within the team, and leveraging AI capabilities effectively.
 *
 * Overall, ChatGPT played a crucial role in the successful development of the
 * WeatherApp project, offering timely assistance and valuable insights at
 * various stages of the development lifecycle.
 */

/*
 * This package contains the main class of the WeatherApp application.
 * It provides functionality to display current weather and forecast for a given location.
 * The class implements the JavaFX Application interface to create a GUI application.
 * It also implements the iAPI interface to interact with the OpenWeatherMap API.
 */
package fi.tuni.prog3.weatherapp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;


/*
 * The WeatherApp class represents the main class of the WeatherApp application.
 * It extends the Application class provided by JavaFX to create a GUI application.
 * It also implements the iAPI interface to interact with the OpenWeatherMap API.
 */
public class WeatherApp extends Application implements iAPI {

    // API key for accessing OpenWeatherMap API
    private final String apiKey = "b7a1f575bd98bd1ef13e41ffe7537bda";

    /*
     * The start method is the entry point of the JavaFX application.
     * It sets up the GUI components and displays the main window of the application.
     */
    @Override
    public void start(Stage stage) {
        // Create a BorderPane layout for the main window
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(0, 0, 0, 0));
        root.setCenter(getCenterVBox()); // Set the center VBox containing main content
        Button quitButton = getQuitButton(); // Create a Quit button
        BorderPane.setMargin(quitButton, new Insets(10, 10, 0, 10)); // Set margins for the Quit button
        root.setBottom(quitButton); // Set the Quit button at the bottom right
        BorderPane.setAlignment(quitButton, Pos.TOP_RIGHT); // Align the Quit button to the top right

        // Create a Scene with the BorderPane as the root and set its dimensions
        Scene scene = new Scene(root, 700, 500);
        stage.setScene(scene); // Set the Scene to the stage
        stage.setTitle("WeatherApp"); // Set the title of the stage
        stage.show(); // Show the stage
    }

    /*
     * The lookUpLocation method is responsible for querying the OpenStreetMap API to retrieve
     * latitude and longitude coordinates for a given location.
     * It takes a location string as input and returns a string containing the latitude and longitude coordinates.
     * If the location is found, it returns the coordinates as a string. Otherwise, it returns an error message.
     */
    @Override
    public String lookUpLocation(String loc) {
        try {
            // Encode the location string to be used in the URL
            String encodedLoc = URLEncoder.encode(loc, "UTF-8");

            // Construct the API URL with the encoded location
            String apiUrl = "https://nominatim.openstreetmap.org/search?q=" + encodedLoc + "&format=json";
            URL url = new URL(apiUrl);

            // Open a connection to the API URL
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Read the response from the API
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse the JSON response to extract latitude and longitude coordinates
            String[] parts = response.toString().split(",");
            String lat = null, lon = null;
            for (String part : parts) {
                if (part.contains("\"lat\":")) {
                    lat = part.split(":")[1].replace("\"", "");
                }
                if (part.contains("\"lon\":")) {
                    lon = part.split(":")[1].replace("\"", "");
                }
            }

            // Return the coordinates as a string if found, otherwise return an error message
            if (lat != null && lon != null) {
                return lat + ", " + lon;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error occurred while fetching coordinates";
        }
    }

    /*
     * The getCurrentWeather method is responsible for retrieving the current weather information
     * for a given latitude and longitude coordinates using the OpenWeatherMap API.
     * It takes latitude and longitude coordinates as input and returns a string containing the current weather details.
     * If the weather information is successfully retrieved, it returns the formatted weather data as a string.
     * Otherwise, it returns an error message.
     */
    @Override
    public String getCurrentWeather(double lat, double lon) {
        // Construct the API URL for fetching current weather data
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey;

        // Send HTTP request to the API URL and retrieve the JSON response
        String jsonResponse = sendHttpRequest(apiUrl);

        // Parse the JSON response and return the formatted weather data or an error message
        if (jsonResponse != null) {
            return parseCurrentWeather(jsonResponse);
        } else {
            return "Error occurred while fetching current weather";
        }
    }

    /*
     * The getForecast method is responsible for retrieving the weather forecast
     * for a given latitude and longitude coordinates using the OpenWeatherMap API.
     * It takes latitude and longitude coordinates as input and returns a string containing the weather forecast.
     * If the forecast information is successfully retrieved, it returns the formatted forecast data as a string.
     * Otherwise, it returns an error message.
     */
    @Override
    public String getForecast(double lat, double lon) {
        // Construct the API URL for fetching weather forecast data
        String apiUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey;

        // Send HTTP request to the API URL and retrieve the JSON response
        String jsonResponse = sendHttpRequest(apiUrl);

        // Parse the JSON response and return the formatted forecast data or an error message
        if (jsonResponse != null) {
            return parseForecast(jsonResponse);
        } else {
            return "Error occurred while fetching forecast";
        }
    }
    /*
     * The parseCurrentWeather method is responsible for parsing the JSON response
     * obtained from the OpenWeatherMap API to extract the current weather information.
     * It takes the JSON response as input and returns a formatted string containing
     * the current weather details, including temperature, rain intensity, wind speed,
     * time of observation, and weather icon URL.
     * If the JSON response is successfully parsed, it returns the formatted weather data as a string.
     * Otherwise, it returns an error message.
     */
    private String parseCurrentWeather(String jsonResponse) {
        try {
            // Parse the JSON response to a JsonObject
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Extract temperature from the JSON object
            double temperature = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();

            // Initialize rain intensity to 0
            double rainIntensity = 0;

            // Check if rain information is available in the JSON response
            if (jsonObject.has("rain")) {
                JsonObject rainObj = jsonObject.getAsJsonObject("rain");
                // Check if rain intensity for the last hour is available
                if (rainObj.has("1h")) {
                    rainIntensity = rainObj.get("1h").getAsDouble();
                }
            }

            // Extract wind speed from the JSON object
            double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();

            // Format the time of observation
            String time = new SimpleDateFormat("HH:mm").format(new Date(jsonObject.get("dt").getAsLong() * 1000));

            // Extract weather icon code and construct the icon URL
            String iconCode = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("icon").getAsString();
            String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + ".png";

            // Construct the formatted weather data string
            return time + ", " + temperature + ", " + iconUrl + ", " +  rainIntensity + ", " + windSpeed;
        } catch (JsonSyntaxException e) {
            // Print stack trace and return error message if JSON parsing fails
            e.printStackTrace();
            return "Error parsing current weather response";
        }
    }

    /*
     * The parseForecast method is responsible for parsing the JSON response obtained
     * from the OpenWeatherMap API to extract the weather forecast information for
     * multiple time intervals.
     * It takes the JSON response as input and returns a formatted string containing
     * the forecast details, including date and time, temperature, rain intensity,
     * wind speed, and weather icon URL for each time interval.
     * If the JSON response is successfully parsed, it returns the formatted forecast data as a string.
     * Otherwise, it returns an error message.
     */
    private String parseForecast(String jsonResponse) {
        try {
            // Parse the JSON response to a JsonArray containing forecast data
            JsonArray jsonArray = JsonParser.parseString(jsonResponse).getAsJsonObject().getAsJsonArray("list");

            // Initialize a StringBuilder to construct the formatted forecast string
            StringBuilder forecastBuilder = new StringBuilder();

            // Iterate through each forecast object in the JsonArray
            for (int i = 0; i < jsonArray.size(); i++) {
                // Get the forecast object at index i
                JsonObject forecastObj = jsonArray.get(i).getAsJsonObject();

                // Extract temperature from the forecast object
                double temperature = forecastObj.getAsJsonObject("main").get("temp").getAsDouble();

                // Initialize rain intensity to 0
                double rainIntensity = 0;

                // Check if rain information is available in the forecast object
                if (forecastObj.has("rain")) {
                    JsonObject rainObj = forecastObj.getAsJsonObject("rain");
                    // Check if rain intensity for the last 3 hours is available
                    if (rainObj.has("3h")) {
                        rainIntensity = rainObj.get("3h").getAsDouble();
                    }
                }

                // Extract wind speed from the forecast object
                double windSpeed = forecastObj.getAsJsonObject("wind").get("speed").getAsDouble();

                // Extract date and time of the forecast
                String dateTime = forecastObj.get("dt_txt").getAsString();

                // Extract weather icon code and construct the icon URL
                String iconCode = forecastObj.getAsJsonArray("weather").get(0).getAsJsonObject().get("icon").getAsString();
                String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + ".png";

                // Append the forecast data to the StringBuilder
                forecastBuilder.append(dateTime).append(", ").append(temperature).append(", ").append(iconUrl).append(", ").append(rainIntensity).append(", ").append(windSpeed).append("\n");
            }

            // Return the formatted forecast data as a string
            return forecastBuilder.toString();
        } catch (JsonSyntaxException e) {
            // Print stack trace and return error message if JSON parsing fails
            e.printStackTrace();
            return "Error parsing forecast response";
        }
    }

    /*
     * The sendHttpRequest method is responsible for sending an HTTP GET request to
     * the specified API URL and retrieving the response.
     * It takes the API URL as input and returns the response body as a string.
     * If the request is successful and a response is received, it returns the response body.
     * If an IOException occurs during the request, it prints the stack trace and returns null.
     */
    private String sendHttpRequest(String apiUrl) {
        try {
            // Create a URL object with the specified API URL
            URL url = new URL(apiUrl);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Create a BufferedReader to read the response from the connection
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // Initialize a StringBuilder to store the response body
            StringBuilder response = new StringBuilder();
            String line;

            // Read each line of the response and append it to the StringBuilder
            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            // Close the BufferedReader
            in.close();

            // Return the response body as a string
            return response.toString();
        } catch (IOException e) {
            // Print the stack trace if an IOException occurs and return null
            e.printStackTrace();
            return null;
        }
    }

    /*
     * The getCenterVBox method is responsible for creating and configuring the central VBox
     * layout of the application, which contains the input field for location and the submit button.
     * It also handles the user interaction to fetch weather data based on the entered location.
     * The method returns the configured VBox.
     */
    private VBox getCenterVBox() {
        // Create a VBox with a vertical spacing of 20
        VBox centerVBox = new VBox(20);
        centerVBox.setAlignment(Pos.CENTER); // Set alignment to center
        centerVBox.setStyle("-fx-background-color: lightgreen;"); // Set background color

        // Create a label for the location input field
        Label locationInputLabel = new Label("Learn weather anywhere on Earth. Enter location:");
        // Set label styles
        locationInputLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-type: times;");
        locationInputLabel.setAlignment(Pos.CENTER);

        // Create an HBox to contain the location input field and the submit button
        HBox locationInput = new HBox(10);
        locationInput.setAlignment(Pos.CENTER);

        // Create a text field for entering the location
        TextField locationTextField = new TextField();
        locationTextField.setPromptText("Enter location"); // Set prompt text

        // Create the submit button
        Button submitButton = new Button("Submit");
        // Create the quit button
        Button quitButton = new Button("Quit");
        quitButton.setAlignment(Pos.BOTTOM_RIGHT);
        // Set action for quit button to exit the application
        quitButton.setOnAction(event -> Platform.exit());

        // Set action for submit button to fetch weather data based on entered location
        submitButton.setOnAction(event -> {
            String location = locationTextField.getText(); // Get the entered location
            String coordinates = lookUpLocation(location); // Look up coordinates for the location
            if (coordinates != null) {
                // If coordinates are found, parse latitude and longitude
                String[] coord = coordinates.split(",");
                double lat = Double.parseDouble(coord[0]);
                double lon = Double.parseDouble(coord[1]);
                // Get current weather data for the location
                String currentWeather = getCurrentWeather(lat, lon);
                // Create a VBox to display current weather information
                VBox currentWeatherBox = createWeatherBox(currentWeather, location);
                // Get forecast data for the location
                String forecast = getForecast(lat, lon);
                // Create a VBox to display forecast information
                VBox forecastBox = createForecastBox(forecast);
                // Clear existing content in the center VBox and add current weather and forecast boxes
                centerVBox.getChildren().clear();
                centerVBox.getChildren().addAll(currentWeatherBox, forecastBox);
            } else {
                // If coordinates are not found, display an error message
                Label errorLabel = new Label("Coordinates not found for location: " + location);
                centerVBox.getChildren().clear();
                centerVBox.getChildren().add(errorLabel);
            }
        });

        // Add location text field and submit button to the location input HBox
        locationInput.getChildren().addAll(locationTextField, submitButton);
        // Add location input label and input HBox to the center VBox
        centerVBox.getChildren().addAll(locationInputLabel, locationInput);

        // Return the configured center VBox
        return centerVBox;
    }

    /*
     * The createWeatherBox method is responsible for creating and configuring a VBox layout
     * to display current weather information for a given location. It parses the provided
     * currentWeather string and constructs a layout accordingly. The method returns the
     * configured VBox.
     */
    private VBox createWeatherBox(String currentWeather, String loc) {
        // Create a VBox with vertical spacing of 10
        VBox weatherBox = new VBox(10);
        weatherBox.setStyle("-fx-background-color: lightblue;"); // Set background color
        weatherBox.setAlignment(Pos.TOP_CENTER); // Set alignment to top center

        // Create an HBox for the nested layout
        HBox nestedLayout = new HBox(10);
        nestedLayout.setAlignment(Pos.CENTER); // Set alignment to center

        // Create a label for the location title
        Label titleLabel = new Label(loc);
        // Set label styles
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-type: times;");
        nestedLayout.getChildren().add(titleLabel); // Add title label to the nested layout

        // Split the currentWeather string into lines
        String[] lines = currentWeather.split(", ");
        int count = 0;
        Image image_droplet = null;
        Image image_arrow = null;

        // Iterate through each line in the currentWeather string
        for (String e : lines) {
            if (count == 0) {
                // For the first line, create a label with location details
                Label label = new Label(e);
                // Set label styles
                label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-border-color: red; -fx-border-width: 2px;");
                nestedLayout.getChildren().add(label); // Add label to the nested layout
                weatherBox.getChildren().add(nestedLayout); // Add nested layout to the weatherBox
            } else if (count == 1 || count == 2) {
                if (count == 1) {
                    // For temperature information, create a label with temperature in Celsius
                    nestedLayout = new HBox(10);
                    nestedLayout.setAlignment(Pos.CENTER);
                    double temp = Double.parseDouble(e) - 273.15; // Convert temperature from Kelvin to Celsius
                    temp = Math.round(temp * 100.0) / 100.0; // Round temperature to two decimal places
                    String line = temp + " °C";
                    nestedLayout.getChildren().add(new Label(line)); // Add temperature label to the nested layout
                    nestedLayout.getChildren().get(0).setStyle("-fx-font-size: 16px;"); // Set label styles
                } else {
                    // For weather icon, create an ImageView
                    ImageView iconView = createIconImageView(e);
                    iconView.setFitWidth(80);
                    iconView.setFitHeight(80);
                    nestedLayout.getChildren().add(iconView); // Add icon view to the nested layout
                    weatherBox.getChildren().add(nestedLayout); // Add nested layout to the weatherBox
                }
            } else if (count == 3 || count == 4) {
                if (count == 3) {
                    // For rain intensity, create an ImageView with a water droplet icon
                    nestedLayout = new HBox(10);
                    nestedLayout.setAlignment(Pos.CENTER);
                    File water_droplet = new File("./src/main/java/fi/tuni/prog3/weatherapp/icons8-droplet-96.png");
                    try {
                        FileInputStream inputStream = new FileInputStream(water_droplet);
                        image_droplet = new Image(inputStream);
                    } catch (Exception s) {
                        System.out.println(s);
                    }
                    ImageView imageDroplet = new ImageView(image_droplet);
                    imageDroplet.setFitWidth(50);
                    imageDroplet.setFitHeight(50);
                    nestedLayout.getChildren().add(imageDroplet); // Add water droplet image to the nested layout
                    nestedLayout.getChildren().add(new Label(e)); // Add rain intensity label to the nested layout
                } else {
                    // For wind speed, create an ImageView with an arrow icon
                    File arrow = new File("./src/main/java/fi/tuni/prog3/weatherapp/icons8-arrow-96.png");
                    try {
                        FileInputStream inputStream = new FileInputStream(arrow);
                        image_arrow = new Image(inputStream);
                    } catch (Exception s) {
                        System.out.println(s);
                    }
                    ImageView imageArrow = new ImageView(image_arrow);
                    imageArrow.setFitWidth(50);
                    imageArrow.setFitHeight(50);
                    nestedLayout.getChildren().add(imageArrow); // Add arrow image to the nested layout
                    nestedLayout.getChildren().add(new Label(e)); // Add wind speed label to the nested layout
                    weatherBox.getChildren().add(nestedLayout); // Add nested layout to the weatherBox
                }
            }
            count = count + 1; // Increment count
        }

        // Return the configured weatherBox
        return weatherBox;
    }


    /*
     * The createForecastBox method is responsible for creating and configuring a VBox layout
     * to display forecasted weather information. It parses the provided forecast string and
     * constructs a layout accordingly. The method returns the configured VBox.
     */
    private VBox createForecastBox(String forecast) {
        VBox mainVBox = new VBox(10);
        HBox forecastBox = new HBox(10);
        forecastBox.setAlignment(Pos.TOP_CENTER);

        String[] forecastLines = forecast.split("\n");

        Map<String, List<Double>> tempDataByDay = new TreeMap<>(); // Use TreeMap for automatic sorting
        final VBox[] lastClickedForecastBlock = {null};
        final HBox[] lastAddedDailyForecastBox = {null};

        double[] minTemps = new double[6];
        double[] maxTemps = new double[6];

        for (String line : forecastLines) {
            String[] parts = line.split(", ");
            String date = parts[0].substring(0, 10);
            double temperature = Math.round((Double.parseDouble(parts[1]) - 273.15) * 10.0) / 10.0;

            if (!tempDataByDay.containsKey(date)) {
                tempDataByDay.put(date, new ArrayList<>());
            }
            tempDataByDay.get(date).add(temperature);
        }

        int currentIndex = 0;
        for (Map.Entry<String, List<Double>> entry : tempDataByDay.entrySet()) {
            if (currentIndex < 6) {
                String date = entry.getKey();
                List<Double> temperatures = entry.getValue();
                minTemps[currentIndex] = temperatures.stream().min(Comparator.naturalOrder()).orElse(Double.NaN);
                maxTemps[currentIndex] = temperatures.stream().max(Comparator.naturalOrder()).orElse(Double.NaN);

                VBox forecastBlocks = new VBox(10);

                forecastBlocks.setOnMouseEntered(event -> {
                    forecastBlocks.setStyle("-fx-background-color: orange;");
                });

                forecastBlocks.setOnMouseExited(event -> {
                    forecastBlocks.setStyle("-fx-background-color: transparent;");
                });

                forecastBlocks.setOnMouseClicked(event -> {
                    if (lastClickedForecastBlock[0] != null) {
                        lastClickedForecastBlock[0].setStyle("");
                    }
                    lastClickedForecastBlock[0] = forecastBlocks;
                    forecastBlocks.setStyle("-fx-border-color: blue; -fx-border-width: 2px;");

                    StringBuilder dailyForecastBuilder = new StringBuilder();
                    for (String forecastLine : forecastLines) {
                        if (forecastLine.startsWith(date)) {
                            dailyForecastBuilder.append(forecastLine).append("\n");
                        }
                    }
                    String dailyForecast = dailyForecastBuilder.toString();
                    HBox dailyForecastBox = createDailyForecastBox(dailyForecast);
                    if (lastAddedDailyForecastBox[0] != null) {
                        mainVBox.getChildren().remove(lastAddedDailyForecastBox[0]);
                    }
                    mainVBox.getChildren().add(dailyForecastBox);
                    lastAddedDailyForecastBox[0] = dailyForecastBox;
                });

                String[] icons = new String[6];
                String[] days = new String[6];
                int count = 0;
                String current_day = "";
                int the_count = 0;

                for (String forecastLine : forecastLines) {
                    String[] data = forecastLine.split(", ");
                    String day = data[0].substring(0, 10);
                    if (!Objects.equals(day, current_day)) {
                        if (!current_day.isEmpty() || the_count == 0) {
                            if (data[2] != null) {
                                icons[the_count] = data[2];
                            }
                            days[the_count] = day;
                            the_count++;
                        }
                        current_day = day;
                    }
                }

                forecastBlocks.getChildren().add(new Label(days[currentIndex]));
                forecastBlocks.getChildren().add(createIconImageView(icons[currentIndex]));
                String tempRange = String.valueOf(minTemps[currentIndex]) + "..." + String.valueOf(maxTemps[currentIndex]) + "°C";
                forecastBlocks.getChildren().add(new Label(tempRange));

                forecastBox.getChildren().add(forecastBlocks);
                currentIndex++;
            }
        }

        mainVBox.getChildren().add(forecastBox);
        return mainVBox;
    }

    /*
     * The createDailyForecastBox method is responsible for creating and configuring an HBox layout
     * to display daily forecasted weather information. It parses the provided daily forecast string
     * and constructs a layout accordingly. The method returns the configured HBox.
     */
    private HBox createDailyForecastBox(String dailyForecast) {
        // Initialize image variables
        Image imageDroplet = null;
        Image arrowImage = null;

        // Split the daily forecast string into individual lines
        String[] parts = dailyForecast.split("\n");

        // Create an HBox to hold the daily forecast information
        HBox dailyForecastBox = new HBox(10);

        // Iterate through each line in the daily forecast
        for (String line : parts) {
            // Create a VBox to hold individual forecast data
            VBox indBox = new VBox(10);
            // Split the line into data parts
            String[] data = line.split(", ");
            // Extract hour from timestamp
            String hour = data[0].substring(11, 13) + ".00";
            // Add hour label to indBox VBox
            indBox.getChildren().add(new Label(hour));
            // Create an ImageView for weather icon
            indBox.getChildren().add(createIconImageView(data[2]));
            // Calculate temperature in Celsius and add to indBox VBox
            String temp = String.valueOf(Math.round((Double.parseDouble(data[1]) - 273.15) * 100.0) / 100.0) + "°C";
            indBox.getChildren().add(new Label(temp));

            // Create an HBox to hold rain information
            HBox rainBox = new HBox(10);
            // Load droplet image
            File waterDroplet = new File("./src/main/java/fi/tuni/prog3/weatherapp/icons8-droplet-96.png");
            try {
                FileInputStream inputStream = new FileInputStream(waterDroplet);
                imageDroplet = new Image(inputStream);
            } catch (Exception e) {
                System.out.println(e);
            }
            // Create ImageView for droplet image and add rain information
            ImageView imageDropletView = new ImageView(imageDroplet);
            imageDropletView.setFitWidth(30);
            imageDropletView.setFitHeight(30);
            rainBox.getChildren().add(imageDropletView);
            rainBox.getChildren().add(new Label(data[3]));
            // Add rainBox to indBox VBox
            indBox.getChildren().add(rainBox);

            // Create an HBox to hold wind information
            HBox windBox = new HBox(10);
            // Load arrow image
            File arrow = new File("./src/main/java/fi/tuni/prog3/weatherapp/icons8-arrow-96.png");
            try {
                FileInputStream inputStream = new FileInputStream(arrow);
                arrowImage = new Image(inputStream);
            } catch (Exception e) {
                System.out.println(e);
            }
            // Create ImageView for arrow image and add wind information
            ImageView arrowImageView = new ImageView(arrowImage);
            arrowImageView.setFitWidth(30);
            arrowImageView.setFitHeight(30);
            windBox.getChildren().add(arrowImageView);
            windBox.getChildren().add(new Label(data[4]));
            // Add windBox to indBox VBox
            indBox.getChildren().add(windBox);

            // Add indBox VBox to dailyForecastBox HBox
            dailyForecastBox.getChildren().add(indBox);
        }

        // Create and configure Quit button
        Button quitButton = new Button("Quit");
        quitButton.setAlignment(Pos.BOTTOM_RIGHT);
        quitButton.setOnAction(event -> Platform.exit());

        // Create an HBox to hold the Quit button at the bottom right
        HBox bottomRightBox = new HBox();
        bottomRightBox.getChildren().add(quitButton);
        bottomRightBox.setAlignment(Pos.BOTTOM_RIGHT);

        // Add Quit button HBox to dailyForecastBox HBox
        dailyForecastBox.getChildren().add(bottomRightBox);

        // Return the configured dailyForecastBox HBox
        return dailyForecastBox;
    }

    /*
     * The getQuitButton method is responsible for creating and configuring a Quit button.
     * When clicked, this button will exit the application.
     * It returns the configured Quit button.
     */
    private Button getQuitButton() {
        // Create a new Button with the label "Quit"
        Button button = new Button("Quit");
        // Add an action event handler to the button to exit the application
        button.setOnAction((event) -> {
            Platform.exit();
        });
        // Return the configured Quit button
        return button;
    }


    /*
     * The main method is the entry point of the WeatherApp application.
     * It launches the JavaFX application by calling the launch method.
     */
    public static void main(String[] args) {
        // Launch the JavaFX application
        launch();
    }


    /*
     * This method creates an ImageView for displaying weather icons.
     * It takes the URL of the weather icon as input and returns the corresponding ImageView.
     */
    private ImageView createIconImageView(String iconUrl) {
        try {
            // Create an Image object from the provided URL
            Image iconImage = new Image(iconUrl);
            // Create an ImageView with the Image object
            ImageView iconView = new ImageView(iconImage);
            // Set the width of the ImageView
            iconView.setFitWidth(50);
            // Preserve the aspect ratio of the image
            iconView.setPreserveRatio(true);
            // Return the ImageView
            return iconView;
        } catch (Exception e) {
            // Print the stack trace if an exception occurs
            e.printStackTrace();
            // Return an empty ImageView if an exception occurs
            return new ImageView();
        }
    }

}
