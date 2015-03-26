package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.mapping.data.MappingItem;
import gov.va.isaac.gui.mapping.data.MappingItemComment;
import gov.va.isaac.gui.mapping.data.MappingItemCommentDAO;
import gov.va.isaac.gui.mapping.data.MappingSet;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.isaac.util.ValidBooleanBinding;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
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
    @FXML    private Button 	closeButton;
    @FXML    private TextArea 	newCommentTextArea;
    @FXML    private ScrollPane commentsPane;
    @FXML    private Label 		mappingSetLabel;
    @FXML    private AnchorPane mainPane;
    @FXML    private AnchorPane newCommentPane;
    @FXML    private Button 	saveButton;
    @FXML    private Label		titleLabel;
    @FXML    private VBox 		commentListVBox;
    @FXML    private Label 		sourceLabel;
    @FXML    private Label 		qualifierLabel;
    @FXML    private Label 		targetLabel;
    @FXML    private GridPane 	titleGridPane;

    private MappingItem mappingItem_;
    
	public Region getRootNode() {
		return mainPane;
	}
	
	public StringProperty getTitle() {
		return titleLabel.textProperty();
	}
	
	@FXML
	public void initialize() {
		assert titleLabel 			!= null : "fx:id=\"titleLabel\" was not injected: check your FXML file 'CommentDialog.fxml'.";
		assert sourceLabel 			!= null : "fx:id=\"sourceLabel\" was not injected: check your FXML file 'CommentDialog.fxml'.";
		assert qualifierLabel 		!= null : "fx:id=\"qualifierLabel\" was not injected: check your FXML file 'CommentDialog.fxml'.";
		assert closeButton 			!= null : "fx:id=\"closeButton\" was not injected: check your FXML file 'CommentDialog.fxml'.";
		assert targetLabel 			!= null : "fx:id=\"targetLabel\" was not injected: check your FXML file 'CommentDialog.fxml'.";
		assert newCommentTextArea 	!= null : "fx:id=\"newCommentTextArea\" was not injected: check your FXML file 'CommentDialog.fxml'.";
		assert commentsPane 		!= null : "fx:id=\"commentsPane\" was not injected: check your FXML file 'CommentDialog.fxml'.";
		assert mappingSetLabel 		!= null : "fx:id=\"mappingSetLabel\" was not injected: check your FXML file 'CommentDialog.fxml'.";
		assert mainPane 			!= null : "fx:id=\"mainPane\" was not injected: check your FXML file 'CommentDialog.fxml'.";
		assert newCommentPane 		!= null : "fx:id=\"newCommentPane\" was not injected: check your FXML file 'CommentDialog.fxml'.";
		assert saveButton 			!= null : "fx:id=\"saveButton\" was not injected: check your FXML file 'CommentDialog.fxml'.";
		assert commentListVBox 		!= null : "fx:id=\"commentListVBox\" was not injected: check your FXML file 'CommentDialog.fxml'.";
        assert titleGridPane 		!= null : "fx:id=\"titleGridPane\" was not injected: check your FXML file 'CommentDialog.fxml'.";
		
		commentListVBox.setPrefHeight(0);
		commentsPane.setFitToWidth(true);
		commentListVBox.setFillWidth(true);
		
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				String commentText = newCommentTextArea.getText().trim();
				MappingItemComment newComment = null;
				if (commentText != null && !commentText.equals("")) {
					try {
						//TODO use context?
						newComment = MappingItemCommentDAO.createMappingItemComment(mappingItem_.getPrimordialUUID(), commentText, null);
					} catch (Exception ex) {
						LOG.error(ex.toString());
						ex.printStackTrace();
						String message = (ex.getMessage() == null)? "Unspecified error, possibly use of null" : ex.getMessage();
						AppContext.getCommonDialogs().showInformationDialog("Cannot Create Comment", message);
					}
				}
				if (newComment != null) {
					addCommentToList(newComment);
					newCommentTextArea.setText("");
				}
			}
		});

		closeButton.setCancelButton(true);
		closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) { 
				mappingItem_ = null;
				closeButton.getScene().getWindow().hide();
			}
		});
		closeButton.setOnKeyPressed(new EventHandler<KeyEvent>()  {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					event.consume();
					closeButton.fire();
				}
			}
		});
	}	
	
	
	@Override
    public void taskComplete(long taskStartTime, Integer taskId) {
	    // TODO Auto-generated method stub
	    
    }
	
	public void setMappingSetAndItem(MappingSet mappingSet, MappingItem mappingItem) {
		mappingItem_ = mappingItem;
		mappingSetLabel.setText(mappingSet.getName());
		sourceLabel.setText(mappingItem.getSourceConceptProperty().getValueSafe());
		targetLabel.setText(mappingItem.getTargetConceptProperty().getValueSafe());
		qualifierLabel.setText(mappingItem.getQualifierConceptProperty().getValueSafe());
		try
		{
			List<MappingItemComment> comments = mappingItem.getComments();
			for (MappingItemComment comment : comments) {
				addCommentToList(comment);
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addCommentToList(MappingItemComment comment) {
		CommentControl cc = new CommentControl();
		cc.setPrefWidth(commentListVBox.getPrefWidth());
		cc.setComment(comment);
		commentListVBox.setPrefHeight(commentListVBox.getPrefHeight() + cc.getPrefHeight());
		commentListVBox.getChildren().add(cc);
	}
}
