package eu.berdosi.app.heartbeat;

import android.os.AsyncTask;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class RequestHandler extends AsyncTask<String, Void, String> {


    public static final String API_URL = "http://5.15.43.5:7058/";
    private static final String AUTH = "api/Auth/";

    public static final String LOGIN_ENDPOINT = AUTH + "login";
    public static final String DATA_ENDPOINT = "api/phone";
    public static final String TRIGGER_ENDPOINT = "api/trigger";

    public static String JWT = "";



    public static void SendPulseData(CopyOnWriteArrayList<Measurement<Integer>> pulses) throws JsonProcessingException {
        String URL = API_URL + DATA_ENDPOINT;

        RequestHandler requestHandler = new RequestHandler();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(pulses);
        requestHandler.execute(URL,"POST", json);

    }

    public static void SendLogin() throws Exception {
        Credentials credentials = new Credentials("Phone", "12345");

        String URL = API_URL + LOGIN_ENDPOINT;

        RequestHandler requestHandler = new RequestHandler();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(credentials);
        requestHandler.execute(URL, "POST", json);
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String urlString = params[0];
            String requestType = params[1];
            String jsonData = params[2];

            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(requestType);

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + RequestHandler.JWT);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonData.getBytes());
            outputStream.flush();

            int responseCode = connection.getResponseCode();
            String output = connection.getResponseMessage();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> responseMap;
                responseMap = objectMapper.readValue(responseBuilder.toString(), new TypeReference<Map<String, String>>() {});

                if(responseMap.containsKey("token")){
                    RequestHandler.JWT = responseMap.get("token");
                }


                return "";
            } else {
                return "Error: " + responseCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
