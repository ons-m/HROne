package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Utils.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.net.URLEncoder;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class FrmBlog {

    // ==================== FXML EXISTANTS ====================
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private VBox feedContainer;
    @FXML private TextField postTitleField;
    @FXML private TextArea postDescriptionArea;
    @FXML private TextField postImageField;
    @FXML private Button clearPostButton;
    @FXML private Button submitPostButton;
    @FXML private Button chooseImageButton;
    @FXML private ComboBox<String> languageCombo;
    @FXML private Button whatsappBtn;
    @FXML private Label whatsappFooterLabel;
    @FXML private Label appTitleLabel;
    @FXML private Label appSubtitleLabel;
    @FXML private Label heroTitleLabel;
    @FXML private Text heroSubtitleLabel;
    @FXML private Label positiveStatsLabel;
    @FXML private Label neutralStatsLabel;
    @FXML private Label negativeStatsLabel;
    @FXML private Label membersStatsLabel;
    @FXML private Label postsStatsLabel;
    @FXML private Label commentsStatsLabel;
    @FXML private Button tagAllBtn;
    @FXML private Button tagSportBtn;
    @FXML private Button tagCuisineBtn;
    @FXML private Button tagTechBtn;
    @FXML private Button tagRHBtn;
    @FXML private Button tagCultureBtn;
    @FXML private FlowPane popularTagsContainer;
    @FXML private VBox chatContainer;
    @FXML private TextField chatInputField;
    @FXML private Button sendChatBtn;
    @FXML private Button weatherBtn;
    @FXML private Button adviceBtn;
    @FXML private Button quoteBtn;
    @FXML private Button jokeBtn;
    @FXML private Button animalBtn;
    @FXML private Button translateBtn;
    @FXML private Button refreshWeatherBtn;
    @FXML private Label weatherCityLabel;
    @FXML private Label weatherTempLabel;
    @FXML private Label weatherDescLabel;
    @FXML private VBox sidePanel;

    // ==================== CONSTANTES ====================
    private static final String UPLOAD_DIR = "uploads/";
    private static final String WHATSAPP_NUMBER = "21690044054";
    private static final String WHATSAPP_API = "https://wa.me/";

    // APIs
    private static final String WEATHER_API = "https://wttr.in/";
    private static final String JOKE_API = "https://v2.jokeapi.dev/joke/Any?type=single";
    private static final String QUOTE_API = "https://api.quotable.io/random";
    private static final String DOG_API = "https://dog.ceo/api/breeds/image/random";
    private static final String CAT_API = "https://api.thecatapi.com/v1/images/search";
    private static final String ADVICE_API = "https://api.adviceslip.com/advice";
    private static final String TRANSLATION_API = "https://api.mymemory.translated.net/get";

    // ==================== VARIABLES ====================
    private int currentUserId = 2;
    private String currentUserName = "Ons";
    private final List<Node> postCards = new ArrayList<>();
    private String currentLanguage = "fr";
    private String currentTagFilter = "all";

    // Pattern pour validation
    private static final Pattern TITLE_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s\\p{P}]{3,100}$");
    private static final Pattern CONTENT_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s\\p{P}]{3,5000}$");

    // ==================== INITIALIZATION ====================
    @FXML
    private void initialize() {
        createUploadDirectory();
        initializeCurrentUser();
        loadPostsFromDatabase();
        wireSearch();
        wirePostForm();
        wireApiButtons();
        wireChatbot();
        initializeLanguageCombo();
        wireLanguageSelector();
        wireWhatsApp();
        refreshWeather();
        updateStatistics();
        loadPopularTags();

        addChatMessage("🤖 Bonjour ! Je suis votre assistant intelligent. Comment puis-je vous aider ?", true);
        System.out.println("✅ BlogController initialisé avec toutes les fonctionnalités");
    }

    private void initializeCurrentUser() {
        currentUserId = 2;
        currentUserName = "Ons";
    }

    private void initializeLanguageCombo() {
        if (languageCombo != null) {
            languageCombo.getItems().addAll("🇫🇷 Français", "🇬🇧 English", "🇹🇳 العربية");
            languageCombo.setValue("🇫🇷 Français");
        }
    }

    // ==================== API METHODS ====================
    private void wireApiButtons() {
        if (weatherBtn != null) weatherBtn.setOnAction(e -> showWeather());
        if (adviceBtn != null) adviceBtn.setOnAction(e -> showAdvice());
        if (quoteBtn != null) quoteBtn.setOnAction(e -> showRandomQuote());
        if (jokeBtn != null) jokeBtn.setOnAction(e -> showRandomJoke());
        if (animalBtn != null) animalBtn.setOnAction(e -> showRandomAnimal());
        if (translateBtn != null) translateBtn.setOnAction(e -> translateCurrentPost());
        if (refreshWeatherBtn != null) refreshWeatherBtn.setOnAction(e -> refreshWeather());
        System.out.println("✅ API buttons wired");
    }

    private void refreshWeather() {
        CompletableFuture.runAsync(() -> {
            try {
                String city = "Tunis";
                String urlString = WEATHER_API + city + "?format=%c+%t+%w+%m&lang=fr";
                URL url = new URL(urlString);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = in.readLine();
                in.close();

                if (response != null) {
                    String[] parts = response.trim().split("\\s+");
                    Platform.runLater(() -> {
                        if (parts.length >= 2) {
                            weatherCityLabel.setText("Tunis");
                            weatherTempLabel.setText(parts[1]);
                            weatherDescLabel.setText(parts[0].replace("+", "").replace("c", "°C"));
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    weatherCityLabel.setText("Tunis");
                    weatherTempLabel.setText("22°C");
                    weatherDescLabel.setText("Ensoleillé");
                });
            }
        });
    }

    private void showWeather() { refreshWeather(); showInfo("☀️ Météo actualisée", "info"); }

    private void showAdvice() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(ADVICE_API);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = in.readLine();
                in.close();

                String advice = "Conseil du jour";
                if (response != null && response.contains("\"advice\":")) {
                    int start = response.indexOf("\"advice\":\"") + 10;
                    int end = response.indexOf("\"", start);
                    if (start > 10 && end > start) {
                        advice = response.substring(start, end);
                    }
                }

                String finalAdvice = advice;
                Platform.runLater(() -> {
                    showInfo("💡 Conseil: " + finalAdvice, "info");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showInfo("💡 Conseil: Prenez le temps d'écouter vos collègues.", "info");
                });
            }
        });
    }

    private void showRandomQuote() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(QUOTE_API);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = in.readLine();
                in.close();

                String quote = "Le succès n'est pas final, l'échec n'est pas fatal.";
                String author = "Winston Churchill";

                if (response != null && response.contains("\"content\":")) {
                    int start = response.indexOf("\"content\":\"") + 11;
                    int end = response.indexOf("\"", start);
                    if (start > 11 && end > start) {
                        quote = response.substring(start, end);
                    }
                }
                if (response != null && response.contains("\"author\":")) {
                    int start = response.indexOf("\"author\":\"") + 10;
                    int end = response.indexOf("\"", start);
                    if (start > 10 && end > start) {
                        author = response.substring(start, end);
                    }
                }

                String finalQuote = quote;
                String finalAuthor = author;
                Platform.runLater(() -> {
                    showInfo("📝 \"" + finalQuote + "\" - " + finalAuthor, "info");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showInfo("📝 \"Le succès n'est pas final, l'échec n'est pas fatal.\" - Winston Churchill", "info");
                });
            }
        });
    }

    private void showRandomJoke() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(JOKE_API);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = in.readLine();
                in.close();

                String joke = "Pourquoi les programmeurs préfèrent le mode nuit? Parce que la lumière attire les bugs!";

                if (response != null && response.contains("\"joke\":")) {
                    int start = response.indexOf("\"joke\":\"") + 8;
                    int end = response.indexOf("\"", start);
                    if (start > 8 && end > start) {
                        joke = response.substring(start, end);
                    }
                }

                String finalJoke = joke;
                Platform.runLater(() -> {
                    showInfo("😂 " + finalJoke, "info");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showInfo("😂 Pourquoi les programmeurs préfèrent le mode nuit? Parce que la lumière attire les bugs!", "info");
                });
            }
        });
    }

    private void showRandomAnimal() {
        CompletableFuture.runAsync(() -> {
            try {
                boolean isDog = Math.random() > 0.5;
                String apiUrl = isDog ? DOG_API : CAT_API;

                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = in.readLine();
                in.close();

                String imageUrl = "";
                if (isDog && response != null && response.contains("\"message\":")) {
                    int start = response.indexOf("\"message\":\"") + 11;
                    int end = response.indexOf("\"", start);
                    if (start > 11 && end > start) {
                        imageUrl = response.substring(start, end);
                    }
                } else if (!isDog && response != null && response.contains("\"url\":")) {
                    int start = response.indexOf("\"url\":\"") + 7;
                    int end = response.indexOf("\"", start);
                    if (start > 7 && end > start) {
                        imageUrl = response.substring(start, end);
                    }
                }

                if (imageUrl.isEmpty()) {
                    imageUrl = "https://images.dog.ceo/breeds/hound-afghan/n02088094_1003.jpg";
                }

                String finalImageUrl = imageUrl;
                Platform.runLater(() -> {
                    showImagePopup(finalImageUrl, isDog ? "🐕 Chien" : "🐱 Chat");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Image animal non disponible", "error");
                });
            }
        });
    }

    private void showImagePopup(String imageUrl, String title) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(title);

            VBox content = new VBox(10);
            content.setPadding(new Insets(20));

            ImageView imageView = new ImageView();
            imageView.setFitWidth(400);
            imageView.setPreserveRatio(true);

            try {
                Image image = new Image(imageUrl, true);
                imageView.setImage(image);
            } catch (Exception e) {
                imageView.setImage(new Image("https://via.placeholder.com/400x300?text=Image+non+disponible"));
            }

            content.getChildren().add(imageView);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();

        } catch (Exception e) {
            showError("Erreur affichage image", "error");
        }
    }

    private void translateCurrentPost() {
        String text = postDescriptionArea.getText();
        if (text.isEmpty()) {
            text = "Bonjour, comment allez-vous ?";
        }

        String finalText = text;
        CompletableFuture.runAsync(() -> {
            try {
                String encodedText = URLEncoder.encode(finalText, "UTF-8");
                String urlString = TRANSLATION_API + "?q=" + encodedText + "&langpair=fr|en";
                URL url = new URL(urlString);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = in.readLine();
                in.close();

                String translatedText = "[Traduction non disponible]";

                if (response != null && response.contains("\"translatedText\":\"")) {
                    int start = response.indexOf("\"translatedText\":\"") + 18;
                    int end = response.indexOf("\"", start);
                    if (start > 18 && end > start) {
                        translatedText = response.substring(start, end);
                    }
                }

                String finalTranslated = translatedText;
                Platform.runLater(() -> {
                    showTranslatedText(finalText, finalTranslated);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showTranslatedText(finalText, "[Traduction temporairement indisponible]");
                });
            }
        });
    }

    private void showTranslatedText(String original, String translated) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Traduction");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label originalLabel = new Label("📝 Original:");
        originalLabel.setStyle("-fx-font-weight: bold;");
        TextArea originalArea = new TextArea(original);
        originalArea.setEditable(false);
        originalArea.setPrefRowCount(3);

        Label translatedLabel = new Label("🌐 Traduction (anglais):");
        translatedLabel.setStyle("-fx-font-weight: bold;");
        TextArea translatedArea = new TextArea(translated);
        translatedArea.setEditable(false);
        translatedArea.setPrefRowCount(3);

        content.getChildren().addAll(originalLabel, originalArea, translatedLabel, translatedArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    private void suggestTagsFromDescription() {
        String description = postDescriptionArea.getText();
        if (description.isEmpty()) {
            showError("Veuillez d'abord écrire une description", "error");
            return;
        }

        List<String> suggestedTags = new ArrayList<>();
        String lowerDesc = description.toLowerCase();

        if (lowerDesc.contains("sport") || lowerDesc.contains("foot") || lowerDesc.contains("basket") || lowerDesc.contains("football")) {
            suggestedTags.add("Sport");
        }
        if (lowerDesc.contains("cuisine") || lowerDesc.contains("manger") || lowerDesc.contains("recette") || lowerDesc.contains("nourriture")) {
            suggestedTags.add("Cuisine");
        }
        if (lowerDesc.contains("tech") || lowerDesc.contains("ordinateur") || lowerDesc.contains("code") || lowerDesc.contains("informatique")) {
            suggestedTags.add("Tech");
        }
        if (lowerDesc.contains("rh") || lowerDesc.contains("recrutement") || lowerDesc.contains("carrière") || lowerDesc.contains("emploi")) {
            suggestedTags.add("RH");
        }
        if (lowerDesc.contains("culture") || lowerDesc.contains("équipe") || lowerDesc.contains("team") || lowerDesc.contains("entreprise")) {
            suggestedTags.add("Culture");
        }
        if (suggestedTags.isEmpty()) {
            suggestedTags.add("General");
        }

        String tags = String.join(", ", suggestedTags);
        showInfo("Tags suggérés: " + tags, "info");
    }

    // ==================== VALIDATION ====================
    private boolean validatePostInput(String title, String description) {
        if (title == null || title.trim().isEmpty()) {
            showError("Le titre ne peut pas être vide", "error");
            return false;
        }
        if (description == null || description.trim().isEmpty()) {
            showError("La description ne peut pas être vide", "error");
            return false;
        }
        if (title.length() < 3 || title.length() > 100) {
            showError("Le titre doit contenir entre 3 et 100 caractères", "error");
            return false;
        }
        if (description.length() < 3 || description.length() > 5000) {
            showError("La description doit contenir entre 3 et 5000 caractères", "error");
            return false;
        }
        return true;
    }

    private boolean validateCommentInput(String content) {
        if (content == null || content.trim().isEmpty()) {
            showError("Le commentaire ne peut pas être vide", "error");
            return false;
        }
        if (content.length() < 2 || content.length() > 1000) {
            showError("Le commentaire doit contenir entre 2 et 1000 caractères", "error");
            return false;
        }
        return true;
    }

    // ==================== VOTES POUR POSTS ====================
    private void handlePostVote(int postId, Button upvoteBtn, Button downvoteBtn, Label voteCountLabel) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                String checkSql = "SELECT vote_type FROM post_votes WHERE post_id = ? AND user_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, postId);
                    checkStmt.setInt(2, currentUserId);
                    ResultSet rs = checkStmt.executeQuery();

                    if (!rs.next()) {
                        String insertSql = "INSERT INTO post_votes (post_id, user_id, vote_type) VALUES (?, ?, 'up')";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, postId);
                            insertStmt.setInt(2, currentUserId);
                            insertStmt.executeUpdate();
                        }

                        Platform.runLater(() -> {
                            upvoteBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                            downvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");
                        });
                    } else {
                        String existingVote = rs.getString("vote_type");
                        if (existingVote.equals("up")) {
                            String deleteSql = "DELETE FROM post_votes WHERE post_id = ? AND user_id = ?";
                            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                                deleteStmt.setInt(1, postId);
                                deleteStmt.setInt(2, currentUserId);
                                deleteStmt.executeUpdate();
                            }

                            Platform.runLater(() -> {
                                upvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");
                                downvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");
                            });
                        }
                    }
                }

                String countSql = "SELECT COUNT(*) as count FROM post_votes WHERE post_id = ? AND vote_type = 'up'";
                try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                    countStmt.setInt(1, postId);
                    ResultSet countRs = countStmt.executeQuery();
                    if (countRs.next()) {
                        int newCount = countRs.getInt("count");
                        Platform.runLater(() -> voteCountLabel.setText(String.valueOf(newCount)));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Erreur lors du vote", "error"));
            }
        });
    }

    private void handleDownVote(int postId, Button upvoteBtn, Button downvoteBtn, Label voteCountLabel) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                String checkSql = "SELECT vote_type FROM post_votes WHERE post_id = ? AND user_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, postId);
                    checkStmt.setInt(2, currentUserId);
                    ResultSet rs = checkStmt.executeQuery();

                    if (!rs.next()) {
                        String insertSql = "INSERT INTO post_votes (post_id, user_id, vote_type) VALUES (?, ?, 'down')";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, postId);
                            insertStmt.setInt(2, currentUserId);
                            insertStmt.executeUpdate();
                        }

                        Platform.runLater(() -> {
                            upvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");
                            downvoteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                        });
                    } else {
                        String existingVote = rs.getString("vote_type");
                        if (existingVote.equals("down")) {
                            String deleteSql = "DELETE FROM post_votes WHERE post_id = ? AND user_id = ?";
                            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                                deleteStmt.setInt(1, postId);
                                deleteStmt.setInt(2, currentUserId);
                                deleteStmt.executeUpdate();
                            }

                            Platform.runLater(() -> {
                                upvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");
                                downvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");
                            });
                        } else {
                            String updateSql = "UPDATE post_votes SET vote_type = 'down' WHERE post_id = ? AND user_id = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                                updateStmt.setInt(1, postId);
                                updateStmt.setInt(2, currentUserId);
                                updateStmt.executeUpdate();
                            }

                            Platform.runLater(() -> {
                                upvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");
                                downvoteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                            });
                        }
                    }
                }

                String countSql = "SELECT COUNT(*) as count FROM post_votes WHERE post_id = ? AND vote_type = 'up'";
                try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                    countStmt.setInt(1, postId);
                    ResultSet countRs = countStmt.executeQuery();
                    if (countRs.next()) {
                        int newCount = countRs.getInt("count");
                        Platform.runLater(() -> voteCountLabel.setText(String.valueOf(newCount)));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Erreur lors du vote", "error"));
            }
        });
    }

    // ==================== STATISTIQUES ====================
    private void updateStatistics() {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                int members = 0;
                String membersSql = "SELECT COUNT(*) as count FROM utilisateur";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(membersSql)) {
                    if (rs.next()) members = rs.getInt("count");
                }

                int posts = 0;
                String postsSql = "SELECT COUNT(*) as count FROM posts WHERE is_active = true";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(postsSql)) {
                    if (rs.next()) posts = rs.getInt("count");
                }

                int comments = 0;
                String commentsSql = "SELECT COUNT(*) as count FROM comments WHERE is_active = true";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(commentsSql)) {
                    if (rs.next()) comments = rs.getInt("count");
                }

                int positive = 0;
                String positiveSql = "SELECT COUNT(*) as count FROM post_votes WHERE vote_type = 'up'";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(positiveSql)) {
                    if (rs.next()) positive = rs.getInt("count");
                }

                int negative = 0;
                String negativeSql = "SELECT COUNT(*) as count FROM post_votes WHERE vote_type = 'down'";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(negativeSql)) {
                    if (rs.next()) negative = rs.getInt("count");
                }

                int neutral = (posts * 3) - positive - negative;

                int finalMembers = members;
                int finalPosts = posts;
                int finalComments = comments;
                int finalPositive = positive;
                int finalNeutral = neutral > 0 ? neutral : 0;
                int finalNegative = negative;

                Platform.runLater(() -> {
                    if (membersStatsLabel != null) membersStatsLabel.setText(String.valueOf(finalMembers));
                    if (postsStatsLabel != null) postsStatsLabel.setText(String.valueOf(finalPosts));
                    if (commentsStatsLabel != null) commentsStatsLabel.setText(String.valueOf(finalComments));
                    if (positiveStatsLabel != null) positiveStatsLabel.setText(String.valueOf(finalPositive));
                    if (neutralStatsLabel != null) neutralStatsLabel.setText(String.valueOf(finalNeutral));
                    if (negativeStatsLabel != null) negativeStatsLabel.setText(String.valueOf(finalNegative));
                });

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    // ==================== FILTRAGE PAR TAGS ====================
    @FXML
    private void filterByTag(javafx.event.ActionEvent event) {
        Button sourceBtn = (Button) event.getSource();
        String tag = sourceBtn.getText().replace("#", "").trim();

        if (tag.equals("Tous") || tag.equals("All") || tag.equals("الكل")) {
            currentTagFilter = "all";
        } else {
            currentTagFilter = tag;
        }

        applyTagFilter();
    }

    private void applyTagFilter() {
        for (Node postCard : postCards) {
            boolean show = false;
            if (postCard instanceof VBox) {
                for (Node child : ((VBox) postCard).getChildren()) {
                    if (child instanceof Label && child.getStyleClass().contains("post-tag")) {
                        String tagText = ((Label) child).getText().replace("#", "").trim();
                        if (currentTagFilter.equals("all") || tagText.equalsIgnoreCase(currentTagFilter)) {
                            show = true;
                        }
                        break;
                    }
                }
            }
            postCard.setVisible(show);
            postCard.setManaged(show);
        }
    }

    private void loadPopularTags() {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                String sql = "SELECT tag, COUNT(*) as count FROM posts WHERE is_active = true GROUP BY tag ORDER BY count DESC LIMIT 10";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    List<Map<String, Object>> tags = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> tag = new HashMap<>();
                        tag.put("name", rs.getString("tag"));
                        tag.put("count", rs.getInt("count"));
                        tags.add(tag);
                    }

                    Platform.runLater(() -> {
                        if (popularTagsContainer != null) {
                            popularTagsContainer.getChildren().clear();
                            for (Map<String, Object> tag : tags) {
                                String tagName = (String) tag.get("name");
                                int count = (int) tag.get("count");

                                Button tagBtn = new Button("#" + tagName + " (" + count + ")");
                                tagBtn.getStyleClass().add("popular-tag");
                                tagBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;");

                                tagBtn.setOnAction(e -> {
                                    currentTagFilter = tagName;
                                    applyTagFilter();
                                });

                                popularTagsContainer.getChildren().add(tagBtn);
                            }
                        }
                    });
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    // ==================== CHATBOT INTELLIGENT ====================
    private void wireChatbot() {
        if (sendChatBtn != null) sendChatBtn.setOnAction(e -> sendChatMessage());
        if (chatInputField != null) chatInputField.setOnAction(e -> sendChatMessage());
    }

    @FXML
    private void chatSuggestion(javafx.event.ActionEvent event) {
        Button source = (Button) event.getSource();
        chatInputField.setText(source.getText());
        sendChatMessage();
    }

    private void sendChatMessage() {
        String message = chatInputField.getText().trim();
        if (message.isEmpty()) return;

        addChatMessage("👤 " + message, false);
        chatInputField.clear();

        CompletableFuture.runAsync(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
            String response = getIntelligentBotResponse(message);
            Platform.runLater(() -> addChatMessage("🤖 " + response, true));
        });
    }

    private void addChatMessage(String text, boolean isBot) {
        Label msg = new Label(text);
        msg.setWrapText(true);
        msg.setStyle(isBot ?
                "-fx-background-color: #2a5f9f; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 20 20 20 5; -fx-font-size: 13px;" :
                "-fx-background-color: #1a3f6f; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 20 20 5 20; -fx-font-size: 13px;");
        msg.setMaxWidth(280);

        HBox messageBox = new HBox(msg);
        messageBox.setAlignment(isBot ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(2, 5, 2, 5));

        Platform.runLater(() -> {
            chatContainer.getChildren().add(messageBox);
            if (chatContainer.getChildren().size() > 50) chatContainer.getChildren().remove(0);

            if (chatContainer.getParent() instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) chatContainer.getParent();
                scrollPane.setVvalue(1.0);
            }
        });
    }

    private String getIntelligentBotResponse(String message) {
        message = message.toLowerCase();

        Map<String, String[]> responses = new HashMap<>();
        responses.put("bonjour|salut|hello|hi|salam", new String[]{
                "Bonjour ! Comment puis-je vous aider aujourd'hui ?",
                "Salut ! Ravi de vous voir !",
                "Bonjour ! Que puis-je faire pour vous ?"
        });

        responses.put("météo|temps|temperature|weather", new String[]{
                "La météo actuelle à " + (weatherCityLabel != null ? weatherCityLabel.getText() : "Tunis") +
                        " est " + (weatherTempLabel != null ? weatherTempLabel.getText() : "22°C") +
                        " avec " + (weatherDescLabel != null ? weatherDescLabel.getText() : "ensoleillé"),
                "Il fait " + (weatherTempLabel != null ? weatherTempLabel.getText() : "22°C") +
                        " à " + (weatherCityLabel != null ? weatherCityLabel.getText() : "Tunis") +
                        ". " + (weatherDescLabel != null ? weatherDescLabel.getText() : "Beau temps")
        });

        responses.put("post|publier|créer|create", new String[]{
                "Pour créer un post, remplissez le formulaire avec un titre et une description, puis cliquez sur 'Publier'.",
                "Vous pouvez partager vos idées en utilisant le formulaire de création de post."
        });

        responses.put("commentaire|comment|répondre|reply", new String[]{
                "Pour commenter, cliquez sur le bouton 💬 sous un post, écrivez votre message et appuyez sur Entrée.",
                "Les commentaires permettent d'échanger avec la communauté. N'hésitez pas à participer !"
        });

        responses.put("tag|étiquette|categorie|category", new String[]{
                "Utilisez des tags comme #Sport, #Cuisine, #Tech pour catégoriser vos posts.",
                "Les tags aident à organiser le contenu. Cliquez sur un tag pour filtrer les posts."
        });

        responses.put("vote|like|dislike|upvote|downvote", new String[]{
                "Vous pouvez voter pour les posts avec les boutons 👍 et 👎.",
                "Les votes aident à mettre en avant le contenu de qualité."
        });

        responses.put("statistiques|stats|statistics", new String[]{
                "📊 Statistiques actuelles:\n• Membres: " + (membersStatsLabel != null ? membersStatsLabel.getText() : "0") +
                        "\n• Posts: " + (postsStatsLabel != null ? postsStatsLabel.getText() : "0") +
                        "\n• Commentaires: " + (commentsStatsLabel != null ? commentsStatsLabel.getText() : "0")
        });

        responses.put("blague|joke|rire|humour", new String[]{
                "Pour une blague, cliquez sur le bouton '😂 Blague' dans la section APIs !",
                "Je ne suis pas très drôle, mais le bouton '😂 Blague' vous fera rire !"
        });

        responses.put("citation|quote|inspiration", new String[]{
                "Le bouton '📝 Citation' vous donnera une citation inspirante.",
                "Besoin d'inspiration ? Utilisez le bouton '📝 Citation' !"
        });

        responses.put("whatsapp|whats up|wa", new String[]{
                "Pour partager sur WhatsApp, cliquez sur le bouton WhatsApp ou sur le numéro dans le footer !",
                "Vous pouvez nous contacter sur WhatsApp au " + WHATSAPP_NUMBER
        });

        responses.put("aide|help|what can you do|que fais tu", new String[]{
                "Je peux vous aider avec :\n• Créer des posts\n• Commenter\n• Voter\n• Voir la météo\n• Obtenir des blagues/citations\n• Filtrer par tags\n• Statistiques\n• Traduction\n• Partage WhatsApp\n• Et plus encore !"
        });

        responses.put("traduction|translate|anglais|english|arabe|arabic", new String[]{
                "Utilisez le sélecteur de langue en haut pour changer l'interface en Français, English ou العربية.",
                "Pour traduire un post, sélectionnez la langue souhaitée dans le menu déroulant."
        });

        responses.put("merci|thanks|thank you|شكرا", new String[]{
                "Avec plaisir ! N'hésitez pas si vous avez d'autres questions.",
                "Je suis là pour vous aider !"
        });

        responses.put("au revoir|bye|à bientôt|مع السلامة", new String[]{
                "Au revoir ! Passez une excellente journée !",
                "À bientôt sur HR One !"
        });

        for (Map.Entry<String, String[]> entry : responses.entrySet()) {
            String[] keywords = entry.getKey().split("\\|");
            for (String keyword : keywords) {
                if (message.contains(keyword)) {
                    String[] possibleResponses = entry.getValue();
                    return possibleResponses[new Random().nextInt(possibleResponses.length)];
                }
            }
        }

        String[] defaultResponses = {
                "Intéressant ! Pouvez-vous être plus précis ?",
                "Dites-moi en plus, je suis là pour vous aider.",
                "Je n'ai pas bien compris. Essayez avec 'aide' pour voir ce que je peux faire.",
                "Pouvez-vous reformuler votre question ?"
        };

        return defaultResponses[new Random().nextInt(defaultResponses.length)];
    }

    // ==================== TRADUCTION MULTILINGUE ====================
    private void wireLanguageSelector() {
        if (languageCombo != null) languageCombo.setOnAction(e -> changeLanguage());
    }

    private void changeLanguage() {
        String selected = languageCombo.getValue();
        if (selected.contains("Français")) {
            currentLanguage = "fr";
            appTitleLabel.setText("HR One");
            appSubtitleLabel.setText("Forum des employés");
            heroTitleLabel.setText("✨ Partagez vos idées ✨");
            heroSubtitleLabel.setText("Postez des suggestions, posez des questions et animez la conversation.");
            searchField.setPromptText("🔍 Rechercher par mots-clés ou tags...");
            searchButton.setText("🔍 Rechercher");
            tagAllBtn.setText("Tous");
            tagSportBtn.setText("#Sport");
            tagCuisineBtn.setText("#Cuisine");
            tagTechBtn.setText("#Tech");
            tagRHBtn.setText("#RH");
            tagCultureBtn.setText("#Culture");
        } else if (selected.contains("English")) {
            currentLanguage = "en";
            appTitleLabel.setText("HR One");
            appSubtitleLabel.setText("Employee Forum");
            heroTitleLabel.setText("✨ Share Your Ideas ✨");
            heroSubtitleLabel.setText("Post suggestions, ask questions, and keep the conversation moving.");
            searchField.setPromptText("🔍 Search by keywords or tags...");
            searchButton.setText("🔍 Search");
            tagAllBtn.setText("All");
            tagSportBtn.setText("#Sport");
            tagCuisineBtn.setText("#Cooking");
            tagTechBtn.setText("#Tech");
            tagRHBtn.setText("#HR");
            tagCultureBtn.setText("#Culture");
        } else if (selected.contains("العربية")) {
            currentLanguage = "ar";
            appTitleLabel.setText("HR One");
            appSubtitleLabel.setText("منتدى الموظفين");
            heroTitleLabel.setText("✨ شارك أفكارك ✨");
            heroSubtitleLabel.setText("انشر اقتراحاتك، اطرح أسئلة وحافظ على استمرار المحادثة.");
            searchField.setPromptText("🔍 ابحث بالكلمات الرئيسية أو الوسوم...");
            searchButton.setText("🔍 بحث");
            tagAllBtn.setText("الكل");
            tagSportBtn.setText("#رياضة");
            tagCuisineBtn.setText("#طبخ");
            tagTechBtn.setText("#تقنية");
            tagRHBtn.setText("#موارد_بشرية");
            tagCultureBtn.setText("#ثقافة");
        }
        loadPostsFromDatabase();
    }

    // ==================== WHATSAPP ====================
    private void wireWhatsApp() {
        if (whatsappBtn != null) whatsappBtn.setOnAction(e -> openWhatsApp());
        if (whatsappFooterLabel != null) whatsappFooterLabel.setOnMouseClicked(e -> openWhatsApp());
    }

    @FXML
    private void openWhatsApp() {
        try {
            String url = WHATSAPP_API + WHATSAPP_NUMBER;
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            showError("Impossible d'ouvrir WhatsApp", "error");
        }
    }

    private void sharePostOnWhatsApp(String postTitle, String postDescription) {
        try {
            String message = "📝 *" + postTitle + "*\n\n" + postDescription + "\n\nPartagé depuis HR One";
            String encodedMessage = URLEncoder.encode(message, "UTF-8");
            String url = WHATSAPP_API + WHATSAPP_NUMBER + "?text=" + encodedMessage;
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            showError("Erreur lors du partage WhatsApp", "error");
        }
    }

    // ==================== CRUD POSTS ====================

    // CREATE - Déjà existant
    @FXML
    private void createPost() {
        String title = postTitleField.getText().trim();
        String description = postDescriptionArea.getText().trim();
        String imagePath = postImageField.getText().trim();

        if (!validatePostInput(title, description)) return;

        String tag = suggestTagFromDescription(description);

        String sql = "INSERT INTO posts (user_id, title, description, image_url, tag, is_active, created_at) VALUES (?, ?, ?, ?, ?, true, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, currentUserId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setString(4, imagePath.isEmpty() ? null : imagePath);
            stmt.setString(5, tag);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                Platform.runLater(() -> {
                    loadPostsFromDatabase();
                    clearPostForm();
                    showInfo("✅ Post créé avec succès !", "success");
                    updateStatistics();
                    loadPopularTags();
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la création: " + e.getMessage(), "error");
        }
    }

    // UPDATE Post
    private void updatePost(int postId, String title, String description, String imagePath, String tag) {
        String sql = "UPDATE posts SET title = ?, description = ?, image_url = ?, tag = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, imagePath.isEmpty() ? null : imagePath);
            stmt.setString(4, tag);
            stmt.setInt(5, postId);
            stmt.setInt(6, currentUserId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                Platform.runLater(() -> {
                    loadPostsFromDatabase();
                    showInfo("✅ Post mis à jour avec succès !", "success");
                });
            } else {
                showError("Vous n'êtes pas autorisé à modifier ce post", "error");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la mise à jour: " + e.getMessage(), "error");
        }
    }

    // DELETE Post (soft delete)
    private void deletePost(int postId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le post");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce post ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE posts SET is_active = false WHERE id = ? AND user_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, postId);
                stmt.setInt(2, currentUserId);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    Platform.runLater(() -> {
                        loadPostsFromDatabase();
                        showInfo("✅ Post supprimé avec succès !", "success");
                        updateStatistics();
                        loadPopularTags();
                    });
                } else {
                    showError("Vous n'êtes pas autorisé à supprimer ce post", "error");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur lors de la suppression: " + e.getMessage(), "error");
            }
        }
    }

    // ==================== CRUD COMMENTAIRES ====================

    // CREATE Comment - Déjà existant
    private void addComment(int postId, String content, Integer parentCommentId, VBox commentsSection) {
        if (!validateCommentInput(content)) return;

        String sql = "INSERT INTO comments (post_id, user_id, parent_comment_id, content, is_active, created_at) VALUES (?, ?, ?, ?, true, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, postId);
            stmt.setInt(2, currentUserId);
            if (parentCommentId != null) {
                stmt.setInt(3, parentCommentId);
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, content);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                Platform.runLater(() -> refreshCommentsSection(postId));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ajout du commentaire", "error");
        }
    }

    // UPDATE Comment
    private void updateComment(int commentId, String newContent) {
        String sql = "UPDATE comments SET content = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newContent);
            stmt.setInt(2, commentId);
            stmt.setInt(3, currentUserId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                Platform.runLater(() -> {
                    refreshAllComments();
                    showInfo("✅ Commentaire mis à jour", "success");
                });
            } else {
                showError("Vous n'êtes pas autorisé à modifier ce commentaire", "error");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la mise à jour", "error");
        }
    }

    // DELETE Comment (soft delete)
    private void deleteComment(int commentId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le commentaire");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce commentaire ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE comments SET is_active = false WHERE id = ? AND user_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, commentId);
                stmt.setInt(2, currentUserId);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    Platform.runLater(() -> {
                        refreshAllComments();
                        showInfo("✅ Commentaire supprimé", "success");
                        updateStatistics();
                    });
                } else {
                    showError("Vous n'êtes pas autorisé à supprimer ce commentaire", "error");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur lors de la suppression", "error");
            }
        }
    }

    // ==================== MÉTHODES UTILITAIRES POUR CRUD ====================

    private void refreshCommentsSection(int postId) {
        for (Node node : feedContainer.getChildren()) {
            if (node instanceof VBox && node.getId() != null && node.getId().equals("post-" + postId)) {
                VBox postCard = (VBox) node;
                for (int i = 0; i < postCard.getChildren().size(); i++) {
                    Node child = postCard.getChildren().get(i);
                    if (child instanceof VBox && child.getId() != null && child.getId().equals("comments-" + postId)) {
                        VBox oldSection = (VBox) child;
                        boolean wasVisible = oldSection.isVisible();

                        VBox newCommentsSection = new VBox(10);
                        newCommentsSection.getStyleClass().add("comments-section");
                        newCommentsSection.setId("comments-" + postId);
                        newCommentsSection.setVisible(wasVisible);
                        newCommentsSection.setManaged(wasVisible);
                        newCommentsSection.setPadding(new Insets(15, 0, 0, 0));
                        newCommentsSection.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 20; -fx-padding: 20; -fx-spacing: 15; -fx-border-color: #b0d0ff; -fx-border-width: 2; -fx-border-radius: 20;");

                        HBox commentForm = buildCommentForm(postId, newCommentsSection);
                        newCommentsSection.getChildren().add(commentForm);

                        loadCommentsForPost(postId, newCommentsSection);
                        postCard.getChildren().set(i, newCommentsSection);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void refreshAllComments() {
        for (Node node : feedContainer.getChildren()) {
            if (node instanceof VBox && node.getId() != null && node.getId().startsWith("post-")) {
                String postIdStr = node.getId().replace("post-", "");
                try {
                    int postId = Integer.parseInt(postIdStr);
                    refreshCommentsSection(postId);
                } catch (NumberFormatException e) {
                    // Ignorer
                }
            }
        }
    }

    private String suggestTagFromDescription(String description) {
        String lower = description.toLowerCase();
        if (lower.contains("sport") || lower.contains("foot") || lower.contains("basket") || lower.contains("football")) return "Sport";
        if (lower.contains("cuisine") || lower.contains("manger") || lower.contains("recette") || lower.contains("nourriture")) return "Cuisine";
        if (lower.contains("tech") || lower.contains("ordinateur") || lower.contains("code") || lower.contains("informatique")) return "Tech";
        if (lower.contains("rh") || lower.contains("recrutement") || lower.contains("carrière") || lower.contains("emploi")) return "RH";
        if (lower.contains("culture") || lower.contains("équipe") || lower.contains("team") || lower.contains("entreprise")) return "Culture";
        return "General";
    }

    // ==================== CRÉATION DU FORMULAIRE DE POST ====================
    private Node createPostForm() {
        VBox formCard = new VBox();
        formCard.getStyleClass().addAll("panel-card", "post-form-card");
        formCard.setPadding(new Insets(20));
        formCard.setSpacing(15);
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,30,60,0.2), 20, 0, 0, 8); -fx-border-color: #c0d4f0; -fx-border-width: 1; -fx-border-radius: 20;");

        Label title = new Label("Créer un post");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0a2a4a;");

        Label subtitle = new Label("Partagez un titre, décrivez votre idée et ajoutez une image optionnelle.");
        subtitle.setStyle("-fx-text-fill: #4a6a8a; -fx-font-size: 13px;");
        subtitle.setWrapText(true);

        VBox form = new VBox();
        form.setSpacing(15);

        VBox titleField = new VBox(5);
        Label titleLabel = new Label("Titre");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a3a5a;");
        TextField titleInput = new TextField();
        titleInput.setPromptText("Ex: Améliorer l'onboarding des nouveaux");
        titleInput.setId("postTitleField");
        titleInput.setStyle("-fx-background-color: #f5f9ff; -fx-padding: 12; -fx-background-radius: 10; -fx-border-color: #c0d0e8; -fx-border-width: 1; -fx-border-radius: 10;");
        titleField.getChildren().addAll(titleLabel, titleInput);

        VBox descField = new VBox(5);
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a3a5a;");
        TextArea descInput = new TextArea();
        descInput.setPromptText("Décrivez votre idée ou feedback...");
        descInput.setPrefRowCount(4);
        descInput.setId("postDescriptionArea");
        descInput.setStyle("-fx-background-color: #f5f9ff; -fx-padding: 12; -fx-background-radius: 10; -fx-border-color: #c0d0e8; -fx-border-width: 1; -fx-border-radius: 10; -fx-font-size: 14px;");
        descField.getChildren().addAll(descLabel, descInput);

        VBox imageField = new VBox(5);
        Label imageLabel = new Label("Image (optionnelle)");
        imageLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a3a5a;");
        HBox imageBox = new HBox(8);
        TextField imageInput = new TextField();
        imageInput.setPromptText("Aucun fichier choisi");
        imageInput.setId("postImageField");
        imageInput.setPrefWidth(300);
        imageInput.setEditable(false);
        imageInput.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #c0d0e8; -fx-border-width: 1; -fx-border-radius: 5;");

        Button chooseBtn = new Button("Choisir Image");
        chooseBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        chooseBtn.setOnAction(e -> chooseImage(imageInput));

        Button randomImageBtn = new Button("🎲 Image aléatoire");
        randomImageBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        randomImageBtn.setOnAction(e -> getRandomImage(imageInput));

        imageBox.getChildren().addAll(imageInput, chooseBtn, randomImageBtn);
        imageField.getChildren().addAll(imageLabel, imageBox);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button suggestTagsBtn = new Button("🏷️ Suggérer tags");
        suggestTagsBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        suggestTagsBtn.setOnAction(e -> suggestTagsFromDescription());

        Button clearBtn = new Button("Effacer");
        clearBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        clearBtn.setId("clearPostButton");

        Button submitBtn = new Button("Publier");
        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        submitBtn.setId("submitPostButton");

        actions.getChildren().addAll(suggestTagsBtn, clearBtn, submitBtn);

        form.getChildren().addAll(titleField, descField, imageField, actions);
        formCard.getChildren().addAll(title, subtitle, form);

        postTitleField = titleInput;
        postDescriptionArea = descInput;
        postImageField = imageInput;
        chooseImageButton = chooseBtn;
        clearPostButton = clearBtn;
        submitPostButton = submitBtn;

        return formCard;
    }

    private void chooseImage(TextField imageField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                Path destPath = Path.of(UPLOAD_DIR, fileName);
                Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                imageField.setText("uploads/" + fileName);
                showInfo("✅ Image téléchargée avec succès!", "success");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur lors de l'upload: " + e.getMessage(), "error");
            }
        }
    }

    private void getRandomImage(TextField imageField) {
        try {
            String fileName = "random_" + System.currentTimeMillis() + ".jpg";
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            String imageUrl = "https://picsum.photos/600/400?random=" + System.currentTimeMillis();

            URL url = new URL(imageUrl);
            Path destPath = Path.of(UPLOAD_DIR, fileName);
            Files.copy(url.openStream(), destPath, StandardCopyOption.REPLACE_EXISTING);

            imageField.setText("uploads/" + fileName);
            showInfo("✅ Image aléatoire téléchargée!", "success");
        } catch (Exception e) {
            showError("Erreur téléchargement image: " + e.getMessage(), "error");
        }
    }

    // ==================== CHARGEMENT DES POSTS ====================
    private void loadPostsFromDatabase() {
        feedContainer.getChildren().clear();
        postCards.clear();

        Node postForm = createPostForm();
        if (postForm != null) {
            feedContainer.getChildren().add(postForm);
        }

        String sql = "SELECT p.*, u.Nom_Utilisateur as author_name, " +
                "(SELECT COUNT(*) FROM post_votes WHERE post_id = p.id AND vote_type = 'up') as upvotes " +
                "FROM posts p " +
                "JOIN utilisateur u ON p.user_id = u.ID_UTILISATEUR " +
                "WHERE p.is_active = true " +
                "ORDER BY p.created_at DESC";

        List<Map<String, Object>> postsData = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("=== CHARGEMENT DES POSTS ===");

            while (rs.next()) {
                Map<String, Object> post = new HashMap<>();
                post.put("id", rs.getInt("id"));
                post.put("title", rs.getString("title"));
                post.put("description", rs.getString("description"));
                post.put("image_url", rs.getString("image_url"));
                post.put("tag", rs.getString("tag"));
                post.put("upvotes", rs.getInt("upvotes"));
                post.put("author_name", rs.getString("author_name"));
                post.put("created_at", rs.getTimestamp("created_at"));
                post.put("user_id", rs.getInt("user_id"));
                postsData.add(post);
            }

            System.out.println("Nombre de posts trouvés: " + postsData.size());

            for (Map<String, Object> postData : postsData) {
                VBox postCard = buildPostCardFromData(postData);
                if (postCard != null) {
                    feedContainer.getChildren().add(postCard);
                    postCards.add(postCard);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement: " + e.getMessage(), "error");
        }
    }

    private VBox buildPostCardFromData(Map<String, Object> postData) {
        try {
            int postId = (int) postData.get("id");
            String title = (String) postData.get("title");
            String description = (String) postData.get("description");
            String imageUrl = (String) postData.get("image_url");
            String tag = (String) postData.get("tag");
            int upvotes = (int) postData.get("upvotes");
            String author = (String) postData.get("author_name");
            Timestamp createdAt = (Timestamp) postData.get("created_at");
            int userId = (int) postData.get("user_id");

            VBox card = new VBox();
            card.getStyleClass().add("post-card");
            card.setId("post-" + postId);
            card.setSpacing(12);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20; -fx-spacing: 15; -fx-effect: dropshadow(gaussian, rgba(0,30,60,0.15), 15, 0, 0, 5); -fx-border-color: #d0e0f5; -fx-border-width: 0 0 0 5; -fx-border-radius: 20;");

            // En-tête avec auteur et date
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setStyle("-fx-background-color: #f5f9ff; -fx-padding: 10; -fx-background-radius: 12; -fx-border-color: #d0e0f0; -fx-border-width: 1; -fx-border-radius: 12;");

            Label authorLabel = new Label(author);
            authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0a2a4a;");

            Label timeLabel = new Label(formatTimeAgo(createdAt.toLocalDateTime()));
            timeLabel.setStyle("-fx-text-fill: #5a7a9a; -fx-font-size: 12px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Boutons d'action pour le post (Modifier/Supprimer) - uniquement si c'est l'auteur
            if (userId == currentUserId) {
                Button editBtn = new Button("✏️");
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 14px;");
                editBtn.setTooltip(new Tooltip("Modifier ce post"));
                editBtn.setOnAction(e -> showEditPostDialog(postId, title, description, imageUrl, tag));

                Button deleteBtn = new Button("🗑️");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 14px;");
                deleteBtn.setTooltip(new Tooltip("Supprimer ce post"));
                deleteBtn.setOnAction(e -> deletePost(postId));

                header.getChildren().addAll(authorLabel, timeLabel, spacer, editBtn, deleteBtn);
            } else {
                header.getChildren().addAll(authorLabel, timeLabel, spacer);
            }

            // Bouton WhatsApp pour partager ce post
            Button shareWhatsAppBtn = new Button("📱");
            shareWhatsAppBtn.setStyle("-fx-background-color: #25D366; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 14px; -fx-margin: 0 5 0 0;");
            shareWhatsAppBtn.setTooltip(new Tooltip("Partager sur WhatsApp"));
            shareWhatsAppBtn.setOnAction(e -> sharePostOnWhatsApp(title, description));
            header.getChildren().add(shareWhatsAppBtn);

            // Tag
            Label tagLabel = new Label("#" + tag);
            tagLabel.getStyleClass().add("post-tag");
            tagLabel.setStyle("-fx-background-color: #e0edff; -fx-text-fill: #0a3a6a; -fx-padding: 5 15; -fx-background-radius: 30; -fx-font-size: 12px; -fx-font-weight: bold; -fx-border-color: #7aa5d9; -fx-border-width: 1; -fx-border-radius: 30;");

            // Titre
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0a2a4a; -fx-wrap-text: true;");

            // Description
            Label descLabel = new Label(description);
            descLabel.setWrapText(true);
            descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2a3a4a; -fx-wrap-text: true; -fx-line-spacing: 3; -fx-padding: 5 0;");

            // Image (si présente)
            ImageView imageView = null;
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    imageView = new ImageView();
                    File imgFile = new File(imageUrl);
                    Image image;
                    if (imgFile.exists()) {
                        image = new Image(imgFile.toURI().toString());
                    } else {
                        image = new Image(imageUrl, true);
                    }
                    imageView.setImage(image);
                    imageView.setFitWidth(600);
                    imageView.setPreserveRatio(true);
                    imageView.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #d0e0f0; -fx-border-width: 1;");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Section votes
            HBox voteBox = new HBox(5);
            voteBox.setAlignment(Pos.CENTER_LEFT);
            voteBox.getStyleClass().add("vote-container");
            voteBox.setStyle("-fx-background-color: #edf3fc; -fx-background-radius: 30; -fx-padding: 5; -fx-spacing: 8; -fx-border-color: #b8d4ff; -fx-border-width: 1; -fx-border-radius: 30;");

            Button upvoteBtn = new Button("👍");
            upvoteBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 25; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");

            Label voteCount = new Label(String.valueOf(upvotes));
            voteCount.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0a2a4a; -fx-padding: 5 10;");

            Button downvoteBtn = new Button("👎");
            downvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black; -fx-padding: 8 20; -fx-background-radius: 25; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");

            upvoteBtn.setOnAction(e -> handlePostVote(postId, upvoteBtn, downvoteBtn, voteCount));
            downvoteBtn.setOnAction(e -> handleDownVote(postId, upvoteBtn, downvoteBtn, voteCount));

            voteBox.getChildren().addAll(upvoteBtn, voteCount, downvoteBtn);

            // Bouton commentaire
            Button commentBtn = new Button("💬 Commenter");
            commentBtn.getStyleClass().add("btn-secondary");
            commentBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 25; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
            commentBtn.setOnAction(e -> toggleComments(card));

            // Section commentaires (cachée par défaut)
            VBox commentsSection = new VBox(10);
            commentsSection.getStyleClass().add("comments-section");
            commentsSection.setId("comments-" + postId);
            commentsSection.setVisible(false);
            commentsSection.setManaged(false);
            commentsSection.setPadding(new Insets(15, 0, 0, 0));
            commentsSection.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 20; -fx-padding: 20; -fx-spacing: 15; -fx-border-color: #b0d0ff; -fx-border-width: 2; -fx-border-radius: 20;");

            // Formulaire pour ajouter un commentaire
            HBox commentForm = buildCommentForm(postId, commentsSection);
            commentsSection.getChildren().add(commentForm);

            // Charger les commentaires existants
            loadCommentsForPost(postId, commentsSection);

            // Assemblage de la carte
            card.getChildren().add(header);
            card.getChildren().add(tagLabel);
            card.getChildren().add(titleLabel);
            card.getChildren().add(descLabel);
            if (imageView != null) card.getChildren().add(imageView);
            card.getChildren().add(voteBox);
            card.getChildren().add(commentBtn);
            card.getChildren().add(commentsSection);

            return card;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Dialog pour modifier un post
    private void showEditPostDialog(int postId, String currentTitle, String currentDescription, String currentImageUrl, String currentTag) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier le post");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TextField titleField = new TextField(currentTitle);
        titleField.setPromptText("Titre");
        titleField.setStyle("-fx-padding: 10; -fx-background-radius: 5;");

        TextArea descArea = new TextArea(currentDescription);
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(5);
        descArea.setStyle("-fx-padding: 10; -fx-background-radius: 5;");

        TextField tagField = new TextField(currentTag);
        tagField.setPromptText("Tag");
        tagField.setStyle("-fx-padding: 10; -fx-background-radius: 5;");

        TextField imageField = new TextField(currentImageUrl != null ? currentImageUrl : "");
        imageField.setPromptText("URL de l'image");
        imageField.setEditable(false);
        imageField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-background-color: #f0f0f0;");

        Button chooseImageBtn = new Button("Choisir une image");
        chooseImageBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 5; -fx-cursor: hand;");
        chooseImageBtn.setOnAction(e -> chooseImage(imageField));

        content.getChildren().addAll(
                new Label("Titre:"), titleField,
                new Label("Description:"), descArea,
                new Label("Tag:"), tagField,
                new Label("Image:"), new HBox(10, imageField, chooseImageBtn)
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String newTitle = titleField.getText().trim();
                String newDesc = descArea.getText().trim();
                String newTag = tagField.getText().trim();
                String newImage = imageField.getText().trim();

                if (validatePostInput(newTitle, newDesc)) {
                    updatePost(postId, newTitle, newDesc, newImage, newTag);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    // ==================== AFFICHER/MASQUER LES COMMENTAIRES ====================
    private void toggleComments(VBox postCard) {
        for (Node child : postCard.getChildren()) {
            if (child instanceof VBox && child.getStyleClass().contains("comments-section")) {
                boolean visible = !child.isVisible();
                child.setVisible(visible);
                child.setManaged(visible);

                for (Node btn : postCard.getChildren()) {
                    if (btn instanceof Button && ((Button) btn).getText().contains("💬")) {
                        Button commentBtn = (Button) btn;
                        if (visible) {
                            commentBtn.setText("💬 Masquer");
                        } else {
                            commentBtn.setText("💬 Commenter");
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    // ==================== CONSTRUCTION DU FORMULAIRE DE COMMENTAIRE ====================
    private HBox buildCommentForm(int postId, VBox commentsSection) {
        HBox form = new HBox(10);
        form.getStyleClass().add("comment-form");
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-background-color: #e8f2ff; -fx-background-radius: 40; -fx-padding: 12; -fx-spacing: 10; -fx-border-color: #7aa5d9; -fx-border-width: 2; -fx-border-radius: 40;");

        TextField commentInput = new TextField();
        commentInput.setPromptText("Écrire un commentaire...");
        commentInput.setPrefWidth(350);
        commentInput.setStyle("-fx-background-color: white; -fx-background-radius: 30; -fx-padding: 12 18; -fx-font-size: 13px; -fx-border-color: #b0d0ff; -fx-border-width: 1; -fx-border-radius: 30;");
        HBox.setHgrow(commentInput, Priority.ALWAYS);

        Button submitComment = new Button("Publier");
        submitComment.setStyle("-fx-background-color: #2a5f9f; -fx-text-fill: white; -fx-padding: 10 25; -fx-background-radius: 30; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");

        submitComment.setOnAction(e -> {
            String content = commentInput.getText().trim();
            if (!content.isEmpty()) {
                addComment(postId, content, null, commentsSection);
                commentInput.clear();
            }
        });

        commentInput.setOnAction(e -> {
            String content = commentInput.getText().trim();
            if (!content.isEmpty()) {
                addComment(postId, content, null, commentsSection);
                commentInput.clear();
            }
        });

        form.getChildren().addAll(commentInput, submitComment);
        return form;
    }

    // ==================== CHARGEMENT DES COMMENTAIRES ====================
    private void loadCommentsForPost(int postId, VBox commentsSection) {
        String sql = "SELECT c.*, u.Nom_Utilisateur as author_name " +
                "FROM comments c " +
                "JOIN utilisateur u ON c.user_id = u.ID_UTILISATEUR " +
                "WHERE c.post_id = ? AND c.is_active = true AND c.parent_comment_id IS NULL " +
                "ORDER BY c.created_at ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            List<Map<String, Object>> commentsData = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> comment = new HashMap<>();
                comment.put("id", rs.getInt("id"));
                comment.put("user_id", rs.getInt("user_id"));
                comment.put("content", rs.getString("content"));
                comment.put("created_at", rs.getTimestamp("created_at"));
                comment.put("author_name", rs.getString("author_name"));
                commentsData.add(comment);
            }

            for (Map<String, Object> commentData : commentsData) {
                VBox commentBox = buildCommentFromData(commentData, postId, commentsSection);
                commentsSection.getChildren().add(commentsSection.getChildren().size() - 1, commentBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== CONSTRUCTION D'UN COMMENTAIRE ====================
    private VBox buildCommentFromData(Map<String, Object> commentData, int postId, VBox commentsSection) {
        try {
            int commentId = (int) commentData.get("id");
            int userId = (int) commentData.get("user_id");
            String author = (String) commentData.get("author_name");
            String content = (String) commentData.get("content");
            Timestamp createdAt = (Timestamp) commentData.get("created_at");

            VBox commentBox = new VBox(8);
            commentBox.getStyleClass().add("comment");
            commentBox.setId("comment-" + commentId);
            commentBox.setPadding(new Insets(15));
            commentBox.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 15; -fx-spacing: 10; -fx-border-color: #d0e5ff; -fx-border-width: 1; -fx-border-radius: 16;");

            // En-tête du commentaire
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setStyle("-fx-background-color: #e8f2ff; -fx-padding: 8 12; -fx-background-radius: 30; -fx-spacing: 10;");

            Label authorLabel = new Label(author);
            authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0a3a6a; -fx-background-color: #d0e5ff; -fx-padding: 3 12; -fx-background-radius: 20;");

            Label timeLabel = new Label(formatTimeAgo(createdAt.toLocalDateTime()));
            timeLabel.setStyle("-fx-text-fill: #4a6f94; -fx-font-size: 11px; -fx-font-style: italic;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Boutons d'action pour le commentaire (Modifier/Supprimer) - uniquement si c'est l'auteur
            HBox actionButtons = new HBox(5);
            if (userId == currentUserId) {
                Button editCommentBtn = new Button("✏️");
                editCommentBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 15; -fx-cursor: hand; -fx-font-size: 11px;");
                editCommentBtn.setTooltip(new Tooltip("Modifier ce commentaire"));
                editCommentBtn.setOnAction(e -> showEditCommentDialog(commentId, content));

                Button deleteCommentBtn = new Button("🗑️");
                deleteCommentBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 15; -fx-cursor: hand; -fx-font-size: 11px;");
                deleteCommentBtn.setTooltip(new Tooltip("Supprimer ce commentaire"));
                deleteCommentBtn.setOnAction(e -> deleteComment(commentId));

                actionButtons.getChildren().addAll(editCommentBtn, deleteCommentBtn);
            }

            // Bouton réponse
            Button replyBtn = new Button("↩️ Répondre");
            replyBtn.setStyle("-fx-background-color: #e5f0ff; -fx-text-fill: #0a3f7a; -fx-padding: 5 15; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #7aa5d9; -fx-border-width: 1; -fx-border-radius: 20;");
            replyBtn.setOnAction(e -> showReplyInput(commentBox, postId, commentId, commentsSection));

            header.getChildren().addAll(authorLabel, timeLabel, spacer, actionButtons, replyBtn);

            // Contenu du commentaire
            VBox contentBox = new VBox();
            contentBox.setStyle("-fx-background-color: #f5faff; -fx-padding: 12 15; -fx-background-radius: 12; -fx-border-color: #c0d8ff; -fx-border-width: 1; -fx-border-radius: 12;");

            Label message = new Label(content);
            message.setWrapText(true);
            message.setStyle("-fx-font-size: 13px; -fx-text-fill: #1a2e42; -fx-wrap-text: true; -fx-line-spacing: 3;");

            contentBox.getChildren().add(message);

            commentBox.getChildren().addAll(header, contentBox);

            // Charger les réponses à ce commentaire
            loadReplies(commentId, commentBox, postId, commentsSection);

            return commentBox;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Dialog pour modifier un commentaire
    private void showEditCommentDialog(int commentId, String currentContent) {
        TextInputDialog dialog = new TextInputDialog(currentContent);
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText("Modifier votre commentaire");
        dialog.setContentText("Nouveau contenu:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            if (!newContent.trim().isEmpty() && validateCommentInput(newContent)) {
                updateComment(commentId, newContent);
            }
        });
    }

    // ==================== CHARGEMENT DES RÉPONSES ====================
    private void loadReplies(int parentCommentId, VBox parentCommentBox, int postId, VBox commentsSection) {
        String sql = "SELECT c.*, u.Nom_Utilisateur as author_name " +
                "FROM comments c " +
                "JOIN utilisateur u ON c.user_id = u.ID_UTILISATEUR " +
                "WHERE c.parent_comment_id = ? AND c.is_active = true " +
                "ORDER BY c.created_at ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, parentCommentId);
            ResultSet rs = stmt.executeQuery();

            VBox repliesBox = new VBox(8);
            repliesBox.getStyleClass().add("replies");
            repliesBox.setPadding(new Insets(8, 0, 0, 25));
            repliesBox.setStyle("-fx-background-color: #f0f8ff; -fx-background-radius: 12; -fx-padding: 10; -fx-spacing: 8; -fx-border-color: #b0d0ff; -fx-border-width: 0 0 0 3; -fx-border-radius: 0 12 12 0;");

            boolean hasReplies = false;
            while (rs.next()) {
                hasReplies = true;
                int replyId = rs.getInt("id");
                int userId = rs.getInt("user_id");
                String author = rs.getString("author_name");
                String content = rs.getString("content");
                Timestamp createdAt = rs.getTimestamp("created_at");

                VBox replyBox = new VBox(5);
                replyBox.getStyleClass().add("reply");
                replyBox.setId("reply-" + replyId);
                replyBox.setPadding(new Insets(10));
                replyBox.setStyle("-fx-background-color: #f5faff; -fx-background-radius: 12; -fx-border-color: #c0d8ff; -fx-border-width: 1; -fx-border-radius: 12;");

                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);

                Label authorLabel = new Label("↳ " + author);
                authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #0a3a6a;");

                Label timeLabel = new Label(formatTimeAgo(createdAt.toLocalDateTime()));
                timeLabel.setStyle("-fx-text-fill: #4a6f94; -fx-font-size: 10px; -fx-font-style: italic;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // Boutons d'action pour la réponse (Modifier/Supprimer) - uniquement si c'est l'auteur
                HBox actionButtons = new HBox(5);
                if (userId == currentUserId) {
                    Button editReplyBtn = new Button("✏️");
                    editReplyBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-size: 10px;");
                    editReplyBtn.setTooltip(new Tooltip("Modifier cette réponse"));
                    editReplyBtn.setOnAction(e -> showEditCommentDialog(replyId, content));

                    Button deleteReplyBtn = new Button("🗑️");
                    deleteReplyBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-size: 10px;");
                    deleteReplyBtn.setTooltip(new Tooltip("Supprimer cette réponse"));
                    deleteReplyBtn.setOnAction(e -> deleteComment(replyId));

                    actionButtons.getChildren().addAll(editReplyBtn, deleteReplyBtn);
                }

                header.getChildren().addAll(authorLabel, timeLabel, spacer, actionButtons);

                Label message = new Label(content);
                message.setWrapText(true);
                message.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a2e42; -fx-padding: 3 0;");

                replyBox.getChildren().addAll(header, message);
                repliesBox.getChildren().add(replyBox);
            }

            if (hasReplies) {
                parentCommentBox.getChildren().add(repliesBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== AFFICHER LE CHAMP DE RÉPONSE ====================
    private void showReplyInput(VBox parentCommentBox, int postId, int parentCommentId, VBox commentsSection) {
        for (Node child : parentCommentBox.getChildren()) {
            if (child instanceof HBox && child.getStyleClass().contains("reply-form")) {
                return;
            }
        }

        HBox replyForm = new HBox(10);
        replyForm.getStyleClass().add("reply-form");
        replyForm.setPadding(new Insets(8, 0, 0, 25));
        replyForm.setStyle("-fx-background-color: #f0f5ff; -fx-background-radius: 30; -fx-padding: 10; -fx-spacing: 8; -fx-border-color: #b0d0ff; -fx-border-width: 1; -fx-border-radius: 30;");

        TextField replyInput = new TextField();
        replyInput.setPromptText("Écrire une réponse...");
        replyInput.setPrefWidth(300);
        replyInput.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-padding: 8 15; -fx-font-size: 12px; -fx-border-color: #b0d0ff; -fx-border-width: 1; -fx-border-radius: 25;");

        Button submitReply = new Button("Répondre");
        submitReply.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 6 15; -fx-background-radius: 25; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
        submitReply.setOnAction(e -> {
            String content = replyInput.getText().trim();
            if (!content.isEmpty()) {
                addComment(postId, content, parentCommentId, commentsSection);
                parentCommentBox.getChildren().remove(replyForm);
            }
        });

        Button cancelReply = new Button("Annuler");
        cancelReply.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 6 15; -fx-background-radius: 25; -fx-font-size: 11px; -fx-cursor: hand;");
        cancelReply.setOnAction(e -> parentCommentBox.getChildren().remove(replyForm));

        replyInput.setOnAction(e -> {
            String content = replyInput.getText().trim();
            if (!content.isEmpty()) {
                addComment(postId, content, parentCommentId, commentsSection);
                parentCommentBox.getChildren().remove(replyForm);
            }
        });

        replyForm.getChildren().addAll(replyInput, submitReply, cancelReply);
        parentCommentBox.getChildren().add(replyForm);
    }

    // ==================== MÉTHODES UTILITAIRES ====================
    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance();
    }

    private void createUploadDirectory() {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("✅ Dossier uploads créé");
        }
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        long seconds = java.time.Duration.between(dateTime, LocalDateTime.now()).getSeconds();
        if (seconds < 60) return "à l'instant";
        if (seconds < 3600) return (seconds / 60) + " min";
        if (seconds < 86400) return (seconds / 3600) + " h";
        if (seconds < 604800) return (seconds / 86400) + " j";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private void showError(String message, String type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type.equals("error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
            alert.setTitle(type.equals("error") ? "Erreur" : "Information");
            alert.setHeaderText(null);
            alert.setContentText(message);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #2a5f9f; -fx-border-width: 2; -fx-border-radius: 15;");

            alert.showAndWait();
        });
    }

    private void showInfo(String message, String type) {
        showError(message, type);
    }

    // ==================== MÉTHODES DE RECHERCHE ET FILTRAGE ====================
    private void wireSearch() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        if (searchButton != null) searchButton.setOnAction(event -> applyFilter());
    }

    private void wirePostForm() {
        if (clearPostButton != null) clearPostButton.setOnAction(event -> clearPostForm());
        if (submitPostButton != null) submitPostButton.setOnAction(event -> createPost());
        if (chooseImageButton != null) chooseImageButton.setOnAction(e -> chooseImage(postImageField));
    }

    private void applyFilter() {
        String query = normalize(searchField.getText());
        for (Node postCard : postCards) {
            String text = normalize(collectText(postCard));
            boolean show = query.isEmpty() || text.contains(query);
            postCard.setVisible(show);
            postCard.setManaged(show);
        }
    }

    private void clearPostForm() {
        if (postTitleField != null) postTitleField.clear();
        if (postDescriptionArea != null) postDescriptionArea.clear();
        if (postImageField != null) postImageField.clear();
    }

    private String collectText(Node node) {
        if (node instanceof Labeled) return ((Labeled) node).getText();
        if (node instanceof TextInputControl) return ((TextInputControl) node).getText();
        if (node instanceof Text) return ((Text) node).getText();
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            StringBuilder builder = new StringBuilder();
            for (Node child : parent.getChildrenUnmodifiable()) {
                String childText = collectText(child);
                if (!childText.isBlank()) builder.append(' ').append(childText);
            }
            return builder.toString();
        }
        return "";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}