package gov.va.isaac.gui.mapping;

import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.isaac.util.ValidBooleanBinding;

import java.io.IOException;
import java.net.URL;

import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for the Comment Dialog View.
 *
 * @author dtriglianos
 * 
 */

public class CommentDialogController implements TaskCompleteCallback {
	private static final Logger LOG = LoggerFactory.getLogger(MappingController.class);

    @FXML    private AnchorPane titlePane;
    @FXML    private Label 		mappingItemLabel;
    @FXML    private Button 	closeButton;
    @FXML    private TextArea 	newCommentTextArea;
    @FXML    private ScrollPane commentsPane;
    @FXML    private Label 		mappingSetLabel;
    @FXML    private AnchorPane mainPane;
    @FXML    private AnchorPane newCommentPane;
    @FXML    private Button 	saveButton;
    @FXML    private Label		titleLabel;
	
	public Region getRootNode() {
		//return region;
		return mainPane;
	}
	
	public StringProperty getTitle() {
		return titleLabel.textProperty();
	}
	
	@FXML
	public void initialize() {
		VBox commentContent = new VBox();
		commentsPane.setContent(commentContent);

		// Create example comment
		CommentControl cc = new CommentControl();
		AnchorPane.setLeftAnchor(cc, 0.0);
		AnchorPane.setRightAnchor(cc, 10.0);
		
		cc.setPrefWidth(commentContent.getPrefWidth());
		cc.setComment("This is a comment on the mapping item", "John Smith", "3/1/2015 1:23pm");

		commentContent.setPrefHeight(commentContent.getPrefHeight() + cc.getPrefHeight());
		commentContent.getChildren().add(cc);
	}	
	
	
	@Override
    public void taskComplete(long taskStartTime, Integer taskId) {
	    // TODO Auto-generated method stub
	    
    }
}
