package com.recruitx.hrone.API;

import com.recruitx.hrone.Models.DisifyResult;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.json.JSONObject;

/**
 * Service for validating emails using Disify API.
 * Fail-safe: if API is unreachable, validation will not block signup.
 */
public class DisifyService {

    private static final String BASE_URL = "https://www.disify.com/api/email/";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public static DisifyResult validateEmail(String email) {

        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + email))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return DisifyResult.apiFailure();
            }

            JSONObject json = new JSONObject(response.body());

            return new DisifyResult(
                    json.optString("address"),
                    json.optBoolean("format"),
                    json.optString("domain"),
                    json.optBoolean("disposable"),
                    json.optBoolean("dns"),
                    json.optBoolean("mx"),
                    false
            );

        } catch (IOException | InterruptedException e) {
            return DisifyResult.apiFailure();
        }
    }
}