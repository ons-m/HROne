package com.recruitx.hrone.API;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArbeitnowJobService {

    private static final String BASE_URL = "https://www.arbeitnow.com/api/job-board-api?page=1";

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    public static SearchResult fetchOffers(String query) {
        List<ExternalJobOffer> offers = new ArrayList<>();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return SearchResult.failure(offers, "HTTP " + response.statusCode());
            }

            JSONObject root = new JSONObject(response.body());
            JSONArray data = root.optJSONArray("data");
            if (data == null) {
                return SearchResult.failure(offers, "Reponse API invalide");
            }

            String normalizedQuery = normalize(query);

            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.optJSONObject(i);
                if (item == null) {
                    continue;
                }

                String title = item.optString("title", "Offre externe").trim();
                String company = item.optString("company_name", "").trim();
                String location = item.optString("location", "").trim();
                String description = item.optString("description", "").trim();
                String applyUrl = item.optString("url", "").trim();
                String source = "arbeitnow.com";
                String employmentType = extractJobTypes(item.optJSONArray("job_types"));

                if (title.isBlank() || applyUrl.isBlank()) {
                    continue;
                }

                ExternalJobOffer offer = new ExternalJobOffer(
                        title,
                        description,
                        applyUrl,
                        source,
                        company,
                        location.isBlank() ? "Europe" : location,
                        employmentType
                );

                if (normalizedQuery.isBlank() || normalize(offer.searchText()).contains(normalizedQuery)) {
                    offers.add(offer);
                }
            }

            return SearchResult.success(offers);

        } catch (IOException ex) {
            return SearchResult.failure(offers, "Connexion impossible");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return SearchResult.failure(offers, "Requete interrompue");
        }
    }

    private static String extractJobTypes(JSONArray jobTypes) {
        if (jobTypes == null || jobTypes.isEmpty()) {
            return "N/A";
        }

        List<String> values = new ArrayList<>();
        for (int i = 0; i < jobTypes.length(); i++) {
            String value = jobTypes.optString(i, "").trim();
            if (!value.isBlank()) {
                values.add(value);
            }
        }

        if (values.isEmpty()) {
            return "N/A";
        }

        return String.join(", ", values);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).trim();
    }

    public static class ExternalJobOffer {
        private final String title;
        private final String description;
        private final String applyUrl;
        private final String sourceWebsite;
        private final String company;
        private final String location;
        private final String employmentType;

        public ExternalJobOffer(
                String title,
                String description,
                String applyUrl,
                String sourceWebsite,
                String company,
                String location,
                String employmentType
        ) {
            this.title = title;
            this.description = description;
            this.applyUrl = applyUrl;
            this.sourceWebsite = sourceWebsite;
            this.company = company;
            this.location = location;
            this.employmentType = employmentType;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getApplyUrl() {
            return applyUrl;
        }

        public String getSourceWebsite() {
            return sourceWebsite;
        }

        public String getCompany() {
            return company;
        }

        public String getLocation() {
            return location;
        }

        public String getEmploymentType() {
            return employmentType;
        }

        public String searchText() {
            return title + " " + company + " " + location + " " + employmentType + " " + description;
        }
    }

    public static class SearchResult {
        private final List<ExternalJobOffer> offers;
        private final boolean success;
        private final String message;

        private SearchResult(List<ExternalJobOffer> offers, boolean success, String message) {
            this.offers = offers == null ? new ArrayList<>() : offers;
            this.success = success;
            this.message = message == null ? "" : message;
        }

        public static SearchResult success(List<ExternalJobOffer> offers) {
            return new SearchResult(offers, true, "OK");
        }

        public static SearchResult failure(List<ExternalJobOffer> offers, String message) {
            return new SearchResult(offers, false, message);
        }

        public List<ExternalJobOffer> getOffers() {
            return offers;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
