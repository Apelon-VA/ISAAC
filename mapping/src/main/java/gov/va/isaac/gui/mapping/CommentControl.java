package gov.va.isaac.gui.mapping;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class CommentControl extends AnchorPane {
    @FXML private Label timestampLabel;
    @FXML private Label authorLabel;
    @FXML private TextArea commentTextArea;

    public CommentControl() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CommentControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

    }
    
    public void setComment(String comment, String author, String timestamp) {
    	commentTextArea.textProperty().set(comment);
    	authorLabel.textProperty().set(author);
    	timestampLabel.textProperty().set(timestamp);
    }
}
