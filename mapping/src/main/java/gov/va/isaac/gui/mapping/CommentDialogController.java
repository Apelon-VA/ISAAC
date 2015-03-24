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
	
    private MappingItem mappingItem_;
    private VBox commentListVBox = new VBox();
    
	public Region getRootNode() {
		//return region;
		return mainPane;
	}
	
	public StringProperty getTitle() {
		return titleLabel.textProperty();
	}
	
	@FXML
	public void initialize() {
		commentsPane.setContent(commentListVBox);

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
		//TODO dan isn't sure what this was for
		mappingItemLabel.setText("dan broke it"); //mappingItem.getName());
		
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
		AnchorPane.setLeftAnchor(cc, 0.0);
		AnchorPane.setRightAnchor(cc, 10.0);
		cc.setPrefWidth(commentListVBox.getPrefWidth());
		cc.setComment(comment);
		commentListVBox.setPrefHeight(commentListVBox.getPrefHeight() + cc.getPrefHeight());
		commentListVBox.getChildren().add(cc);
	}
}
