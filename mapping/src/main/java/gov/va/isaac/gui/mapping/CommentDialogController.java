package gov.va.isaac.gui.mapping;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.mapping.data.MappingItem;
import gov.va.isaac.gui.mapping.data.MappingItemComment;
import gov.va.isaac.gui.mapping.data.MappingItemCommentDAO;
import gov.va.isaac.gui.mapping.data.MappingSet;
import gov.va.isaac.gui.util.ErrorMarkerUtils;
import gov.va.isaac.gui.util.WrappedLabeled;
import gov.va.isaac.util.TaskCompleteCallback;
import java.io.IOException;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
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

	@FXML    private Button 	closeButton;
    @FXML    private TextArea 	newCommentTextArea;
    @FXML    private ScrollPane commentsPane;
    @FXML    private Label 		mappingSetLabel;
    @FXML    private VBox 		mainPane;
    @FXML    private AnchorPane newCommentPane;
    @FXML    private Button 	saveButton;
    @FXML    private VBox 		commentListVBox;
    @FXML    private Label 		sourceLabel;
    @FXML    private Label 		qualifierLabel;
    @FXML    private Label 		targetLabel;
    @FXML    private GridPane 	titleGridPane;

    private List<MappingItem> mappingItems_;
    
	public Region getRootNode() {
		return mainPane;
	}
	
	public StringProperty getTitle() {
		return new SimpleStringProperty((mappingItems_.size() > 1)? "Add Bulk Comment" : "Mapping Comments");
	}
	
	@FXML
	public void initialize() {
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
						for (MappingItem mappingItem : mappingItems_) {
							newComment = MappingItemCommentDAO.createMappingItemComment(mappingItem.getPrimordialUUID(), commentText, null);
							mappingItem.refreshCommentsProperty();
						}
					} catch (Exception ex) {
						LOG.error(ex.toString());
						ex.printStackTrace();
						String message = (ex.getMessage() == null)? "Unspecified error, possibly use of null" : ex.getMessage();
						AppContext.getCommonDialogs().showInformationDialog("Cannot Create Comment", message);
					}
				}
				if (newComment != null) {
					if (mappingItems_.size() > 1) {
						close();
					} else {
						//TODO fix the index issue - need to wait for the index to complete.  This gives us a better chance at refreshing properly...
						Platform.runLater(() -> refreshComments());
						newCommentTextArea.setText("");
					}
				}
			}
		});

		closeButton.setCancelButton(true);
		closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				close();
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
	
	private void close() {
		mappingItems_ = null;
		closeButton.getScene().getWindow().hide();
	}
	
	public void setMappingSetAndItems(MappingSet mappingSet, ObservableList<MappingItem> mappingItems) {
		mappingItems_ = mappingItems;
		if (mappingItems.size() > 1) {
			double height = newCommentPane.getHeight();
			titleGridPane.setVisible(false);
			titleGridPane.setManaged(false);
			commentsPane.setVisible(false);
			commentsPane.setManaged(false);
			mainPane.setPrefHeight(height);
			
		} else if (mappingItems.size() > 0) {
			
			mappingSetLabel.setText(mappingSet.getName());
			sourceLabel.setText(mappingItems.get(0).getSourceConceptProperty().getValueSafe());
			targetLabel.setText(mappingItems.get(0).getTargetConceptProperty().getValueSafe());
			qualifierLabel.setText(mappingItems.get(0).getQualifierConceptProperty().getValueSafe());
			refreshComments();
		}
	}
	
	private void addCommentToList(MappingItemComment comment) {
		CommentControl cc = new CommentControl();
		cc.set(this, mappingItems_.get(0), comment);
		commentListVBox.getChildren().add(cc);
	}
	
	private void clearComments() {
		commentListVBox.getChildren().clear();
	}
	
	public void refreshComments() {
		try
		{
			clearComments();
			List<MappingItemComment> comments = mappingItems_.get(0).getComments();
			for (MappingItemComment comment : comments) {
				if (comment.isActive()) {
					addCommentToList(comment);
				}
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
