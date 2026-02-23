package com.recruitx.hrone;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javafx.scene.layout.Priority;

public class BlogController {
    // ==================== FXML EXISTANTS ====================
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private VBox feedContainer;
    @FXML
    private TextField postTitleField;
    @FXML
    private TextArea postDescriptionArea;
    @FXML
    private TextField postImageField;
    @FXML
    private Button clearPostButton;
    @FXML
    private Button submitPostButton;
    @FXML
    private Button chooseImageButton;

    // ==================== FXML POUR API ====================
    @FXML
    private Button randomImageBtn;
    @FXML
    private Button suggestTagsBtn;
    @FXML
    private Button analyzeBtn;
    @FXML
    private Button weatherBtn;
    @FXML
    private Button adviceBtn;
    @FXML
    private Button quoteBtn;
    @FXML
    private Button jokeBtn;
    @FXML
    private Button animalBtn;
    @FXML
    private Button translateBtn;
    @FXML
    private Button sentimentBtn;

    // ==================== FXML POUR MAIL (AJOUTÉS !) ====================
    @FXML
    private Button emailBtn;           // Bouton pour envoyer un email
    @FXML
    private Button newsletterBtn;       // Bouton pour newsletter

    // ==================== FXML POUR MÉTÉO ====================
    @FXML
    private Label weatherCityLabel;
    @FXML
    private Label weatherTempLabel;
    @FXML
    private Label weatherDescLabel;
    @FXML
    private Button refreshWeatherBtn;

    // ==================== FXML POUR CHATBOT (AJOUTÉS !) ====================
    @FXML
    private VBox chatContainer;         // Zone des messages
    @FXML
    private TextField chatInputField;    // Champ de saisie
    @FXML
    private Button sendChatBtn;          // Bouton d'envoi

    private final List<Node> postCards = new ArrayList<>();

    // Informations de connexion BD
    private static final String DB_URL = "jdbc:mysql://localhost:3306/blog_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    // Utilisateur actuel
    private int currentUserId = 1;
    private String currentUserName = "Lina M.";

    // Dossier pour stocker les images uploadées
    private static final String UPLOAD_DIR = "uploads/";

    // APIs Gratuites (sans clé requise)
    private static final String TRANSLATION_API = "https://api.mymemory.translated.net/get";
    private static final String WEATHER_API = "https://wttr.in/";
    private static final String RANDOM_IMAGE_API = "https://picsum.photos/600/400";
    private static final String JOKE_API = "https://v2.jokeapi.dev/joke/Any?type=single";
    private static final String QUOTE_API = "https://api.quotable.io/random";
    private static final String DOG_API = "https://dog.ceo/api/breeds/image/random";
    private static final String CAT_API = "https://api.thecatapi.com/v1/images/search";
    private static final String ADVICE_API = "https://api.adviceslip.com/advice";

    // ==================== INITIALIZATION ====================
    @FXML
    private void initialize() {
        // Créer le dossier uploads s'il n'existe pas
        createUploadDirectory();

        // Créer les tables si elles n'existent pas
        createTablesIfNotExist();

        loadPostsFromDatabase();
        wireSearch();
        wirePostForm();
        wireApiButtons();
        wireEmailButtons();    // AJOUTÉ : initialiser les boutons email
        wireChatbot();         // AJOUTÉ : initialiser le chatbot
        refreshWeather();      // Charger la météo au démarrage
        applyFilter();

        // Ajouter un message de bienvenue dans le chat
        Platform.runLater(() -> {
            addChatMessage("🤖 Bonjour ! Je suis votre assistant. Comment puis-je vous aider ?", "#0a1929", "white");
        });
    }

    // ==================== MÉTHODES DE WIRING ====================

    private void wireApiButtons() {
        if (randomImageBtn != null) {
            randomImageBtn.setOnAction(e -> getRandomImage(postImageField));
        }
        if (suggestTagsBtn != null) {
            suggestTagsBtn.setOnAction(e -> suggestTagsFromDescription());
        }
        if (analyzeBtn != null) {
            analyzeBtn.setOnAction(e -> analyzeSentiment());
        }
        if (weatherBtn != null) {
            weatherBtn.setOnAction(e -> showWeather());
        }
        if (adviceBtn != null) {
            adviceBtn.setOnAction(e -> showAdvice());
        }
        if (quoteBtn != null) {
            quoteBtn.setOnAction(e -> showRandomQuote());
        }
        if (jokeBtn != null) {
            jokeBtn.setOnAction(e -> showRandomJoke());
        }
        if (animalBtn != null) {
            animalBtn.setOnAction(e -> showRandomAnimal());
        }
        if (translateBtn != null) {
            translateBtn.setOnAction(e -> translateCurrentPost());
        }
        if (sentimentBtn != null) {
            sentimentBtn.setOnAction(e -> analyzeSentiment());
        }
        if (refreshWeatherBtn != null) {
            refreshWeatherBtn.setOnAction(e -> refreshWeather());
        }
    }

    // ==================== NOUVEAU : EMAIL BUTTONS ====================
    private void wireEmailButtons() {
        if (emailBtn != null) {
            emailBtn.setOnAction(e -> sendEmail());
            System.out.println("✅ Bouton email initialisé");
        }
        if (newsletterBtn != null) {
            newsletterBtn.setOnAction(e -> sendNewsletter());
            System.out.println("✅ Bouton newsletter initialisé");
        }
    }

    // ==================== NOUVEAU : CHATBOT ====================
    private void wireChatbot() {
        if (sendChatBtn != null) {
            sendChatBtn.setOnAction(e -> sendChatMessage());
            System.out.println("✅ Bouton chat initialisé");
        }
        if (chatInputField != null) {
            chatInputField.setOnAction(e -> sendChatMessage());
        }
    }

    // ==================== MÉTHODES EMAIL ====================
    private void sendEmail() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("📧 Envoyer un email");
        dialog.setHeaderText("Partager ce post par email");
        dialog.setContentText("Adresse email du destinataire:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            if (isValidEmail(email)) {
                String subject = "Partage de post: " + postTitleField.getText();
                String content = postDescriptionArea.getText();
                showInfo("✅ Email envoyé à " + email + " avec succès!\n\nSujet: " + subject + "\n\nContenu:\n" + content);

                // Ici vous pourriez ajouter un vrai service d'envoi d'email
                // Par exemple avec JavaMail API
            } else {
                showError("❌ Adresse email invalide");
            }
        });
    }

    private void sendNewsletter() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("📰 Newsletter");
        dialog.setHeaderText("Inscription à la newsletter HR One");
        dialog.setContentText("Votre email:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            if (isValidEmail(email)) {
                showInfo("✅ Inscription réussie pour " + email + "\n\nVous recevrez nos actualités chaque semaine !");
                // Ici vous pourriez sauvegarder l'email dans la base de données
                sauvegarderEmailNewsletter(email);
            } else {
                showError("❌ Adresse email invalide");
            }
        });
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private void sauvegarderEmailNewsletter(String email) {
        // Optionnel: sauvegarder dans la base de données
        String sql = "INSERT INTO newsletter_emails (email) VALUES (?) ON DUPLICATE KEY UPDATE email = email";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
            System.out.println("✅ Email newsletter sauvegardé: " + email);
        } catch (SQLException e) {
            System.err.println("❌ Erreur sauvegarde email: " + e.getMessage());
        }
    }

    // ==================== MÉTHODES CHATBOT ====================
    private void sendChatMessage() {
        String message = chatInputField.getText().trim();
        if (message.isEmpty()) return;

        // Message de l'utilisateur
        addChatMessage("👤 Vous: " + message, "#e2eafc", "#0a1929");
        chatInputField.clear();

        // Simuler une réponse (le bot réfléchit)
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(500); // Petit délai pour simuler la réflexion
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Réponse du bot
            String response = getBotResponse(message);
            Platform.runLater(() -> {
                addChatMessage("🤖 Assistant: " + response, "#0a1929", "white");
            });
        });
    }

    private void addChatMessage(String text, String bgColor, String textColor) {
        Label msg = new Label(text);
        msg.setWrapText(true);
        msg.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor +
                "; -fx-padding: 12 15; -fx-background-radius: 18; -fx-font-size: 13px;");
        msg.setMaxWidth(280);

        // Aligner les messages (utilisateur à droite, bot à gauche)
        HBox messageBox = new HBox(msg);
        if (text.startsWith("👤 Vous:")) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);
        }
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        chatContainer.getChildren().add(messageBox);

        // Auto-scroll (si chatContainer est dans un ScrollPane)
        if (chatContainer.getParent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) chatContainer.getParent();
            scrollPane.setVvalue(1.0);
        }
    }

    private String getBotResponse(String message) {
        message = message.toLowerCase();

        if (message.contains("bonjour") || message.contains("salut") || message.contains("hello") || message.contains("bonsoir")) {
            return "Bonjour ! Comment puis-je vous aider aujourd'hui ?";

        } else if (message.contains("post") || message.contains("publier")) {
            return "Pour publier un post, remplissez le formulaire à gauche avec un titre et une description, puis cliquez sur 'Publier'.";

        } else if (message.contains("commentaire") || message.contains("commenter")) {
            return "Vous pouvez commenter les posts en cliquant sur le bouton 💬 sous chaque post, puis en écrivant votre commentaire.";

        } else if (message.contains("répondre") || message.contains("reply")) {
            return "Pour répondre à un commentaire, cliquez sur 'Reply' sous le commentaire concerné.";

        } else if (message.contains("météo") || message.contains("temps") || message.contains("temperature")) {
            return "La météo actuelle à Paris est " + weatherTempLabel.getText() + " avec " + weatherDescLabel.getText() + ". Vous pouvez actualiser avec le bouton 🔄.";

        } else if (message.contains("tag") || message.contains("étiquette") || message.contains("suggestion")) {
            return "Utilisez le bouton '🏷️ Suggest Tags' pour obtenir des suggestions de tags basées sur votre description.";

        } else if (message.contains("image") || message.contains("photo")) {
            return "Vous pouvez ajouter une image à votre post en cliquant sur 'Choose Image' ou utiliser '🎲 Random Image' pour une image aléatoire.";

        } else if (message.contains("traduction") || message.contains("translate") || message.contains("anglais")) {
            return "Cliquez sur '🌐 Traduire le post' pour traduire votre texte en anglais.";

        } else if (message.contains("blague") || message.contains("joke") || message.contains("humour")) {
            return "Pour une blague, cliquez sur le bouton '😂 Blague aléatoire' dans la section APIs !";

        } else if (message.contains("citation") || message.contains("quote") || message.contains("inspiration")) {
            return "Le bouton '📝 Citation inspirante' vous donnera une citation du jour !";

        } else if (message.contains("animal") || message.contains("chien") || message.contains("chat") || message.contains("mignon")) {
            return "Cliquez sur '🐕 Image d\\'animal' pour voir des photos mignonnes de chiens ou chats !";

        } else if (message.contains("email") || message.contains("mail")) {
            return "Vous pouvez envoyer un email ou vous inscrire à la newsletter avec les boutons 📧 et 📰 dans la section APIs.";

        } else if (message.contains("conseil") || message.contains("advice")) {
            return "Le bouton '💡 Conseil du jour' vous donnera un conseil pour votre vie professionnelle.";

        } else if (message.contains("sentiment") || message.contains("analyse") || message.contains("ton")) {
            return "Utilisez '📊 Analyze' ou '😊 Analyser le ton' pour analyser le sentiment de votre texte.";

        } else if (message.contains("aide") || message.contains("help") || message.contains("?")) {
            return "Je peux vous aider avec :\n" +
                    "• Publier des posts\n" +
                    "• Commenter et répondre\n" +
                    "• Utiliser les APIs (météo, blagues, citations...)\n" +
                    "• Envoyer des emails\n" +
                    "• Voir la météo\n" +
                    "• Analyser le sentiment\n" +
                    "• Et bien plus ! Tapez un mot clé pour plus d'infos.";

        } else if (message.contains("merci")) {
            return "Avec plaisir ! N'hésitez pas si vous avez d'autres questions.";

        } else if (message.contains("au revoir") || message.contains("bye") || message.contains("à bientôt")) {
            return "Au revoir ! Bonne journée à vous !";

        } else if (message.contains("bonne nuit")) {
            return "Bonne nuit ! Faites de beaux rêves et à demain !";

        } else {
            return "Intéressant ! Pouvez-vous être plus précis ? Ou tapez 'aide' pour voir ce que je peux faire.\n\n" +
                    "Quelques suggestions : post, commentaire, météo, email, blague, citation, image...";
        }
    }

    // ==================== MÉTÉO ====================
    private void refreshWeather() {
        CompletableFuture.runAsync(() -> {
            try {
                String city = "Paris";
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
                            weatherCityLabel.setText("Paris");
                            weatherTempLabel.setText(parts[1]);
                            weatherDescLabel.setText(parts[0].replace("+", "").replace("c", "°C"));
                        }
                    });
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    weatherCityLabel.setText("Paris");
                    weatherTempLabel.setText("18°C");
                    weatherDescLabel.setText("Ensoleillé");
                });
            }
        });
    }

    // ==================== MÉTHODES DE CRÉATION ====================

    private void createUploadDirectory() {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("📁 Dossier uploads créé");
        }
    }

    private void createTablesIfNotExist() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INT PRIMARY KEY AUTO_INCREMENT,
                display_name VARCHAR(100) NOT NULL,
                email VARCHAR(100) UNIQUE,
                avatar_url VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createPostsTable = """
            CREATE TABLE IF NOT EXISTS posts (
                id INT PRIMARY KEY AUTO_INCREMENT,
                user_id INT NOT NULL,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                image_url VARCHAR(255),
                tag VARCHAR(50) DEFAULT 'General',
                vote_count INT DEFAULT 0,
                is_active BOOLEAN DEFAULT true,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createCommentsTable = """
            CREATE TABLE IF NOT EXISTS comments (
                id INT PRIMARY KEY AUTO_INCREMENT,
                post_id INT NOT NULL,
                user_id INT NOT NULL,
                parent_comment_id INT NULL,
                content TEXT NOT NULL,
                is_active BOOLEAN DEFAULT true,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createVotesTable = """
            CREATE TABLE IF NOT EXISTS votes (
                id INT PRIMARY KEY AUTO_INCREMENT,
                post_id INT NOT NULL,
                user_id INT NOT NULL,
                vote_type ENUM('up', 'down') DEFAULT 'up',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY unique_vote (post_id, user_id)
            )
        """;

        // Table pour la newsletter (AJOUTÉE)
        String createNewsletterTable = """
            CREATE TABLE IF NOT EXISTS newsletter_emails (
                id INT PRIMARY KEY AUTO_INCREMENT,
                email VARCHAR(100) UNIQUE NOT NULL,
                subscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsersTable);
            stmt.execute(createPostsTable);
            stmt.execute(createCommentsTable);
            stmt.execute(createVotesTable);
            stmt.execute(createNewsletterTable);

            System.out.println("✅ Tables vérifiées/créées avec succès");

            // Insérer un utilisateur de test si la table est vide
            String checkUsers = "SELECT COUNT(*) FROM users";
            ResultSet rs = stmt.executeQuery(checkUsers);
            if (rs.next() && rs.getInt(1) == 0) {
                String insertUser = """
                    INSERT INTO users (display_name, email) VALUES 
                    ('Lina M.', 'lina@example.com'),
                    ('Jean D.', 'jean@example.com'),
                    ('Marie C.', 'marie@example.com')
                """;
                stmt.execute(insertUser);
                System.out.println("✅ Utilisateurs de test insérés");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création des tables: " + e.getMessage());
        }
    }

    // ==================== API METHODS ====================

    private void getRandomImage(TextField imageField) {
        try {
            String fileName = "random_" + System.currentTimeMillis() + ".jpg";
            String imageUrl = RANDOM_IMAGE_API + "?random=" + System.currentTimeMillis();

            URL url = new URL(imageUrl);
            Path destPath = Path.of(UPLOAD_DIR, fileName);
            Files.copy(url.openStream(), destPath, StandardCopyOption.REPLACE_EXISTING);

            imageField.setText("uploads/" + fileName);
            showInfo("✅ Image aléatoire téléchargée avec succès!");
        } catch (Exception e) {
            showError("Erreur lors du téléchargement de l'image: " + e.getMessage());
        }
    }

    private void suggestTagsFromDescription() {
        String description = postDescriptionArea.getText();
        if (description.isEmpty()) {
            showError("Veuillez d'abord écrire une description");
            return;
        }

        List<String> suggestedTags = new ArrayList<>();
        String lowerDesc = description.toLowerCase();

        if (lowerDesc.contains("onboarding") || lowerDesc.contains("new hire") || lowerDesc.contains("recrut")) {
            suggestedTags.add("Onboarding");
        }
        if (lowerDesc.contains("benefit") || lowerDesc.contains("avantage") || lowerDesc.contains("salaire")) {
            suggestedTags.add("Benefits");
        }
        if (lowerDesc.contains("culture") || lowerDesc.contains("team") || lowerDesc.contains("équipe")) {
            suggestedTags.add("Culture");
        }
        if (lowerDesc.contains("remote") || lowerDesc.contains("work from home") || lowerDesc.contains("télétravail")) {
            suggestedTags.add("Remote");
        }
        if (lowerDesc.contains("tool") || lowerDesc.contains("software") || lowerDesc.contains("logiciel")) {
            suggestedTags.add("Tools");
        }
        if (lowerDesc.contains("meeting") || lowerDesc.contains("réunion") || lowerDesc.contains("réu")) {
            suggestedTags.add("Meetings");
        }
        if (lowerDesc.contains("feedback") || lowerDesc.contains("retour") || lowerDesc.contains("avis")) {
            suggestedTags.add("Feedback");
        }
        if (lowerDesc.contains("training") || lowerDesc.contains("formation") || lowerDesc.contains("learn")) {
            suggestedTags.add("Training");
        }
        if (suggestedTags.isEmpty()) {
            suggestedTags.add("General");
            suggestedTags.add("Discussion");
            suggestedTags.add("Ideas");
        }

        String tags = String.join(", ", suggestedTags);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tags suggérés");
        alert.setHeaderText("Voici des tags suggérés pour votre post:");
        alert.setContentText(tags);
        alert.showAndWait();
    }

    private void showWeather() {
        CompletableFuture.runAsync(() -> {
            try {
                String city = "Paris";
                String urlString = WEATHER_API + city + "?format=%c+%t+%w+%m&lang=fr";
                URL url = new URL(urlString);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = in.readLine();
                in.close();

                String finalResponse = response != null ? response.trim() : "Données non disponibles";
                Platform.runLater(() -> {
                    showInfo("☀️ Météo à " + city + ":\n\n" + finalResponse);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showInfo("☀️ Météo (simulée):\n\nParis: 18°C, Nuageux");
                });
            }
        });
    }

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
                } else {
                    advice = "Prenez le temps d'écouter les idées de vos collègues.";
                }

                String finalAdvice = advice;
                Platform.runLater(() -> {
                    showInfo("💡 Conseil du jour:\n\n" + finalAdvice);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showInfo("💡 Conseil du jour:\n\nPrenez le temps d'écouter les idées de vos collègues.");
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
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseStr = response.toString();
                String joke = "Pourquoi les programmeurs préfèrent le mode nuit?\nParce que la lumière attire les bugs!";

                if (responseStr.contains("\"joke\":")) {
                    int start = responseStr.indexOf("\"joke\":\"") + 8;
                    int end = responseStr.indexOf("\"", start);
                    if (start > 8 && end > start) {
                        joke = responseStr.substring(start, end);
                    }
                }

                String finalJoke = joke;
                Platform.runLater(() -> {
                    showInfo("😂 Blague du jour:\n\n" + finalJoke);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showInfo("😂 Blague du jour:\n\nPourquoi les programmeurs préfèrent le mode nuit?\nParce que la lumière attire les bugs!");
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
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseStr = response.toString();
                String quote = "Le succès n'est pas final, l'échec n'est pas fatal.";
                String author = "Winston Churchill";

                if (responseStr.contains("\"content\":")) {
                    int start = responseStr.indexOf("\"content\":\"") + 11;
                    int end = responseStr.indexOf("\"", start);
                    if (start > 11 && end > start) {
                        quote = responseStr.substring(start, end);
                    }
                }
                if (responseStr.contains("\"author\":")) {
                    int start = responseStr.indexOf("\"author\":\"") + 10;
                    int end = responseStr.indexOf("\"", start);
                    if (start > 10 && end > start) {
                        author = responseStr.substring(start, end);
                    }
                }

                String finalQuote = quote;
                String finalAuthor = author;
                Platform.runLater(() -> {
                    showInfo("📝 Citation inspirante:\n\n\"" + finalQuote + "\"\n\n- " + finalAuthor);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showInfo("📝 Citation inspirante:\n\n\"Le succès n'est pas final, l'échec n'est pas fatal.\"\n\n- Winston Churchill");
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
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseStr = response.toString();
                String imageUrl = "";

                if (isDog) {
                    if (responseStr.contains("\"message\":\"")) {
                        int start = responseStr.indexOf("\"message\":\"") + 11;
                        int end = responseStr.indexOf("\"", start);
                        if (start > 11 && end > start) {
                            imageUrl = responseStr.substring(start, end);
                        }
                    }
                } else {
                    if (responseStr.contains("\"url\":\"")) {
                        int start = responseStr.indexOf("\"url\":\"") + 7;
                        int end = responseStr.indexOf("\"", start);
                        if (start > 7 && end > start) {
                            imageUrl = responseStr.substring(start, end);
                        }
                    }
                }

                if (imageUrl.isEmpty()) {
                    imageUrl = "https://images.dog.ceo/breeds/hound-afghan/n02088094_1003.jpg";
                }

                String finalImageUrl = imageUrl;
                Platform.runLater(() -> {
                    showImagePopup(finalImageUrl, isDog ? "🐕 Chien mignon" : "🐱 Chat mignon");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showInfo("Image d'animal non disponible pour le moment");
                });
            }
        });
    }

    private void translateCurrentPost() {
        String text = postDescriptionArea.getText();
        if (text.isEmpty()) {
            text = "Hello, how are you?";
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
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseStr = response.toString();
                String translatedText = "[Traduction non disponible]";

                if (responseStr.contains("\"translatedText\":\"")) {
                    int start = responseStr.indexOf("\"translatedText\":\"") + 18;
                    int end = responseStr.indexOf("\"", start);
                    if (start > 18 && end > start) {
                        translatedText = responseStr.substring(start, end);
                        // Remplacer les codes Unicode
                        translatedText = translatedText.replace("\\u00e9", "é")
                                .replace("\\u00e8", "è")
                                .replace("\\u00ea", "ê")
                                .replace("\\u00e0", "à")
                                .replace("\\u00f4", "ô")
                                .replace("\\u00ee", "î")
                                .replace("\\u00fb", "û");
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
        dialog.setHeaderText("Texte original et traduction");

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
            showError("Veuillez écrire du texte à analyser");
            return;
        }

        String lowerText = text.toLowerCase();
        int positiveWords = 0;
        int negativeWords = 0;

        String[] positive = {"good", "great", "excellent", "awesome", "love", "happy", "super", "👍", "génial", "merveilleux", "excellent", "parfait", "bien", "magnifique"};
        String[] negative = {"bad", "terrible", "awful", "hate", "sad", "angry", "problem", "issue", "👎", "mauvais", "horrible", "problème", "déteste"};

        for (String word : positive) {
            if (lowerText.contains(word)) positiveWords++;
        }
        for (String word : negative) {
            if (lowerText.contains(word)) negativeWords++;
        }

        String sentiment;
        Color color;
        String emoji;

        if (positiveWords > negativeWords) {
            sentiment = "POSITIF";
            color = Color.GREEN;
            emoji = "😊";
        } else if (negativeWords > positiveWords) {
            sentiment = "NÉGATIF";
            color = Color.RED;
            emoji = "😔";
        } else {
            sentiment = "NEUTRE";
            color = Color.GRAY;
            emoji = "😐";
        }

        String stats = "Mots positifs: " + positiveWords + "\nMots négatifs: " + negativeWords;
        String result = sentiment + " " + emoji + "\n\n" + stats;

        Label resultLabel = new Label(result);
        resultLabel.setTextFill(color);
        resultLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 20px;");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Analyse de sentiment");
        dialog.setHeaderText("Résultat de l'analyse");
        dialog.getDialogPane().setContent(resultLabel);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        dialog.showAndWait();
    }

    private void showImagePopup(String imageUrl, String title) {
        try {
            Popup popup = new Popup();
            VBox content = new VBox(10);
            content.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 10; -fx-padding: 20; -fx-background-radius: 10;");
            content.setEffect(new DropShadow());

            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

            ImageView imageView = new ImageView();
            imageView.setFitWidth(400);
            imageView.setPreserveRatio(true);

            try {
                Image image = new Image(imageUrl, true);
                imageView.setImage(image);
            } catch (Exception e) {
                imageView.setImage(new Image("https://via.placeholder.com/400x300?text=Image+non+disponible"));
            }

            Button closeBtn = new Button("Fermer");
            closeBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-padding: 8 20; -fx-border-radius: 5;");
            closeBtn.setOnAction(e -> popup.hide());

            content.getChildren().addAll(titleLabel, imageView, closeBtn);
            popup.getContent().add(content);

            popup.show(feedContainer.getScene().getWindow());
        } catch (Exception e) {
            showError("Erreur lors de l'affichage de l'image");
        }
    }

    // ==================== MÉTHODES POST ====================

    private void loadPostsFromDatabase() {
        feedContainer.getChildren().clear();

        // Garder le formulaire de création de post
        Node postForm = createPostForm();
        if (postForm != null) {
            feedContainer.getChildren().add(postForm);
        }

        String sql = "SELECT p.*, u.display_name as author_name, u.avatar_url, " +
                "COUNT(DISTINCT c.id) as comment_count " +
                "FROM posts p " +
                "JOIN users u ON p.user_id = u.id " +
                "LEFT JOIN comments c ON p.id = c.post_id AND c.is_active = true " +
                "WHERE p.is_active = true " +
                "GROUP BY p.id " +
                "ORDER BY p.created_at DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                VBox postCard = buildPostCardFromDB(rs);
                feedContainer.getChildren().add(postCard);
                postCards.add(postCard);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement des posts: " + e.getMessage());
        }
    }

    private Node createPostForm() {
        VBox formCard = new VBox();
        formCard.getStyleClass().addAll("panel-card", "post-form-card");
        formCard.setPadding(new Insets(20));
        formCard.setSpacing(15);
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label title = new Label("Create a post");
        title.getStyleClass().add("section-title");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Share a title, describe your idea, and add an optional image.");
        subtitle.getStyleClass().add("muted");
        subtitle.setStyle("-fx-text-fill: #7f8c8d;");
        subtitle.setWrapText(true);

        VBox form = new VBox();
        form.getStyleClass().add("post-form");
        form.setSpacing(15);

        // Champ Titre
        VBox titleField = new VBox(5);
        titleField.getStyleClass().add("field");
        Label titleLabel = new Label("Title");
        titleLabel.setStyle("-fx-font-weight: bold;");
        TextField titleInput = new TextField();
        titleInput.setPromptText("Ex: Improve onboarding for new hires");
        titleInput.setId("postTitleField");
        titleInput.setStyle("-fx-padding: 10; -fx-background-radius: 5;");
        titleField.getChildren().addAll(titleLabel, titleInput);

        // Champ Description
        VBox descField = new VBox(5);
        descField.getStyleClass().add("field");
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-font-weight: bold;");
        TextArea descInput = new TextArea();
        descInput.setPromptText("Describe your idea or feedback...");
        descInput.setPrefRowCount(4);
        descInput.setId("postDescriptionArea");
        descInput.setStyle("-fx-padding: 10; -fx-background-radius: 5;");
        descField.getChildren().addAll(descLabel, descInput);

        // Champ Image avec boutons
        VBox imageField = new VBox(5);
        imageField.getStyleClass().add("field");
        Label imageLabel = new Label("Image (optional)");
        imageLabel.setStyle("-fx-font-weight: bold;");
        HBox imageBox = new HBox(8);
        TextField imageInput = new TextField();
        imageInput.setPromptText("No file chosen");
        imageInput.setId("postImageField");
        imageInput.setPrefWidth(300);
        imageInput.setEditable(false);
        imageInput.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");

        Button chooseBtn = new Button("Choose Image");
        chooseBtn.getStyleClass().addAll("btn", "btn-ghost");
        chooseBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 5;");
        chooseBtn.setOnAction(e -> chooseImage(imageInput));

        Button randomImageBtn = new Button("🎲 Random Image");
        randomImageBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 5;");
        randomImageBtn.setOnAction(e -> getRandomImage(imageInput));

        imageBox.getChildren().addAll(imageInput, chooseBtn, randomImageBtn);
        imageField.getChildren().addAll(imageLabel, imageBox);

        // Boutons d'actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button suggestTagsBtn = new Button("🏷️ Suggest Tags");
        suggestTagsBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 5; -fx-font-weight: bold;");
        suggestTagsBtn.setOnAction(e -> suggestTagsFromDescription());

        Button analyzeBtn = new Button("📊 Analyze");
        analyzeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 5; -fx-font-weight: bold;");
        analyzeBtn.setOnAction(e -> analyzeSentiment());

        Button clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 5;");
        clearBtn.setId("clearPostButton");

        Button submitBtn = new Button("Post");
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
        fileChooser.setTitle("Choose an image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destPath = Path.of(UPLOAD_DIR, fileName);
                Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                imageField.setText("uploads/" + fileName);
                showInfo("✅ Image téléchargée avec succès!");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur lors de l'upload de l'image: " + e.getMessage());
            }
        }
    }

    private VBox buildPostCardFromDB(ResultSet rs) throws SQLException {
        int postId = rs.getInt("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String imageUrl = rs.getString("image_url");
        String tag = rs.getString("tag");
        int voteCount = rs.getInt("vote_count");
        String authorName = rs.getString("author_name");
        String avatarUrl = rs.getString("avatar_url");
        Timestamp createdAt = rs.getTimestamp("created_at");
        int commentCount = rs.getInt("comment_count");

        String timeAgo = formatTimeAgo(createdAt.toLocalDateTime());

        VBox postCard = new VBox();
        postCard.getStyleClass().add("post-card");
        postCard.setId("post-" + postId);
        postCard.setSpacing(10);
        postCard.setPadding(new Insets(15));
        postCard.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // En-tête du post
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        ImageView avatar = new ImageView();
        avatar.setFitHeight(40);
        avatar.setFitWidth(40);
        avatar.setPreserveRatio(true);
        avatar.getStyleClass().add("avatar");
        avatar.setStyle("-fx-background-radius: 20; -fx-border-radius: 20; -fx-background-color: #3498db;");
        try {
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                avatar.setImage(new Image(avatarUrl));
            } else {
                Image defaultAvatar = getClass().getResourceAsStream("/default-avatar.png") != null ?
                        new Image(getClass().getResourceAsStream("/default-avatar.png")) : null;
                if (defaultAvatar != null) {
                    avatar.setImage(defaultAvatar);
                }
            }
        } catch (Exception e) {
            // Pas d'avatar, tant pis
        }

        VBox authorInfo = new VBox(2);
        Label authorLabel = new Label(authorName);
        authorLabel.getStyleClass().add("author-name");
        authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label timeLabel = new Label(timeAgo);
        timeLabel.getStyleClass().add("time-ago");
        timeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        authorInfo.getChildren().addAll(authorLabel, timeLabel);
        header.getChildren().addAll(avatar, authorInfo);

        // Tag
        Label tagLabel = new Label(tag);
        tagLabel.getStyleClass().addAll("tag", "tag-" + tag.toLowerCase());
        tagLabel.setStyle("-fx-background-color: #e0e0ff; -fx-padding: 5 15; -fx-border-radius: 20; -fx-background-radius: 20;");

        // Titre
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("post-title");
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Image du post (si elle existe)
        ImageView postImage = null;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                postImage = new ImageView();
                File imageFile = new File(imageUrl);
                if (imageFile.exists()) {
                    postImage.setImage(new Image(imageFile.toURI().toString()));
                } else {
                    postImage.setImage(new Image(imageUrl));
                }
                postImage.setFitWidth(600);
                postImage.setPreserveRatio(true);
                postImage.setStyle("-fx-padding: 10 0 10 0; -fx-border-radius: 5;");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Description
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("post-text");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-padding: 10 0 10 0; -fx-text-fill: #34495e;");

        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setStyle("-fx-padding: 5 0;");

        Button voteBtn = new Button("👍 " + voteCount);
        voteBtn.getStyleClass().add("vote-btn");
        voteBtn.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-font-size: 14px;");
        voteBtn.setOnAction(e -> handleVote(postId, voteBtn));

        Button commentBtn = new Button("💬 " + commentCount);
        commentBtn.getStyleClass().add("comment-btn");
        commentBtn.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 8 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-font-size: 14px;");
        commentBtn.setOnAction(e -> toggleComments(postCard));

        Button editBtn = new Button("✏️ Edit");
        editBtn.getStyleClass().add("edit-btn");
        editBtn.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 8 15; -fx-border-radius: 25; -fx-background-radius: 25;");
        editBtn.setOnAction(e -> showEditPostDialog(postId, title, description));

        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.getStyleClass().add("delete-btn");
        deleteBtn.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 8 15; -fx-border-radius: 25; -fx-background-radius: 25;");
        deleteBtn.setOnAction(e -> handleDeletePost(postId, postCard));

        actions.getChildren().addAll(voteBtn, commentBtn, editBtn, deleteBtn);

        // Section commentaires (cachée par défaut)
        VBox commentsSection = new VBox(10);
        commentsSection.getStyleClass().add("comments-section");
        commentsSection.setId("comments-" + postId);
        commentsSection.setVisible(false);
        commentsSection.setManaged(false);
        commentsSection.setPadding(new Insets(10, 0, 0, 0));
        commentsSection.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-border-radius: 5;");

        // Formulaire de commentaire
        HBox commentForm = buildCommentForm(postId, commentsSection);
        commentsSection.getChildren().add(commentForm);

        // Charger les commentaires existants
        loadCommentsForPost(postId, commentsSection);

        // Ajouter tous les éléments au post
        postCard.getChildren().add(header);
        postCard.getChildren().add(tagLabel);
        postCard.getChildren().add(titleLabel);
        if (postImage != null) {
            postCard.getChildren().add(postImage);
        }
        postCard.getChildren().add(descriptionLabel);
        postCard.getChildren().add(actions);
        postCard.getChildren().add(commentsSection);

        return postCard;
    }

    private HBox buildCommentForm(int postId, VBox commentsSection) {
        HBox form = new HBox(10);
        form.getStyleClass().add("comment-form");
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(5));
        form.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10;");

        TextField commentInput = new TextField();
        commentInput.setPromptText("Write a comment...");
        commentInput.setPrefWidth(400);
        commentInput.setStyle("-fx-padding: 10; -fx-background-radius: 5;");
        HBox.setHgrow(commentInput, Priority.ALWAYS);

        Button submitComment = new Button("Post Comment");
        submitComment.getStyleClass().addAll("btn", "btn-primary");
        submitComment.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-weight: bold;");
        submitComment.setOnAction(e -> addComment(postId, commentInput.getText(), null, commentsSection));

        commentInput.setOnAction(e -> addComment(postId, commentInput.getText(), null, commentsSection));

        form.getChildren().addAll(commentInput, submitComment);
        return form;
    }

    private void loadCommentsForPost(int postId, VBox commentsSection) {
        String sql = "SELECT c.*, u.display_name as author_name, u.avatar_url " +
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

    private VBox buildCommentFromDB(ResultSet rs, VBox commentsSection) throws SQLException {
        int commentId = rs.getInt("id");
        String author = rs.getString("author_name");
        String content = rs.getString("content");
        Timestamp createdAt = rs.getTimestamp("created_at");
        int postId = rs.getInt("post_id");

        VBox commentBox = new VBox(5);
        commentBox.getStyleClass().add("comment");
        commentBox.setId("comment-" + commentId);
        commentBox.setPadding(new Insets(10));
        commentBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 0 3; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: white;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label(author);
        authorLabel.getStyleClass().add("comment-author");
        authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label timeLabel = new Label(formatTimeAgo(createdAt.toLocalDateTime()));
        timeLabel.getStyleClass().add("comment-time");
        timeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        header.getChildren().addAll(authorLabel, timeLabel);

        Label message = new Label(content);
        message.getStyleClass().add("comment-text");
        message.setWrapText(true);
        message.setStyle("-fx-padding: 5 0; -fx-font-size: 13px;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button replyBtn = new Button("↩️ Reply");
        replyBtn.getStyleClass().add("reply-btn");
        replyBtn.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5 15; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-size: 11px;");
        replyBtn.setOnAction(e -> showReplyInput(commentBox, postId, commentId, commentsSection));

        Button editBtn = new Button("✏️ Edit");
        editBtn.getStyleClass().add("edit-btn");
        editBtn.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5 15; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-size: 11px;");
        editBtn.setOnAction(e -> editComment(commentId, message, commentBox));

        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.getStyleClass().add("delete-btn");
        deleteBtn.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5 15; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-size: 11px;");
        deleteBtn.setOnAction(e -> deleteComment(commentId, commentBox));

        actions.getChildren().addAll(replyBtn, editBtn, deleteBtn);

        commentBox.getChildren().addAll(header, message, actions);

        // Charger les réponses
        loadReplies(commentId, commentBox, postId, commentsSection);

        return commentBox;
    }

    private void loadReplies(int parentCommentId, VBox parentCommentBox, int postId, VBox commentsSection) {
        String sql = "SELECT c.*, u.display_name as author_name, u.avatar_url " +
                "FROM comments c " +
                "JOIN users u ON c.user_id = u.id " +
                "WHERE c.parent_comment_id = ? AND c.is_active = true " +
                "ORDER BY c.created_at ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, parentCommentId);
            ResultSet rs = stmt.executeQuery();

            VBox repliesBox = new VBox(5);
            repliesBox.getStyleClass().add("replies");
            repliesBox.setPadding(new Insets(5, 0, 0, 20));

            while (rs.next()) {
                int replyId = rs.getInt("id");
                String author = rs.getString("author_name");
                String content = rs.getString("content");
                Timestamp createdAt = rs.getTimestamp("created_at");

                VBox replyBox = new VBox(3);
                replyBox.getStyleClass().add("reply");
                replyBox.setId("reply-" + replyId);
                replyBox.setPadding(new Insets(5));
                replyBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 8; -fx-border-radius: 5;");

                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);

                Label authorLabel = new Label("↳ " + author);
                authorLabel.getStyleClass().add("reply-author");
                authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

                Label timeLabel = new Label(formatTimeAgo(createdAt.toLocalDateTime()));
                timeLabel.getStyleClass().add("reply-time");
                timeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");

                header.getChildren().addAll(authorLabel, timeLabel);

                Label message = new Label(content);
                message.getStyleClass().add("reply-text");
                message.setWrapText(true);
                message.setStyle("-fx-font-size: 12px; -fx-padding: 5 0;");

                HBox actions = new HBox(10);
                Button editReply = new Button("Edit");
                editReply.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 3 10; -fx-border-radius: 15; -fx-background-radius: 15; -fx-font-size: 10px;");
                Button deleteReply = new Button("Delete");
                deleteReply.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 3 10; -fx-border-radius: 15; -fx-background-radius: 15; -fx-font-size: 10px;");
                editReply.setOnAction(e -> editComment(replyId, message, replyBox));
                deleteReply.setOnAction(e -> deleteComment(replyId, replyBox));
                actions.getChildren().addAll(editReply, deleteReply);

                replyBox.getChildren().addAll(header, message, actions);
                repliesBox.getChildren().add(replyBox);
            }

            if (!repliesBox.getChildren().isEmpty()) {
                parentCommentBox.getChildren().add(repliesBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showReplyInput(VBox parentCommentBox, int postId, int parentCommentId, VBox commentsSection) {
        // Vérifier si un formulaire de réponse existe déjà
        for (Node child : parentCommentBox.getChildren()) {
            if (child instanceof HBox && child.getStyleClass().contains("reply-form")) {
                return;
            }
        }

        HBox replyForm = new HBox(10);
        replyForm.getStyleClass().add("reply-form");
        replyForm.setPadding(new Insets(5, 0, 0, 20));
        replyForm.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-border-radius: 5;");

        TextField replyInput = new TextField();
        replyInput.setPromptText("Write a reply...");
        replyInput.setPrefWidth(300);
        replyInput.setStyle("-fx-padding: 8; -fx-background-radius: 5;");

        Button submitReply = new Button("Reply");
        submitReply.getStyleClass().addAll("btn", "btn-primary");
        submitReply.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5;");
        submitReply.setOnAction(e -> {
            addComment(postId, replyInput.getText(), parentCommentId, commentsSection);
            parentCommentBox.getChildren().remove(replyForm);
        });

        Button cancelReply = new Button("Cancel");
        cancelReply.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5;");
        cancelReply.setOnAction(e -> parentCommentBox.getChildren().remove(replyForm));

        replyForm.getChildren().addAll(replyInput, submitReply, cancelReply);
        parentCommentBox.getChildren().add(replyForm);
    }

    private void addComment(int postId, String content, Integer parentCommentId, VBox commentsSection) {
        if (content.trim().isEmpty()) {
            showError("Le commentaire ne peut pas être vide");
            return;
        }

        String sql = "INSERT INTO comments (post_id, user_id, parent_comment_id, content) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, postId);
            stmt.setInt(2, currentUserId);

            if (parentCommentId != null && parentCommentId > 0) {
                stmt.setInt(3, parentCommentId);
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setString(4, content.trim());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La création du commentaire a échoué");
            }

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int commentId = rs.getInt(1);

                if (parentCommentId == null) {
                    // Commentaire principal
                    addCommentToUI(commentId, postId, content, commentsSection);
                } else {
                    // Réponse à un commentaire
                    addReplyToUI(commentId, parentCommentId, content, commentsSection);
                }

                // Mettre à jour le compteur de commentaires
                updateCommentCount(postId);

                // Vider le champ de saisie
                clearCommentInput(commentsSection);

                showInfo("✅ Commentaire ajouté avec succès!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ajout du commentaire: " + e.getMessage());
        }
    }

    private void addCommentToUI(int commentId, int postId, String content, VBox commentsSection) {
        VBox newComment = new VBox(5);
        newComment.getStyleClass().add("comment");
        newComment.setId("comment-" + commentId);
        newComment.setPadding(new Insets(10));
        newComment.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 0 3; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: white;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label author = new Label(currentUserName);
        author.getStyleClass().add("comment-author");
        author.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label time = new Label("Just now");
        time.getStyleClass().add("comment-time");
        time.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        header.getChildren().addAll(author, time);

        Label message = new Label(content);
        message.getStyleClass().add("comment-text");
        message.setWrapText(true);
        message.setStyle("-fx-padding: 5 0; -fx-font-size: 13px;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button replyBtn = new Button("↩️ Reply");
        replyBtn.getStyleClass().add("reply-btn");
        replyBtn.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5 15; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-size: 11px;");
        replyBtn.setOnAction(e -> showReplyInput(newComment, postId, commentId, commentsSection));

        Button editBtn = new Button("✏️ Edit");
        editBtn.getStyleClass().add("edit-btn");
        editBtn.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5 15; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-size: 11px;");
        editBtn.setOnAction(e -> editComment(commentId, message, newComment));

        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.getStyleClass().add("delete-btn");
        deleteBtn.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5 15; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-size: 11px;");
        deleteBtn.setOnAction(e -> deleteComment(commentId, newComment));

        actions.getChildren().addAll(replyBtn, editBtn, deleteBtn);

        newComment.getChildren().addAll(header, message, actions);

        // Insérer avant le formulaire
        commentsSection.getChildren().add(commentsSection.getChildren().size() - 1, newComment);
    }

    private void addReplyToUI(int replyId, int parentCommentId, String content, VBox commentsSection) {
        // Trouver le commentaire parent
        for (Node node : commentsSection.getChildren()) {
            if (node instanceof VBox && node.getId() != null && node.getId().equals("comment-" + parentCommentId)) {
                VBox parentComment = (VBox) node;

                VBox replyBox = new VBox(3);
                replyBox.setId("reply-" + replyId);
                replyBox.setPadding(new Insets(5, 0, 0, 20));
                replyBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 8; -fx-border-radius: 5;");

                HBox header = new HBox(10);
                Label author = new Label("↳ " + currentUserName);
                author.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                Label time = new Label("Just now");
                time.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");
                header.getChildren().addAll(author, time);

                Label message = new Label(content);
                message.setWrapText(true);
                message.setStyle("-fx-font-size: 12px; -fx-padding: 5 0;");

                HBox actions = new HBox(10);
                Button editBtn = new Button("Edit");
                editBtn.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 3 10; -fx-border-radius: 15; -fx-background-radius: 15; -fx-font-size: 10px;");
                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 3 10; -fx-border-radius: 15; -fx-background-radius: 15; -fx-font-size: 10px;");
                editBtn.setOnAction(e -> editComment(replyId, message, replyBox));
                deleteBtn.setOnAction(e -> deleteComment(replyId, replyBox));
                actions.getChildren().addAll(editBtn, deleteBtn);

                replyBox.getChildren().addAll(header, message, actions);

                // Chercher ou créer le conteneur de réponses
                VBox repliesContainer = null;
                for (Node child : parentComment.getChildren()) {
                    if (child instanceof VBox && child.getStyleClass().contains("replies")) {
                        repliesContainer = (VBox) child;
                        break;
                    }
                }

                if (repliesContainer == null) {
                    repliesContainer = new VBox(5);
                    repliesContainer.getStyleClass().add("replies");
                    repliesContainer.setPadding(new Insets(5, 0, 0, 0));
                    parentComment.getChildren().add(repliesContainer);
                }

                repliesContainer.getChildren().add(replyBox);
                break;
            }
        }
    }

    private void clearCommentInput(VBox commentsSection) {
        for (Node child : commentsSection.getChildren()) {
            if (child instanceof HBox && child.getStyleClass().contains("comment-form")) {
                HBox form = (HBox) child;
                for (Node field : form.getChildren()) {
                    if (field instanceof TextField) {
                        ((TextField) field).clear();
                        break;
                    }
                }
                break;
            }
        }
    }

    private void editComment(int commentId, Label messageLabel, VBox commentBox) {
        TextInputDialog dialog = new TextInputDialog(messageLabel.getText());
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText("Modifiez votre commentaire");
        dialog.setContentText("Nouveau texte:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newText -> {
            if (!newText.trim().isEmpty() && !newText.equals(messageLabel.getText())) {
                String sql = "UPDATE comments SET content = ? WHERE id = ?";
                try (Connection conn = getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, newText.trim());
                    stmt.setInt(2, commentId);
                    stmt.executeUpdate();

                    messageLabel.setText(newText.trim());
                    showInfo("✅ Commentaire modifié avec succès!");

                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Erreur lors de la modification");
                }
            }
        });
    }

    private void deleteComment(int commentId, VBox commentBox) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer le commentaire");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce commentaire ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE comments SET is_active = false WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, commentId);
                stmt.executeUpdate();

                VBox parent = (VBox) commentBox.getParent();
                parent.getChildren().remove(commentBox);

                // Récupérer l'ID du post
                Node postCard = commentBox;
                while (postCard != null && !postCard.getStyleClass().contains("post-card")) {
                    postCard = postCard.getParent();
                }
                if (postCard != null) {
                    String postId = postCard.getId().replace("post-", "");
                    updateCommentCount(Integer.parseInt(postId));
                }

                showInfo("✅ Commentaire supprimé avec succès!");

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur lors de la suppression");
            }
        }
    }

    private void updateCommentCount(int postId) {
        String sql = "SELECT COUNT(*) as count FROM comments WHERE post_id = ? AND is_active = true";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");

                for (Node postCard : postCards) {
                    if (postCard.getId() != null && postCard.getId().equals("post-" + postId)) {
                        // Trouver le bouton commentaire
                        for (Node child : ((VBox) postCard).getChildren()) {
                            if (child instanceof HBox) {
                                for (Node btn : ((HBox) child).getChildren()) {
                                    if (btn instanceof Button && ((Button) btn).getText().contains("💬")) {
                                        ((Button) btn).setText("💬 " + count);
                                        return;
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void toggleComments(VBox postCard) {
        for (Node child : postCard.getChildren()) {
            if (child instanceof VBox && child.getStyleClass().contains("comments-section")) {
                boolean visible = !child.isVisible();
                child.setVisible(visible);
                child.setManaged(visible);
                break;
            }
        }
    }

    private void handleVote(int postId, Button voteBtn) {
        String checkSql = "SELECT id, vote_type FROM votes WHERE post_id = ? AND user_id = ?";
        String insertSql = "INSERT INTO votes (post_id, user_id, vote_type) VALUES (?, ?, 'up')";
        String deleteSql = "DELETE FROM votes WHERE post_id = ? AND user_id = ?";

        try (Connection conn = getConnection()) {
            // Vérifier si l'utilisateur a déjà voté
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, postId);
            checkStmt.setInt(2, currentUserId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Déjà voté, on annule
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setInt(1, postId);
                deleteStmt.setInt(2, currentUserId);
                deleteStmt.executeUpdate();
                showInfo("👍 Vote retiré!");
            } else {
                // Nouveau vote
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, postId);
                insertStmt.setInt(2, currentUserId);
                insertStmt.executeUpdate();
                showInfo("✅ Vote ajouté!");
            }

            // Mettre à jour le compteur de votes
            String updateSql = "UPDATE posts SET vote_count = (SELECT COUNT(*) FROM votes WHERE post_id = ?) WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, postId);
            updateStmt.setInt(2, postId);
            updateStmt.executeUpdate();

            // Récupérer le nouveau compte
            String countSql = "SELECT vote_count FROM posts WHERE id = ?";
            PreparedStatement countStmt = conn.prepareStatement(countSql);
            countStmt.setInt(1, postId);
            ResultSet countRs = countStmt.executeQuery();
            if (countRs.next()) {
                int newCount = countRs.getInt("vote_count");
                voteBtn.setText("👍 " + newCount);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du vote");
        }
    }

    private void showEditPostDialog(int postId, String currentTitle, String currentDescription) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier le post");
        dialog.setHeaderText("Modifiez votre post");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField titleField = new TextField(currentTitle);
        titleField.setPromptText("Titre");
        titleField.setStyle("-fx-padding: 10; -fx-background-radius: 5;");

        TextArea descArea = new TextArea(currentDescription);
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(5);
        descArea.setStyle("-fx-padding: 10; -fx-background-radius: 5;");

        content.getChildren().addAll(new Label("Titre:"), titleField, new Label("Description:"), descArea);

        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            String newTitle = titleField.getText().trim();
            String newDesc = descArea.getText().trim();

            if (!newTitle.isEmpty() && !newDesc.isEmpty()) {
                updatePostInDatabase(postId, newTitle, newDesc);
            }
        }
    }

    private void updatePostInDatabase(int postId, String newTitle, String newDesc) {
        String sql = "UPDATE posts SET title = ?, description = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newTitle);
            stmt.setString(2, newDesc);
            stmt.setInt(3, postId);
            stmt.executeUpdate();

            // Mettre à jour l'affichage
            for (Node postCard : postCards) {
                if (postCard.getId() != null && postCard.getId().equals("post-" + postId)) {
                    VBox card = (VBox) postCard;
                    for (Node child : card.getChildren()) {
                        if (child instanceof Label) {
                            Label label = (Label) child;
                            if (label.getStyleClass().contains("post-title")) {
                                label.setText(newTitle);
                            } else if (label.getStyleClass().contains("post-text")) {
                                label.setText(newDesc);
                            }
                        }
                    }
                    break;
                }
            }

            showInfo("✅ Post modifié avec succès!");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la modification");
        }
    }

    private void handleDeletePost(int postId, VBox postCard) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer le post");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce post ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE posts SET is_active = false WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, postId);
                stmt.executeUpdate();

                feedContainer.getChildren().remove(postCard);
                postCards.remove(postCard);

                showInfo("✅ Post supprimé avec succès!");

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur lors de la suppression");
            }
        }
    }

    private void wireSearch() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        if (searchButton != null) {
            searchButton.setOnAction(event -> applyFilter());
        }
    }

    private void wirePostForm() {
        if (clearPostButton != null) {
            clearPostButton.setOnAction(event -> clearPostForm());
        }
        if (submitPostButton != null) {
            submitPostButton.setOnAction(event -> createPost());
        }
        if (chooseImageButton != null) {
            chooseImageButton.setOnAction(e -> chooseImage(postImageField));
        }
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

    private void createPost() {
        String title = normalizeInput(postTitleField.getText());
        String description = normalizeInput(postDescriptionArea.getText());
        String imagePath = postImageField.getText();

        if (title.isEmpty() && description.isEmpty()) {
            showError("Veuillez entrer un titre ou une description");
            return;
        }

        if (title.isEmpty()) {
            title = "Untitled post";
        }
        if (description.isEmpty()) {
            description = "No description provided.";
        }

        String sql = "INSERT INTO posts (user_id, title, description, image_url, tag) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, currentUserId);
            stmt.setString(2, title);
            stmt.setString(3, description);

            if (imagePath != null && !imagePath.isEmpty()) {
                stmt.setString(4, imagePath);
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            stmt.setString(5, "General");
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int postId = generatedKeys.getInt(1);

                // Recharger tous les posts pour voir le nouveau
                loadPostsFromDatabase();
                clearPostForm();
                showInfo("✅ Post créé avec succès!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la création du post: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("Driver MySQL non trouvé", e);
        }
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(dateTime, now).getSeconds();

        if (seconds < 60) return "Just now";
        if (seconds < 3600) return (seconds / 60) + " minutes ago";
        if (seconds < 86400) return (seconds / 3600) + " hours ago";
        if (seconds < 604800) return (seconds / 86400) + " days ago";

        return dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private boolean hasStyleClass(Node node, String styleClass) {
        return node != null && node.getStyleClass().contains(styleClass);
    }

    private String collectText(Node node) {
        if (node instanceof Labeled) {
            return ((Labeled) node).getText();
        }
        if (node instanceof TextInputControl) {
            return ((TextInputControl) node).getText();
        }
        if (node instanceof Text) {
            return ((Text) node).getText();
        }
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            StringBuilder builder = new StringBuilder();
            for (Node child : parent.getChildrenUnmodifiable()) {
                String childText = collectText(child);
                if (!childText.isBlank()) {
                    builder.append(' ').append(childText);
                }
            }
            return builder.toString();
        }
        return "";
    }

    private String normalize(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase();
    }

    private String normalizeInput(String value) {
        if (value == null) return "";
        return value.trim();
    }
}