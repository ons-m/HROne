package com.recruitx.hrone.API;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class MailCheckService {

    private static final String BASE_URL = "https://api.usercheck.com/email/";
    private final HttpClient httpClient;

    public MailCheckService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public boolean verifyEmail(String email) {

        try {
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
            String url = BASE_URL + encodedEmail;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return false;
            }

            String body = response.body();

            boolean mx = body.contains("\"mx\": true");
            boolean disposable = body.contains("\"disposable\": true");
            boolean spam = body.contains("\"spam\": true");

            return mx && !disposable && !spam;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}