package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.mapping.data.MappingItemComment;
import gov.va.isaac.gui.mapping.data.MappingItemCommentDAO;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.gui.util.Images;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.util.OTFUtility;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;

public class CommentControl extends AnchorPane {
    @FXML private Label timestampLabel;
    @FXML private Label authorLabel;
    @FXML private TextArea commentTextArea;
    @FXML private Button retireButton;

    @FXML
    public void initialize() {
        assert timestampLabel != null : "fx:id=\"timestampLabel\" was not injected: check your FXML file 'CommentControl.fxml'.";
        assert authorLabel != null : "fx:id=\"authorLabel\" was not injected: check your FXML file 'CommentControl.fxml'.";
        assert commentTextArea != null : "fx:id=\"commentTextArea\" was not injected: check your FXML file 'CommentControl.fxml'.";
        assert retireButton != null : "fx:id=\"retireButton\" was not injected: check your FXML file 'CommentControl.fxml'.";

		FxUtils.assignImageToButton(retireButton, Images.MINUS.createImageView(), "Retire Comment");
        
        retireButton.setOnAction((event) -> {
        	try {
				DialogResponse response = AppContext.getCommonDialogs().showYesNoDialog("Please Confirm", "Are you sure you want to retire this comment?");
				if (response == DialogResponse.YES) {
	            	MappingItemCommentDAO.retireComment(comment_.getPrimordialUUID());
	            	if (dialogController != null) {
		            	dialogController.refreshComments();
	            	}
				}
				//this.getScene().getWindow().requestFocus();

        	} catch (IOException e) {
        		throw new RuntimeException(e);
        	}
        });
        
    }

    private MappingItemComment comment_;
    private CommentDialogController dialogController;
    
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
    
    public void setDialogController(CommentDialogController controller) {
    	dialogController = controller;
    }
    
    public void setComment(MappingItemComment comment) {
    	comment_ = comment;
    	commentTextArea.textProperty().set(comment.getCommentText());
    	authorLabel.textProperty().set(OTFUtility.getDescription(comment.getAuthorName()));
    	timestampLabel.textProperty().set(dateTimeFormatShort.format(comment.getCreationDate()));
    }
}
