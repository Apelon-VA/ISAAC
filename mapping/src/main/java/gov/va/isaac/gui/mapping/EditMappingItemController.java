import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class EditMappingItemController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private GridPane infoGridPane;
    @FXML private Button cancelButton;
    @FXML private Label sourceLabel;
    @FXML private Label qualifierLabel;
    @FXML private Label targetLabel;
    @FXML private AnchorPane mainPane;
    @FXML private Button saveButton;

    @FXML
    void initialize() {
        assert infoGridPane != null : "fx:id=\"infoGridPane\" was not injected: check your FXML file 'EditMappingItem.fxml'.";
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'EditMappingItem.fxml'.";
        assert sourceLabel != null : "fx:id=\"sourceLabel\" was not injected: check your FXML file 'EditMappingItem.fxml'.";
        assert qualifierLabel != null : "fx:id=\"qualifierLabel\" was not injected: check your FXML file 'EditMappingItem.fxml'.";
        assert targetLabel != null : "fx:id=\"targetLabel\" was not injected: check your FXML file 'EditMappingItem.fxml'.";
        assert mainPane != null : "fx:id=\"mainPane\" was not injected: check your FXML file 'EditMappingItem.fxml'.";
        assert saveButton != null : "fx:id=\"saveButton\" was not injected: check your FXML file 'EditMappingItem.fxml'.";

    }
}
