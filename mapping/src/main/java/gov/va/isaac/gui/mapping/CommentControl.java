package gov.va.isaac.gui.mapping;

import gov.va.isaac.gui.mapping.data.MappingItemComment;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class CommentControl extends AnchorPane {
    @FXML private Label timestampLabel;
    @FXML private Label authorLabel;
    @FXML private TextArea commentTextArea;

    private static SimpleDateFormat dateTimeFormatShort = new SimpleDateFormat("MM/dd/yy HH:mm");
    
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
    
    public void setComment(MappingItemComment comment) {
    	commentTextArea.textProperty().set(comment.getCommentText());
    	authorLabel.textProperty().set(comment.getAuthorName());
    	timestampLabel.textProperty().set(dateTimeFormatShort.format(comment.getCreatedDate()));
    }
}
