package com.recruitx.hrone.controller;

import com.recruitx.hrone.dao.FormationDAO;
import com.recruitx.hrone.models.Formation;
import com.recruitx.hrone.utils.COrdre;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FormationsDashboardController {

    // DAO
    private FormationDAO formationDAO;
    // Form fields
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextField imageUrlField;
    @FXML private Button browseImageButton;
    @FXML private TextField courseInputField;
    @FXML private Button addCourseButton;
    @FXML private VBox coursesListContainer;
    @FXML private Label formStatusLabel;
    @FXML private Button resetButton;
    @FXML private Button publishButton;

    // Labels for required fields
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label imageUrlLabel;

    // Preview elements
    @FXML private ImageView previewImageView;
    @FXML private Label previewTitleLabel;
    @FXML private Label previewDescriptionLabel;
    @FXML private Label previewCoursesCountLabel;
    @FXML private VBox previewCoursesListContainer;

    // Published formations list
    @FXML private VBox formationList;

    // Data storage (in-memory for courses)
    private List<String> currentCourses = new ArrayList<>();
    private Formation editingFormation = null;
    private int currentEntrepriseId = 1; // TODO: Get from logged-in user session

    @FXML
    public void initialize() {
        // Initialize DAO
        formationDAO = new FormationDAO();

        setupEventHandlers();
        setupRequiredFieldLabels();
        initializeDefaultValues();
        loadPublishedFormations();
    }

    private void setupRequiredFieldLabels() {
        // Add red asterisk to required field labels
        if (titleLabel != null) {
            addRequiredIndicator(titleLabel);
        }
        if (descriptionLabel != null) {
            addRequiredIndicator(descriptionLabel);
        }
        if (imageUrlLabel != null) {
            addRequiredIndicator(imageUrlLabel);
        }
    }

    private void addRequiredIndicator(Label label) {
        String originalText = label.getText();
        // Remove existing asterisk if present
        originalText = originalText.replaceAll("\\s*\\*$", "");

        Text normalText = new Text(originalText + " ");
        Text asterisk = new Text("*");
        asterisk.setFill(Color.RED);
        asterisk.setStyle("-fx-font-weight: bold;");

        TextFlow textFlow = new TextFlow(normalText, asterisk);

        // Replace label with TextFlow (if you want to keep using Label, see alternative below)
        label.setText(originalText + " *");
        label.setStyle("-fx-text-fill: black;");

        // Alternative: Use graphic property
        label.setGraphic(textFlow);
        label.setText("");
    }

    private void setupEventHandlers() {
        addCourseButton.setOnAction(e -> addCourse());
        courseInputField.setOnAction(e -> addCourse());
        resetButton.setOnAction(e -> resetForm());
        publishButton.setOnAction(e -> publishFormation());

        // Browse button for local image selection
        if (browseImageButton != null) {
            browseImageButton.setOnAction(e -> browseForImage());
        }

        // Live preview updates
        titleField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        imageUrlField.textProperty().addListener((obs, oldVal, newVal) -> updatePreviewImage());
    }

    private void initializeDefaultValues() {
        previewTitleLabel.setText("Titre de formation");
        previewDescriptionLabel.setText("La description apparaitra ici pour presenter la formation aux candidats.");
        previewCoursesCountLabel.setText("0 module");

        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/com/recruitx/hrone/formation/logo.png"));
            previewImageView.setImage(defaultImage);
        } catch (Exception e) {
            System.out.println("Could not load default image");
        }
    }

    private void addCourse() {
        String courseTitle = courseInputField.getText().trim();

        if (courseTitle.isEmpty()) {
            showStatus("Veuillez entrer un titre de cours.", true);
            return;
        }

        currentCourses.add(courseTitle);
        HBox courseItem = createCourseItem(courseTitle, currentCourses.size() - 1);
        coursesListContainer.getChildren().add(courseItem);
        updatePreviewCourses();

        courseInputField.clear();
        courseInputField.requestFocus();
        showStatus("Module ajouté avec succès.", false);
    }

    private HBox createCourseItem(String title, int index) {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);
        container.setSpacing(8);
        container.getStyleClass().add("resource-item");

        Label titleLabel = new Label((index + 1) + ". " + title);
        titleLabel.getStyleClass().add("resource-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("btn", "btn-danger", "btn-small");
        deleteButton.setOnAction(e -> removeCourse(index));

        container.getChildren().addAll(titleLabel, deleteButton);
        return container;
    }

    private void removeCourse(int index) {
        if (index >= 0 && index < currentCourses.size()) {
            currentCourses.remove(index);
            refreshCoursesDisplay();
            updatePreviewCourses();
            showStatus("Module supprimé.", false);
        }
    }

    private void refreshCoursesDisplay() {
        coursesListContainer.getChildren().clear();
        for (int i = 0; i < currentCourses.size(); i++) {
            HBox courseItem = createCourseItem(currentCourses.get(i), i);
            coursesListContainer.getChildren().add(courseItem);
        }
    }

    private void updatePreview() {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();

        previewTitleLabel.setText(title.isEmpty() ? "Titre de formation" : title);
        previewDescriptionLabel.setText(description.isEmpty()
                ? "La description apparaitra ici pour presenter la formation aux candidats."
                : description);
    }

    private void updatePreviewImage() {
        String imageUrl = imageUrlField.getText().trim();

        if (!imageUrl.isEmpty()) {
            try {
                Image image = new Image(imageUrl, true);
                image.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        showStatus("Impossible de charger l'image. Vérifiez l'URL.", true);
                    }
                });
                previewImageView.setImage(image);
            } catch (Exception e) {
                showStatus("URL d'image invalide.", true);
            }
        }
    }

    private void updatePreviewCourses() {
        int count = currentCourses.size();
        previewCoursesCountLabel.setText(count + " module" + (count > 1 ? "s" : ""));

        previewCoursesListContainer.getChildren().clear();

        if (currentCourses.isEmpty()) {
            Label emptyLabel = new Label("Ajoutez vos cours pour les voir dans l'apercu.");
            emptyLabel.getStyleClass().add("muted");
            previewCoursesListContainer.getChildren().add(emptyLabel);
        } else {
            for (String course : currentCourses) {
                Label courseLabel = new Label("✓ " + course);
                courseLabel.getStyleClass().add("resource-meta");
                previewCoursesListContainer.getChildren().add(courseLabel);
            }
        }
    }

    private void publishFormation() {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String imageUrl = imageUrlField.getText().trim();

        // Validate each field individually with specific messages
        if (title.isEmpty()) {
            showStatus("Le titre de la formation est obligatoire.", true);
            return;
        }

        if (title.length() < 10) {
            showStatus("Le titre doit contenir au moins 10 caractères.", true);
            return;
        }

        if (description.isEmpty()) {
            showStatus("La description de la formation est obligatoire.", true);
            return;
        }

        if (description.length() < 20) {
            showStatus("La description doit contenir au moins 20 caractères.", true);
            return;
        }

        if (imageUrl.isEmpty()) {
            showStatus("L'URL de l'image est obligatoire.", true);
            return;
        }

        // Accept both local files (file:/) and URLs (http/https)
        if (!isLocalFile(imageUrl) && !isValidUrl(imageUrl)) {
            showStatus("L'image n'est pas valide. Utilisez une URL (http/https) ou sélectionnez un fichier local.", true);
            return;
        }

        if (currentCourses.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Aucun module");
            alert.setHeaderText("Vous n'avez ajouté aucun module à cette formation.");
            alert.setContentText("Voulez-vous continuer quand même ?");

            if (alert.showAndWait().get() != ButtonType.OK) {
                return;
            }
        }

        try {
            // Build full description with courses
            String fullDescription = buildDescriptionWithCourses(description);

            if (editingFormation != null) {
                // UPDATE MODE
                editingFormation.setTitre(title);
                editingFormation.setDescription(fullDescription);
                editingFormation.setImage(imageUrl);

                boolean success = formationDAO.update(editingFormation);

                if (success) {
                    showStatus("Formation mise à jour avec succès !", false);
                    formStatusLabel.getStyleClass().add("status-success");
                } else {
                    showStatus("Erreur lors de la mise à jour.", true);
                    return;
                }

                editingFormation = null;
            } else {
                // CREATE MODE
                //int orderNumber = formationDAO.getNextOrderNumber();
                long orderNumber = COrdre.GetNumOrdreNow();

                Formation formation = new Formation(
                        title,
                        fullDescription,
                        (int) orderNumber,
                        currentEntrepriseId,
                        imageUrl
                );
                System.out.println("orderNumber  ======"+orderNumber);

                boolean success = formationDAO.create(formation);
                System.out.println("orderNumber  ======"+orderNumber);

                if (success) {
                    showStatus("Formation publiée avec succès !", false);
                    formStatusLabel.getStyleClass().add("status-success");
                } else {
                    showStatus("Erreur lors de la publication.", true);
                    return;
                }
            }

            loadPublishedFormations();
            resetForm();

        } catch (Exception e) {
            showStatus("Erreur: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private String buildDescriptionWithCourses(String baseDescription) {
        if (currentCourses.isEmpty()) {
            return baseDescription;
        }

        StringBuilder sb = new StringBuilder(baseDescription);
        sb.append("\n\n[MODULES]\n");
        for (int i = 0; i < currentCourses.size(); i++) {
            sb.append((i + 1)).append(". ").append(currentCourses.get(i)).append("\n");
        }

        return sb.toString();
    }

    private void parseCoursesFromDescription(String description) {
        currentCourses.clear();

        if (description == null || !description.contains("[MODULES]")) {
            return;
        }

        String[] parts = description.split("\\[MODULES\\]");
        if (parts.length > 1) {
            String coursesSection = parts[1].trim();
            String[] lines = coursesSection.split("\n");

            for (String line : lines) {
                line = line.trim();
                if (line.matches("^\\d+\\..*")) {
                    // Remove number prefix "1. ", "2. ", etc.
                    String courseTitle = line.replaceFirst("^\\d+\\.\\s*", "");
                    if (!courseTitle.isEmpty()) {
                        currentCourses.add(courseTitle);
                    }
                }
            }
        }
    }

    private String getBaseDescription(String fullDescription) {
        if (fullDescription == null || !fullDescription.contains("[MODULES]")) {
            return fullDescription;
        }

        String[] parts = fullDescription.split("\\[MODULES\\]");
        return parts[0].trim();
    }

    private void loadPublishedFormations() {
        formationList.getChildren().clear();

        List<Formation> formations = formationDAO.readByEntreprise(currentEntrepriseId);

        if (formations.isEmpty()) {
            Label emptyLabel = new Label("Aucune formation publiée pour le moment.");
            emptyLabel.getStyleClass().add("muted");
            formationList.getChildren().add(emptyLabel);
        } else {
            for (Formation formation : formations) {
                addFormationToList(formation);
            }
        }
    }

    private void addFormationToList(Formation formation) {
        VBox formationCard = new VBox();
        formationCard.setSpacing(8);
        formationCard.getStyleClass().add("resource-item");

        Label titleLabel = new Label(formation.getTitre());
        titleLabel.getStyleClass().add("resource-title");

        // Count modules from description
        int moduleCount = countModulesInDescription(formation.getDescription());

        Label metaLabel = new Label(
                moduleCount + " modules • Ordre: " + formation.getNumOrdreCreation()
        );
        metaLabel.getStyleClass().add("resource-meta");

        // Display only base description (without modules list)
        String baseDesc = getBaseDescription(formation.getDescription());
        Label descLabel = new Label(baseDesc);
        descLabel.getStyleClass().add("muted");
        descLabel.setWrapText(true);

        HBox actionsBox = new HBox();
        actionsBox.setSpacing(8);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = new Button("Modifier");
        editButton.getStyleClass().addAll("btn", "btn-ghost", "btn-small");
        editButton.setOnAction(e -> editFormation(formation));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("btn", "btn-danger", "btn-small");
        deleteButton.setOnAction(e -> deleteFormation(formation));

        actionsBox.getChildren().addAll(editButton, deleteButton);

        formationCard.getChildren().addAll(titleLabel, metaLabel, descLabel, actionsBox);
        formationList.getChildren().add(formationCard);
    }

    private int countModulesInDescription(String description) {
        if (description == null || !description.contains("[MODULES]")) {
            return 0;
        }

        String[] parts = description.split("\\[MODULES\\]");
        if (parts.length > 1) {
            String coursesSection = parts[1].trim();
            String[] lines = coursesSection.split("\n");
            int count = 0;
            for (String line : lines) {
                if (line.trim().matches("^\\d+\\..*")) {
                    count++;
                }
            }
            return count;
        }
        return 0;
    }

    private void editFormation(Formation formation) {
        editingFormation = formation;

        titleField.setText(formation.getTitre());

        // Parse courses and base description
        String baseDesc = getBaseDescription(formation.getDescription());
        descriptionField.setText(baseDesc);

        parseCoursesFromDescription(formation.getDescription());

        imageUrlField.setText(formation.getImage() != null ? formation.getImage() : "");

        refreshCoursesDisplay();
        updatePreviewCourses();

        publishButton.setText("Mettre à jour la formation");
        showStatus("Formation chargée pour modification.", false);

        titleField.requestFocus();
    }

    private void deleteFormation(Formation formation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer la formation");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer cette formation ?");
        alert.setContentText(formation.getTitre());

        if (alert.showAndWait().get() == ButtonType.OK) {
            boolean success = formationDAO.delete(formation.getIdFormation());

            if (success) {
                showStatus("Formation supprimée.", false);
                loadPublishedFormations();
            } else {
                showStatus("Erreur lors de la suppression.", true);
            }
        }
    }

    private void resetForm() {
        titleField.clear();
        descriptionField.clear();
        imageUrlField.clear();
        courseInputField.clear();
        currentCourses.clear();
        coursesListContainer.getChildren().clear();
        editingFormation = null;

        initializeDefaultValues();
        updatePreviewCourses();

        publishButton.setText("Publier la formation");

        formStatusLabel.setText("");
        formStatusLabel.getStyleClass().removeAll("status-success", "status-error");
    }

    private void showStatus(String message, boolean isError) {
        formStatusLabel.setText(message);
        formStatusLabel.getStyleClass().removeAll("status-success", "status-error");

        if (isError) {
            formStatusLabel.getStyleClass().add("status-error");
        } else {
            formStatusLabel.getStyleClass().add("status-success");
        }
    }

    // Validate URL format
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        // Check if URL starts with http:// or https://
        String lowerUrl = url.toLowerCase();
        if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://")) {
            return false;
        }

        // Basic validation: check if there's something after the protocol
        if (url.length() <= 8) { // "https://" is 8 characters
            return false;
        }

        // Optional: More comprehensive URL validation using regex
        try {
            String urlPattern = "^(https?://)([\\w\\-]+\\.)+[\\w\\-]+(/[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]*)?$";
            return url.matches(urlPattern);
        } catch (Exception e) {
            return false;
        }
    }

    // Browse for local image file
    private void browseForImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        // Set extension filters
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Images (*.png, *.jpg, *.jpeg, *.gif)",
                "*.png", "*.jpg", "*.jpeg", "*.gif", "*.PNG", "*.JPG", "*.JPEG", "*.GIF"
        );
        fileChooser.getExtensionFilters().add(imageFilter);

        // Show open file dialog
        Stage stage = (Stage) imageUrlField.getScene().getWindow();
        java.io.File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Convert file path to file:// URL format
            String filePath = selectedFile.toURI().toString();
            imageUrlField.setText(filePath);

            // Update preview with local file
            updatePreviewImage();
            showStatus("Image locale sélectionnée: " + selectedFile.getName(), false);
        }
    }

    // Check if the path is a local file or URL
    private boolean isLocalFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        return path.toLowerCase().startsWith("file:/");
    }

    // Method to set enterprise ID from login session
    public void setEntrepriseId(int entrepriseId) {
        this.currentEntrepriseId = entrepriseId;
        loadPublishedFormations();
    }

    @FXML
    void navigateToCandidatures(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/recruitx/hrone/formation/formation.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}