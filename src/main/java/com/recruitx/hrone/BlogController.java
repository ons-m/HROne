package com.recruitx.hrone;

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
import javafx.scene.layout.Priority;  // ← IMPORT MANQUANT AJOUTÉ
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.scene.paint.Color;

public class BlogController {

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

    // Nouveaux FXML
    @FXML private ComboBox<String> languageCombo;
    @FXML private Button whatsappBtn;
    @FXML private Label whatsappFooterLabel;
    @FXML private Label appTitleLabel;
    @FXML private Label appSubtitleLabel;
    @FXML private Label heroTitleLabel;
    @FXML private Text heroSubtitleLabel;

    // Statistiques
    @FXML private Label positiveStatsLabel;
    @FXML private Label neutralStatsLabel;
    @FXML private Label negativeStatsLabel;
    @FXML private Label membersStatsLabel;
    @FXML private Label postsStatsLabel;
    @FXML private Label commentsStatsLabel;

    // Tags
    @FXML private Button tagAllBtn;
    @FXML private Button tagSportBtn;
    @FXML private Button tagCuisineBtn;
    @FXML private Button tagTechBtn;
    @FXML private Button tagRHBtn;
    @FXML private Button tagCultureBtn;
    @FXML private FlowPane popularTagsContainer;

    // Chatbot
    @FXML private VBox chatContainer;
    @FXML private TextField chatInputField;
    @FXML private Button sendChatBtn;

    // API Buttons
    @FXML private Button weatherBtn;
    @FXML private Button adviceBtn;
    @FXML private Button quoteBtn;
    @FXML private Button jokeBtn;
    @FXML private Button animalBtn;
    @FXML private Button translateBtn;
    @FXML private Button emailBtn;
    @FXML private Button newsletterBtn;
    @FXML private Button sentimentBtn;
    @FXML private Button refreshWeatherBtn;

    // Météo
    @FXML private Label weatherCityLabel;
    @FXML private Label weatherTempLabel;
    @FXML private Label weatherDescLabel;

    // ==================== CONSTANTES ====================
    private static final String DB_URL = "jdbc:mysql://localhost:3306/blog_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static final String UPLOAD_DIR = "uploads/";
    private static final String WHATSAPP_NUMBER = "21690044054";

    // APIs
    private static final String WEATHER_API = "https://wttr.in/";
    private static final String JOKE_API = "https://v2.jokeapi.dev/joke/Any?type=single";
    private static final String QUOTE_API = "https://api.quotable.io/random";
    private static final String DOG_API = "https://dog.ceo/api/breeds/image/random";
    private static final String CAT_API = "https://api.thecatapi.com/v1/images/search";
    private static final String ADVICE_API = "https://api.adviceslip.com/advice";
    private static final String TRANSLATION_API = "https://api.mymemory.translated.net/get";

    // ==================== VARIABLES ====================
    private int currentUserId = 1;
    private String currentUserName = "Lina M.";
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
        createTablesIfNotExist();
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

    private void initializeLanguageCombo() {
        if (languageCombo != null) {
            languageCombo.getItems().addAll("🇫🇷 Français", "🇬🇧 English", "🇹🇳 العربية");
            languageCombo.setValue("🇫🇷 Français");
        }
    }

    // ==================== CRÉATION DES TABLES ====================
    private void createTablesIfNotExist() {
        // Exécuter les requêtes UNE PAR UNE (pas en bloc)
        String[] sqlQueries = {
                "CREATE TABLE IF NOT EXISTS users ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT, "
                        + "display_name VARCHAR(100) NOT NULL, "
                        + "email VARCHAR(100) UNIQUE, "
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                "CREATE TABLE IF NOT EXISTS posts ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT, "
                        + "user_id INT NOT NULL, "
                        + "title VARCHAR(255) NOT NULL, "
                        + "description TEXT, "
                        + "image_url VARCHAR(255), "
                        + "tag VARCHAR(50) DEFAULT 'General', "
                        + "is_active BOOLEAN DEFAULT true, "
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                "CREATE TABLE IF NOT EXISTS comments ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT, "
                        + "post_id INT NOT NULL, "
                        + "user_id INT NOT NULL, "
                        + "parent_comment_id INT NULL, "
                        + "content TEXT NOT NULL, "
                        + "is_active BOOLEAN DEFAULT true, "
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                "CREATE TABLE IF NOT EXISTS post_votes ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT, "
                        + "post_id INT NOT NULL, "
                        + "user_id INT NOT NULL, "
                        + "vote_type ENUM('up', 'down') NOT NULL, "
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                        + "UNIQUE KEY unique_post_vote (post_id, user_id))",

                "CREATE TABLE IF NOT EXISTS comment_votes ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT, "
                        + "comment_id INT NOT NULL, "
                        + "user_id INT NOT NULL, "
                        + "vote_type ENUM('up', 'down') NOT NULL, "
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                        + "UNIQUE KEY unique_comment_vote (comment_id, user_id))",

                "CREATE TABLE IF NOT EXISTS newsletter_emails ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT, "
                        + "email VARCHAR(100) UNIQUE NOT NULL, "
                        + "subscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : sqlQueries) {
                try {
                    stmt.execute(sql);
                    System.out.println("✅ Table créée/vérifiée: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                } catch (SQLException e) {
                    System.err.println("❌ Erreur sur requête: " + sql);
                    System.err.println("   Message: " + e.getMessage());
                }
            }
            System.out.println("✅ Toutes les tables ont été traitées");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
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
        if (emailBtn != null) emailBtn.setOnAction(e -> sendEmail());
        if (newsletterBtn != null) newsletterBtn.setOnAction(e -> sendNewsletter());
        if (sentimentBtn != null) sentimentBtn.setOnAction(e -> analyzeSentiment());
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

    private void analyzeSentiment() {
        String text = postDescriptionArea.getText();
        if (text.isEmpty()) {
            showError("Veuillez écrire du texte à analyser", "error");
            return;
        }

        String lowerText = text.toLowerCase();
        int positiveWords = 0;
        int negativeWords = 0;

        String[] positive = {"good", "great", "excellent", "awesome", "love", "happy", "super", "👍", "génial", "merveilleux", "parfait", "bien", "magnifique"};
        String[] negative = {"bad", "terrible", "awful", "hate", "sad", "angry", "problem", "issue", "👎", "mauvais", "horrible", "problème", "déteste"};

        for (String word : positive) {
            if (lowerText.contains(word)) positiveWords++;
        }
        for (String word : negative) {
            if (lowerText.contains(word)) negativeWords++;
        }

        String sentiment;
        String emoji;

        if (positiveWords > negativeWords) {
            sentiment = "POSITIF";
            emoji = "😊";
        } else if (negativeWords > positiveWords) {
            sentiment = "NÉGATIF";
            emoji = "😔";
        } else {
            sentiment = "NEUTRE";
            emoji = "😐";
        }

        String stats = "Mots positifs: " + positiveWords + "\nMots négatifs: " + negativeWords;
        String result = sentiment + " " + emoji + "\n\n" + stats;

        showInfo(result, "info");
    }

    private void suggestTagsFromDescription() {
        String description = postDescriptionArea.getText();
        if (description.isEmpty()) {
            showError("Veuillez d'abord écrire une description", "error");
            return;
        }

        List<String> suggestedTags = new ArrayList<>();
        String lowerDesc = description.toLowerCase();

        if (lowerDesc.contains("sport") || lowerDesc.contains("foot") || lowerDesc.contains("basket")) {
            suggestedTags.add("Sport");
        }
        if (lowerDesc.contains("cuisine") || lowerDesc.contains("manger") || lowerDesc.contains("recette")) {
            suggestedTags.add("Cuisine");
        }
        if (lowerDesc.contains("tech") || lowerDesc.contains("ordinateur") || lowerDesc.contains("code")) {
            suggestedTags.add("Tech");
        }
        if (lowerDesc.contains("rh") || lowerDesc.contains("recrutement") || lowerDesc.contains("carrière")) {
            suggestedTags.add("RH");
        }
        if (lowerDesc.contains("culture") || lowerDesc.contains("équipe") || lowerDesc.contains("team")) {
            suggestedTags.add("Culture");
        }
        if (suggestedTags.isEmpty()) {
            suggestedTags.add("General");
        }

        String tags = String.join(", ", suggestedTags);
        showInfo("Tags suggérés: " + tags, "info");
    }

    private void sendEmail() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("📧 Envoyer un email");
        dialog.setHeaderText("Partager ce post par email");
        dialog.setContentText("Adresse email:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            if (isValidEmail(email)) {
                showInfo("✅ Email envoyé à " + email, "success");
            } else {
                showError("❌ Adresse email invalide", "error");
            }
        });
    }

    private void sendNewsletter() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("📰 Newsletter");
        dialog.setHeaderText("Inscription à la newsletter");
        dialog.setContentText("Votre email:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            if (isValidEmail(email)) {
                showInfo("✅ Inscription réussie pour " + email, "success");
                sauvegarderEmailNewsletter(email);
            } else {
                showError("❌ Email invalide", "error");
            }
        });
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private void sauvegarderEmailNewsletter(String email) {
        String sql = "INSERT INTO newsletter_emails (email) VALUES (?) ON DUPLICATE KEY UPDATE email = email";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
            System.out.println("✅ Email newsletter sauvegardé: " + email);
        } catch (SQLException e) {
            System.err.println("❌ Erreur sauvegarde email: " + e.getMessage());
        }
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
        if (!TITLE_PATTERN.matcher(title).matches()) {
            showError("Le titre contient des caractères non autorisés", "error");
            return false;
        }
        if (!CONTENT_PATTERN.matcher(description).matches()) {
            showError("La description contient des caractères non autorisés", "error");
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
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setInt(1, postId);
                checkStmt.setInt(2, currentUserId);
                ResultSet rs = checkStmt.executeQuery();

                String action;
                if (!rs.next()) {
                    action = "insert";
                } else {
                    String existingVote = rs.getString("vote_type");
                    action = existingVote.equals("up") ? "delete" : "update";
                }

                String finalAction = action;
                Platform.runLater(() -> {
                    updatePostVoteInUI(postId, finalAction, upvoteBtn, downvoteBtn, voteCountLabel);
                });

            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Erreur lors du vote", "error"));
            }
        });
    }

    private void updatePostVoteInUI(int postId, String action, Button upvoteBtn, Button downvoteBtn, Label voteCountLabel) {
        try (Connection conn = getConnection()) {
            if (action.equals("insert")) {
                String insertSql = "INSERT INTO post_votes (post_id, user_id, vote_type) VALUES (?, ?, 'up')";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, postId);
                insertStmt.setInt(2, currentUserId);
                insertStmt.executeUpdate();

                upvoteBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                downvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");

            } else if (action.equals("delete")) {
                String deleteSql = "DELETE FROM post_votes WHERE post_id = ? AND user_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setInt(1, postId);
                deleteStmt.setInt(2, currentUserId);
                deleteStmt.executeUpdate();

                upvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");
                downvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");

            } else if (action.equals("update")) {
                String updateSql = "UPDATE post_votes SET vote_type = 'down' WHERE post_id = ? AND user_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, postId);
                updateStmt.setInt(2, currentUserId);
                updateStmt.executeUpdate();

                upvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");
                downvoteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            }

            String countSql = "SELECT COUNT(*) as count FROM post_votes WHERE post_id = ? AND vote_type = 'up'";
            PreparedStatement countStmt = conn.prepareStatement(countSql);
            countStmt.setInt(1, postId);
            ResultSet rs = countStmt.executeQuery();
            if (rs.next()) {
                voteCountLabel.setText(String.valueOf(rs.getInt("count")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== VOTES POUR COMMENTAIRES ====================
    private void handleCommentVote(int commentId, Button upvoteBtn, Button downvoteBtn, Label voteCountLabel) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                String checkSql = "SELECT vote_type FROM comment_votes WHERE comment_id = ? AND user_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setInt(1, commentId);
                checkStmt.setInt(2, currentUserId);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    String insertSql = "INSERT INTO comment_votes (comment_id, user_id, vote_type) VALUES (?, ?, 'up')";
                    PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                    insertStmt.setInt(1, commentId);
                    insertStmt.setInt(2, currentUserId);
                    insertStmt.executeUpdate();

                    Platform.runLater(() -> {
                        upvoteBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                        downvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");
                    });
                } else {
                    String existingVote = rs.getString("vote_type");
                    if (existingVote.equals("up")) {
                        String deleteSql = "DELETE FROM comment_votes WHERE comment_id = ? AND user_id = ?";
                        PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                        deleteStmt.setInt(1, commentId);
                        deleteStmt.setInt(2, currentUserId);
                        deleteStmt.executeUpdate();

                        Platform.runLater(() -> {
                            upvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");
                            downvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");
                        });
                    } else {
                        String updateSql = "UPDATE comment_votes SET vote_type = 'up' WHERE comment_id = ? AND user_id = ?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                        updateStmt.setInt(1, commentId);
                        updateStmt.setInt(2, currentUserId);
                        updateStmt.executeUpdate();

                        Platform.runLater(() -> {
                            upvoteBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                            downvoteBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: black;");
                        });
                    }
                }

                String countSql = "SELECT COUNT(*) as count FROM comment_votes WHERE comment_id = ? AND vote_type = 'up'";
                PreparedStatement countStmt = conn.prepareStatement(countSql);
                countStmt.setInt(1, commentId);
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next()) {
                    int newCount = countRs.getInt("count");
                    Platform.runLater(() -> voteCountLabel.setText(String.valueOf(newCount)));
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Erreur lors du vote", "error"));
            }
        });
    }

    // ==================== ANALYSE DE SENTIMENT ====================
    private void analyzeCommentSentiment(String text) {
        if (text == null || text.isEmpty()) return;
        updateStatistics();
    }

    // ==================== STATISTIQUES ====================
    private void updateStatistics() {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                String membersSql = "SELECT COUNT(*) as count FROM users";
                PreparedStatement membersStmt = conn.prepareStatement(membersSql);
                ResultSet membersRs = membersStmt.executeQuery();
                int members = membersRs.next() ? membersRs.getInt("count") : 0;

                String postsSql = "SELECT COUNT(*) as count FROM posts WHERE is_active = true";
                PreparedStatement postsStmt = conn.prepareStatement(postsSql);
                ResultSet postsRs = postsStmt.executeQuery();
                int posts = postsRs.next() ? postsRs.getInt("count") : 0;

                String commentsSql = "SELECT COUNT(*) as count FROM comments WHERE is_active = true";
                PreparedStatement commentsStmt = conn.prepareStatement(commentsSql);
                ResultSet commentsRs = commentsStmt.executeQuery();
                int comments = commentsRs.next() ? commentsRs.getInt("count") : 0;

                Platform.runLater(() -> {
                    membersStatsLabel.setText(String.valueOf(members));
                    postsStatsLabel.setText(String.valueOf(posts));
                    commentsStatsLabel.setText(String.valueOf(comments));
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
                    if (child instanceof Label && child.getStyleClass().contains("tag")) {
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
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();

                List<String> tags = new ArrayList<>();
                while (rs.next()) {
                    tags.add(rs.getString("tag") + " (" + rs.getInt("count") + ")");
                }

                Platform.runLater(() -> {
                    popularTagsContainer.getChildren().clear();
                    for (String tag : tags) {
                        Button tagBtn = new Button("#" + tag);
                        tagBtn.getStyleClass().add("tag");
                        tagBtn.setOnAction(e -> {
                            currentTagFilter = tag.split(" ")[0];
                            applyTagFilter();
                        });
                        popularTagsContainer.getChildren().add(tagBtn);
                    }
                });

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
        msg.setStyle(isBot ? "-fx-background-color: #2a5f9f; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 20 20 20 5; -fx-font-size: 13px;" :
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
                "La météo actuelle à " + weatherCityLabel.getText() + " est " + weatherTempLabel.getText() + " avec " + weatherDescLabel.getText(),
                "Il fait " + weatherTempLabel.getText() + " à " + weatherCityLabel.getText() + ". " + weatherDescLabel.getText()
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
                "Vous pouvez voter pour les posts et les commentaires avec les boutons 👍 et 👎.",
                "Les votes aident à mettre en avant le contenu de qualité."
        });

        responses.put("statistiques|stats|statistics", new String[]{
                "📊 Statistiques actuelles:\n• Membres: " + membersStatsLabel.getText() +
                        "\n• Posts: " + postsStatsLabel.getText() + "\n• Commentaires: " + commentsStatsLabel.getText()
        });

        responses.put("blague|joke|rire|humour", new String[]{
                "Pour une blague, cliquez sur le bouton '😂 Blague' dans la section APIs !",
                "Je ne suis pas très drôle, mais le bouton '😂 Blague' vous fera rire !"
        });

        responses.put("citation|quote|inspiration", new String[]{
                "Le bouton '📝 Citation' vous donnera une citation inspirante.",
                "Besoin d'inspiration ? Utilisez le bouton '📝 Citation' !"
        });

        responses.put("aide|help|what can you do|que fais tu", new String[]{
                "Je peux vous aider avec :\n• Créer des posts\n• Commenter\n• Voter\n• Voir la météo\n• Obtenir des blagues/citations\n• Filtrer par tags\n• Statistiques\n• Traduction\n• Et plus encore !"
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
        } else if (selected.contains("English")) {
            currentLanguage = "en";
            appTitleLabel.setText("HR One");
            appSubtitleLabel.setText("Employee Forum");
            heroTitleLabel.setText("✨ Share Your Ideas ✨");
            heroSubtitleLabel.setText("Post suggestions, ask questions, and keep the conversation moving.");
            searchField.setPromptText("🔍 Search by keywords or tags...");
            searchButton.setText("🔍 Search");
            tagAllBtn.setText("All");
        } else if (selected.contains("العربية")) {
            currentLanguage = "ar";
            appTitleLabel.setText("HR One");
            appSubtitleLabel.setText("منتدى الموظفين");
            heroTitleLabel.setText("✨ شارك أفكارك ✨");
            heroSubtitleLabel.setText("انشر اقتراحاتك، اطرح أسئلة وحافظ على استمرار المحادثة.");
            searchField.setPromptText("🔍 ابحث بالكلمات الرئيسية أو الوسوم...");
            searchButton.setText("🔍 بحث");
            tagAllBtn.setText("الكل");
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
            String url = "https://wa.me/" + WHATSAPP_NUMBER;
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            showError("Impossible d'ouvrir WhatsApp", "error");
        }
    }

    // ==================== CRÉATION DE POST ====================
    @FXML
    private void createPost() {
        String title = postTitleField.getText().trim();
        String description = postDescriptionArea.getText().trim();
        String imagePath = postImageField.getText().trim();

        if (!validatePostInput(title, description)) return;

        String tag = suggestTagFromDescription(description);

        String sql = "INSERT INTO posts (user_id, title, description, image_url, tag) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, currentUserId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setString(4, imagePath.isEmpty() ? null : imagePath);
            stmt.setString(5, tag);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
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

    private String suggestTagFromDescription(String description) {
        String lower = description.toLowerCase();
        if (lower.contains("sport") || lower.contains("foot") || lower.contains("basket")) return "Sport";
        if (lower.contains("cuisine") || lower.contains("manger") || lower.contains("recette")) return "Cuisine";
        if (lower.contains("tech") || lower.contains("ordinateur") || lower.contains("code")) return "Tech";
        if (lower.contains("rh") || lower.contains("recrutement") || lower.contains("carrière")) return "RH";
        if (lower.contains("culture") || lower.contains("équipe") || lower.contains("team")) return "Culture";
        return "General";
    }

    // ==================== CRÉATION DU FORMULAIRE DE POST ====================
    private Node createPostForm() {
        VBox formCard = new VBox();
        formCard.getStyleClass().addAll("panel-card", "post-form-card");
        formCard.setPadding(new Insets(20));
        formCard.setSpacing(15);
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label title = new Label("Créer un post");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Partagez un titre, décrivez votre idée et ajoutez une image optionnelle.");
        subtitle.setStyle("-fx-text-fill: #7f8c8d;");
        subtitle.setWrapText(true);

        VBox form = new VBox();
        form.setSpacing(15);

        // Champ Titre
        VBox titleField = new VBox(5);
        Label titleLabel = new Label("Titre");
        titleLabel.setStyle("-fx-font-weight: bold;");
        TextField titleInput = new TextField();
        titleInput.setPromptText("Ex: Améliorer l'onboarding des nouveaux");
        titleInput.setId("postTitleField");
        titleInput.setStyle("-fx-padding: 10; -fx-background-radius: 5;");
        titleField.getChildren().addAll(titleLabel, titleInput);

        // Champ Description
        VBox descField = new VBox(5);
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-font-weight: bold;");
        TextArea descInput = new TextArea();
        descInput.setPromptText("Décrivez votre idée ou feedback...");
        descInput.setPrefRowCount(4);
        descInput.setId("postDescriptionArea");
        descInput.setStyle("-fx-padding: 10; -fx-background-radius: 5;");
        descField.getChildren().addAll(descLabel, descInput);

        // Champ Image avec boutons
        VBox imageField = new VBox(5);
        Label imageLabel = new Label("Image (optionnelle)");
        imageLabel.setStyle("-fx-font-weight: bold;");
        HBox imageBox = new HBox(8);
        TextField imageInput = new TextField();
        imageInput.setPromptText("Aucun fichier choisi");
        imageInput.setId("postImageField");
        imageInput.setPrefWidth(300);
        imageInput.setEditable(false);
        imageInput.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");

        Button chooseBtn = new Button("Choisir Image");
        chooseBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 5;");
        chooseBtn.setOnAction(e -> chooseImage(imageInput));

        Button randomImageBtn = new Button("🎲 Image aléatoire");
        randomImageBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 5;");
        randomImageBtn.setOnAction(e -> getRandomImage(imageInput));

        imageBox.getChildren().addAll(imageInput, chooseBtn, randomImageBtn);
        imageField.getChildren().addAll(imageLabel, imageBox);

        // Boutons d'actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button suggestTagsBtn = new Button("🏷️ Suggérer tags");
        suggestTagsBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 5; -fx-font-weight: bold;");
        suggestTagsBtn.setOnAction(e -> suggestTagsFromDescription());

        Button analyzeBtn = new Button("📊 Analyser");
        analyzeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 5; -fx-font-weight: bold;");
        analyzeBtn.setOnAction(e -> analyzeSentiment());

        Button clearBtn = new Button("Effacer");
        clearBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 5;");
        clearBtn.setId("clearPostButton");

        Button submitBtn = new Button("Publier");
        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 5; -fx-font-weight: bold;");
        submitBtn.setId("submitPostButton");

        actions.getChildren().addAll(suggestTagsBtn, analyzeBtn, clearBtn, submitBtn);

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

        String sql = "SELECT p.*, u.display_name as author_name, " +
                "(SELECT COUNT(*) FROM post_votes WHERE post_id = p.id AND vote_type = 'up') as upvotes " +
                "FROM posts p " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE p.is_active = true " +
                "ORDER BY p.created_at DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                VBox postCard = buildPostCard(rs);
                feedContainer.getChildren().add(postCard);
                postCards.add(postCard);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement: " + e.getMessage(), "error");
        }
    }

    // ==================== CONSTRUCTION D'UNE CARTE DE POST ====================
    private VBox buildPostCard(ResultSet rs) throws SQLException {
        int postId = rs.getInt("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String imageUrl = rs.getString("image_url");
        String tag = rs.getString("tag");
        int upvotes = rs.getInt("upvotes");
        String author = rs.getString("author_name");
        Timestamp createdAt = rs.getTimestamp("created_at");

        VBox card = new VBox();
        card.getStyleClass().add("post-card");
        card.setId("post-" + postId);
        card.setSpacing(12);

        // En-tête avec auteur et date
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label(author);
        authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label timeLabel = new Label(formatTimeAgo(createdAt.toLocalDateTime()));
        timeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        header.getChildren().addAll(authorLabel, timeLabel);

        // Tag
        Label tagLabel = new Label("#" + tag);
        tagLabel.getStyleClass().add("tag");

        // Titre
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-wrap-text: true;");

        // Description
        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-padding: 10 0;");

        // Image (si présente)
        ImageView imageView = null;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                imageView = new ImageView();
                File imgFile = new File(imageUrl);
                if (imgFile.exists()) {
                    imageView.setImage(new Image(imgFile.toURI().toString()));
                } else {
                    imageView.setImage(new Image(imageUrl));
                }
                imageView.setFitWidth(600);
                imageView.setPreserveRatio(true);
                imageView.setStyle("-fx-padding: 10 0;");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ==================== SECTION VOTES ====================
        HBox voteBox = new HBox(5);
        voteBox.setAlignment(Pos.CENTER_LEFT);
        voteBox.getStyleClass().add("vote-container");

        Button upvoteBtn = new Button("👍");
        upvoteBtn.getStyleClass().add("vote-btn-up");

        Label voteCount = new Label(String.valueOf(upvotes));
        voteCount.getStyleClass().add("vote-count");

        Button downvoteBtn = new Button("👎");
        downvoteBtn.getStyleClass().add("vote-btn-down");

        upvoteBtn.setOnAction(e -> handlePostVote(postId, upvoteBtn, downvoteBtn, voteCount));
        downvoteBtn.setOnAction(e -> handlePostVote(postId, downvoteBtn, upvoteBtn, voteCount));

        voteBox.getChildren().addAll(upvoteBtn, voteCount, downvoteBtn);

        // ==================== BOUTON COMMENTAIRE ====================
        Button commentBtn = new Button("💬 Commenter");
        commentBtn.getStyleClass().add("btn-secondary");

        // ACTION POUR AFFICHER/MASQUER LES COMMENTAIRES
        commentBtn.setOnAction(e -> toggleComments(card));

        // ==================== SECTION COMMENTAIRES (CACHÉE PAR DÉFAUT) ====================
        VBox commentsSection = new VBox(10);
        commentsSection.getStyleClass().add("comments-section");
        commentsSection.setId("comments-" + postId);
        commentsSection.setVisible(false);
        commentsSection.setManaged(false);
        commentsSection.setPadding(new Insets(15, 0, 0, 0));

        // Formulaire pour ajouter un commentaire
        HBox commentForm = buildCommentForm(postId, commentsSection);
        commentsSection.getChildren().add(commentForm);

        // Charger les commentaires existants
        loadCommentsForPost(postId, commentsSection);

        // ==================== ASSEMBLAGE DE LA CARTE ====================
        card.getChildren().add(header);
        card.getChildren().add(tagLabel);
        card.getChildren().add(titleLabel);
        card.getChildren().add(descLabel);
        if (imageView != null) card.getChildren().add(imageView);
        card.getChildren().add(voteBox);
        card.getChildren().add(commentBtn);
        card.getChildren().add(commentsSection); // Section commentaires (cachée au départ)

        return card;
    }

    // ==================== AFFICHER/MASQUER LES COMMENTAIRES ====================
    private void toggleComments(VBox postCard) {
        for (Node child : postCard.getChildren()) {
            if (child instanceof VBox && child.getStyleClass().contains("comments-section")) {
                boolean visible = !child.isVisible();
                child.setVisible(visible);
                child.setManaged(visible);

                // Changer le texte du bouton
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
        form.setStyle("-fx-background-color: #f0f4f8; -fx-border-radius: 5; -fx-background-radius: 5;");

        TextField commentInput = new TextField();
        commentInput.setPromptText("Écrire un commentaire...");
        commentInput.setPrefWidth(350);
        commentInput.setStyle("-fx-padding: 10; -fx-background-radius: 5;");
        HBox.setHgrow(commentInput, Priority.ALWAYS);  // ← Ligne 1085 où Priority est utilisé

        Button submitComment = new Button("Publier");
        submitComment.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-weight: bold;");

        // Action pour ajouter un commentaire
        submitComment.setOnAction(e -> {
            String content = commentInput.getText().trim();
            if (!content.isEmpty()) {
                addComment(postId, content, null, commentsSection);
                commentInput.clear();
            }
        });

        // Appui sur Entrée pour valider
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
        String sql = "SELECT c.*, u.display_name as author_name " +
                "FROM comments c " +
                "JOIN users u ON c.user_id = u.id " +
                "WHERE c.post_id = ? AND c.is_active = true AND c.parent_comment_id IS NULL " +
                "ORDER BY c.created_at ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                VBox commentBox = buildCommentFromDB(rs, commentsSection);
                // Insérer avant le formulaire
                commentsSection.getChildren().add(commentsSection.getChildren().size() - 1, commentBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== CONSTRUCTION D'UN COMMENTAIRE ====================
    private VBox buildCommentFromDB(ResultSet rs, VBox commentsSection) throws SQLException {
        int commentId = rs.getInt("id");
        int postId = rs.getInt("post_id");
        String author = rs.getString("author_name");
        String content = rs.getString("content");
        Timestamp createdAt = rs.getTimestamp("created_at");

        VBox commentBox = new VBox(8);
        commentBox.getStyleClass().add("comment");
        commentBox.setId("comment-" + commentId);
        commentBox.setPadding(new Insets(12));
        commentBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8;");

        // En-tête du commentaire
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label(author);
        authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label timeLabel = new Label(formatTimeAgo(createdAt.toLocalDateTime()));
        timeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        header.getChildren().addAll(authorLabel, timeLabel);

        // Contenu du commentaire
        Label message = new Label(content);
        message.setWrapText(true);
        message.setStyle("-fx-padding: 5 0; -fx-font-size: 13px;");

        // Actions du commentaire
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button replyBtn = new Button("↩️ Répondre");
        replyBtn.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5 15; -fx-background-radius: 20; -fx-font-size: 11px;");
        replyBtn.setOnAction(e -> showReplyInput(commentBox, postId, commentId, commentsSection));

        actions.getChildren().addAll(replyBtn);

        commentBox.getChildren().addAll(header, message, actions);

        // Charger les réponses à ce commentaire
        loadReplies(commentId, commentBox, postId, commentsSection);

        return commentBox;
    }

    // ==================== CHARGEMENT DES RÉPONSES ====================
    private void loadReplies(int parentCommentId, VBox parentCommentBox, int postId, VBox commentsSection) {
        String sql = "SELECT c.*, u.display_name as author_name " +
                "FROM comments c " +
                "JOIN users u ON c.user_id = u.id " +
                "WHERE c.parent_comment_id = ? AND c.is_active = true " +
                "ORDER BY c.created_at ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, parentCommentId);
            ResultSet rs = stmt.executeQuery();

            VBox repliesBox = new VBox(8);
            repliesBox.getStyleClass().add("replies");
            repliesBox.setPadding(new Insets(8, 0, 0, 25));

            while (rs.next()) {
                int replyId = rs.getInt("id");
                String author = rs.getString("author_name");
                String content = rs.getString("content");
                Timestamp createdAt = rs.getTimestamp("created_at");

                VBox replyBox = new VBox(5);
                replyBox.getStyleClass().add("reply");
                replyBox.setId("reply-" + replyId);
                replyBox.setPadding(new Insets(8));
                replyBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 5; -fx-background-radius: 5;");

                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);

                Label authorLabel = new Label("↳ " + author);
                authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

                Label timeLabel = new Label(formatTimeAgo(createdAt.toLocalDateTime()));
                timeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");

                header.getChildren().addAll(authorLabel, timeLabel);

                Label message = new Label(content);
                message.setWrapText(true);
                message.setStyle("-fx-font-size: 12px; -fx-padding: 3 0;");

                replyBox.getChildren().addAll(header, message);
                repliesBox.getChildren().add(replyBox);
            }

            if (!repliesBox.getChildren().isEmpty()) {
                parentCommentBox.getChildren().add(repliesBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== AFFICHER LE CHAMP DE RÉPONSE ====================
    private void showReplyInput(VBox parentCommentBox, int postId, int parentCommentId, VBox commentsSection) {
        // Vérifier si un formulaire existe déjà
        for (Node child : parentCommentBox.getChildren()) {
            if (child instanceof HBox && child.getStyleClass().contains("reply-form")) {
                return;
            }
        }

        HBox replyForm = new HBox(10);
        replyForm.getStyleClass().add("reply-form");
        replyForm.setPadding(new Insets(8, 0, 0, 25));
        replyForm.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-border-radius: 5;");

        TextField replyInput = new TextField();
        replyInput.setPromptText("Écrire une réponse...");
        replyInput.setPrefWidth(300);
        replyInput.setStyle("-fx-padding: 8; -fx-background-radius: 5;");

        Button submitReply = new Button("Répondre");
        submitReply.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5;");
        submitReply.setOnAction(e -> {
            String content = replyInput.getText().trim();
            if (!content.isEmpty()) {
                addComment(postId, content, parentCommentId, commentsSection);
                parentCommentBox.getChildren().remove(replyForm);
            }
        });

        Button cancelReply = new Button("Annuler");
        cancelReply.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5;");
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

    // ==================== AJOUTER UN COMMENTAIRE OU UNE RÉPONSE ====================
    private void addComment(int postId, String content, Integer parentCommentId, VBox commentsSection) {
        if (!validateCommentInput(content)) return;

        String sql = "INSERT INTO comments (post_id, user_id, parent_comment_id, content) VALUES (?, ?, ?, ?)";

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

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int commentId = rs.getInt(1);
                Platform.runLater(() -> {
                    // Recharger les commentaires pour ce post
                    VBox newCommentsSection = new VBox(10);
                    newCommentsSection.getStyleClass().add("comments-section");
                    newCommentsSection.setId("comments-" + postId);

                    HBox commentForm = buildCommentForm(postId, newCommentsSection);
                    newCommentsSection.getChildren().add(commentForm);

                    loadCommentsForPost(postId, newCommentsSection);

                    // Remplacer l'ancienne section
                    for (Node node : feedContainer.getChildren()) {
                        if (node instanceof VBox && node.getId() != null && node.getId().equals("post-" + postId)) {
                            VBox postCard = (VBox) node;
                            for (int i = 0; i < postCard.getChildren().size(); i++) {
                                Node child = postCard.getChildren().get(i);
                                if (child instanceof VBox && child.getId() != null && child.getId().equals("comments-" + postId)) {
                                    boolean wasVisible = child.isVisible();
                                    newCommentsSection.setVisible(wasVisible);
                                    newCommentsSection.setManaged(wasVisible);
                                    postCard.getChildren().set(i, newCommentsSection);
                                    break;
                                }
                            }
                            break;
                        }
                    }

                    updateStatistics();
                    showInfo("✅ Commentaire ajouté !", "success");
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ajout du commentaire", "error");
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL non trouvé", e);
        }
    }

    private void createUploadDirectory() {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();
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