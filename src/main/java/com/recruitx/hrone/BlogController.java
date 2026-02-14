import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class BlogController {
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

    private final List<Node> postCards = new ArrayList<>();

    @FXML
    private void initialize() {
        refreshPostCards();
        wireSearch();
        wirePostForm();
        wireExistingCommentForms();
        applyFilter();
    }

    private void refreshPostCards() {
        postCards.clear();
        for (Node child : feedContainer.getChildren()) {
            if (hasStyleClass(child, "post-card")) {
                postCards.add(child);
            }
        }
    }

    private void wireSearch() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        searchButton.setOnAction(event -> applyFilter());
    }

    private void wirePostForm() {
        clearPostButton.setOnAction(event -> clearPostForm());
        submitPostButton.setOnAction(event -> createPost());
    }

    private void wireExistingCommentForms() {
        for (Node postCard : postCards) {
            attachCommentHandlers(postCard);
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
        postTitleField.clear();
        postDescriptionArea.clear();
        postImageField.clear();
    }

    private void createPost() {
        String title = normalizeInput(postTitleField.getText());
        String description = normalizeInput(postDescriptionArea.getText());

        if (title.isEmpty() && description.isEmpty()) {
            return;
        }

        if (title.isEmpty()) {
            title = "Untitled post";
        }
        if (description.isEmpty()) {
            description = "No description provided.";
        }

        HBox postCard = buildPostCard(title, description);
        insertPostCard(postCard);
        postCards.add(0, postCard);
        attachCommentHandlers(postCard);
        clearPostForm();
        applyFilter();
    }

    private HBox buildPostCard(String title, String description) {
        HBox postCard = new HBox();
        postCard.getStyleClass().add("post-card");

        VBox vote = new VBox();
        vote.getStyleClass().add("vote");
        Button upVote = new Button("▲");
        upVote.getStyleClass().add("vote-btn");
        Label score = new Label("0");
        score.getStyleClass().add("vote-score");
        Button downVote = new Button("▼");
        downVote.getStyleClass().add("vote-btn");
        vote.getChildren().addAll(upVote, score, downVote);

        VBox postBody = new VBox();
        postBody.getStyleClass().add("post-body");

        HBox meta = new HBox();
        meta.getStyleClass().add("post-meta");
        Label tag = new Label("General");
        tag.getStyleClass().add("tag");
        Label by = new Label("Posted by You");
        Label time = new Label("Just now");
        meta.getChildren().addAll(tag, by, time);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("post-title");
        titleLabel.setWrapText(true);

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("post-text");
        descriptionLabel.setWrapText(true);

        HBox footer = new HBox();
        footer.getStyleClass().add("post-footer");
        HBox actionsLeft = new HBox();
        actionsLeft.getStyleClass().add("post-actions");
        Button edit = new Button("Edit");
        edit.getStyleClass().add("chip");
        Button delete = new Button("Delete");
        delete.getStyleClass().add("chip");
        actionsLeft.getChildren().addAll(edit, delete);

        HBox actionsRight = new HBox();
        actionsRight.getStyleClass().add("post-actions");
        Button comments = new Button("0 comments");
        comments.getStyleClass().add("chip");
        Button share = new Button("Share");
        share.getStyleClass().add("chip");
        actionsRight.getChildren().addAll(comments, share);

        footer.getChildren().addAll(actionsLeft, actionsRight);

        VBox commentsBox = new VBox();
        commentsBox.getStyleClass().add("comments");
        HBox commentForm = buildCommentForm();
        commentsBox.getChildren().add(commentForm);

        postBody.getChildren().addAll(meta, titleLabel, descriptionLabel, footer, commentsBox);
        postCard.getChildren().addAll(vote, postBody);
        return postCard;
    }

    private HBox buildCommentForm() {
        HBox form = new HBox();
        form.getStyleClass().add("comment-form");
        TextField input = new TextField();
        input.setPromptText("Write a comment...");
        HBox.setHgrow(input, javafx.scene.layout.Priority.ALWAYS);
        Button button = new Button("Comment");
        button.getStyleClass().add("btn");
        button.getStyleClass().add("btn-primary");
        form.getChildren().addAll(input, button);
        return form;
    }

    private void insertPostCard(Node postCard) {
        int insertIndex = feedContainer.getChildren().size();
        for (int i = 0; i < feedContainer.getChildren().size(); i++) {
            Node child = feedContainer.getChildren().get(i);
            if (hasStyleClass(child, "post-form-card")) {
                insertIndex = i + 1;
                break;
            }
        }
        feedContainer.getChildren().add(insertIndex, postCard);
    }

    private void attachCommentHandlers(Node postCard) {
        VBox commentsBox = findFirstByStyleClass(postCard, VBox.class, "comments");
        if (commentsBox == null) {
            return;
        }

        HBox commentForm = findFirstByStyleClass(commentsBox, HBox.class, "comment-form");
        if (commentForm == null) {
            return;
        }

        TextField input = findFirstChild(commentForm, TextField.class);
        Button button = findFirstChild(commentForm, Button.class);
        if (input == null || button == null) {
            return;
        }

        Runnable submit = () -> addComment(commentsBox, postCard, input.getText());
        button.setOnAction(event -> submit.run());
        input.setOnAction(event -> submit.run());
    }

    private void addComment(VBox commentsBox, Node postCard, String text) {
        String body = normalizeInput(text);
        if (body.isEmpty()) {
            return;
        }

        VBox comment = new VBox();
        comment.getStyleClass().add("comment");
        Label author = new Label("You");
        author.getStyleClass().add("comment-author");
        Label message = new Label(body);
        message.getStyleClass().add("comment-text");
        message.setWrapText(true);
        HBox actions = new HBox();
        actions.getStyleClass().add("comment-actions");
        Button edit = new Button("Edit");
        edit.getStyleClass().add("link-btn");
        Button delete = new Button("Delete");
        delete.getStyleClass().add("link-btn");
        actions.getChildren().addAll(edit, delete);
        comment.getChildren().addAll(author, message, actions);

        int insertIndex = commentsBox.getChildren().size();
        for (int i = 0; i < commentsBox.getChildren().size(); i++) {
            if (hasStyleClass(commentsBox.getChildren().get(i), "comment-form")) {
                insertIndex = i;
                break;
            }
        }
        commentsBox.getChildren().add(insertIndex, comment);

        TextField input = findFirstChild(findFirstByStyleClass(commentsBox, HBox.class, "comment-form"), TextField.class);
        if (input != null) {
            input.clear();
        }

        updateCommentCount(postCard, commentsBox);
    }

    private void updateCommentCount(Node postCard, VBox commentsBox) {
        int count = 0;
        for (Node child : commentsBox.getChildren()) {
            if (hasStyleClass(child, "comment")) {
                count++;
            }
        }

        HBox footer = findFirstByStyleClass(postCard, HBox.class, "post-footer");
        if (footer == null) {
            return;
        }

        Button commentsButton = findFirstButtonWithText(footer, "comments");
        if (commentsButton != null) {
            commentsButton.setText(count + " comment" + (count == 1 ? "" : "s"));
        }
    }

    private Button findFirstButtonWithText(Node root, String token) {
        if (root instanceof Button button) {
            String text = button.getText();
            if (text != null && text.toLowerCase().contains(token)) {
                return button;
            }
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Button found = findFirstButtonWithText(child, token);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private <T extends Node> T findFirstChild(Parent parent, Class<T> type) {
        if (parent == null) {
            return null;
        }
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (type.isInstance(child)) {
                return type.cast(child);
            }
        }
        return null;
    }

    private <T extends Node> T findFirstByStyleClass(Node root, Class<T> type, String styleClass) {
        if (type.isInstance(root) && hasStyleClass(root, styleClass)) {
            return type.cast(root);
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                T found = findFirstByStyleClass(child, type, styleClass);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private boolean hasStyleClass(Node node, String styleClass) {
        return node != null && node.getStyleClass().contains(styleClass);
    }

    private String collectText(Node node) {
        if (node instanceof Labeled labeled) {
            return labeled.getText();
        }
        if (node instanceof TextInputControl input) {
            return input.getText();
        }
        if (node instanceof Text text) {
            return text.getText();
        }
        if (node instanceof Parent parent) {
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
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private String normalizeInput(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
