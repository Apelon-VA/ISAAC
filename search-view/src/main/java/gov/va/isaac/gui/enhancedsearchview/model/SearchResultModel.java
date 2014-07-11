package gov.va.isaac.gui.enhancedsearchview.model;

import org.ihtsdo.otf.tcc.api.coordinate.Status;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class SearchResultModel {
	// TODO: 
	private StringProperty preferredName;
	private DoubleProperty score;
    private StringProperty uuId;
    private IntegerProperty id;
	private StringProperty fsn;
	private StringProperty matchingText;
	private ObjectProperty<Status> status;
	private StringProperty type;

	public SearchResultModel() {
		super();
	}
	public SearchResultModel(String prefName) {
		super();
		setPreferredTerm(prefName);
	}
	
	public SearchResultModel(
			String preferredName,
			double score,
			String uuId,
			int id,
			String fsn, 
			Status status, 
			String matchingText,
			String matchingTextType) {
		super();
		setPreferredTerm(preferredName);
		setScore(score);
		setUuId(uuId);
		setId(id);
		setFsn(fsn);
		setStatus(status);
		setMatchingText(matchingText);
		setMatchingTextType(matchingTextType);
	}

	public StringProperty preferredNameProperty() {
		return preferredName;
	}
	
	public DoubleProperty scoreProperty() {
		return score;
	}

	public StringProperty uuIdProperty() {
		return uuId;
	}
	
	public IntegerProperty idProperty() {
		return id;
	}

	public StringProperty fsnProperty() {
		return fsn;
	}

	public StringProperty matchingTextProperty() {
		return matchingText;
	}

	public ObjectProperty<Status> statusProperty() {
		return status;
	}

	public StringProperty matchingTextTypeProperty() {
		return type;
	}

	public String getPreferredName() {
		return preferredName.toString();
	}
	public void setPreferredTerm(String name) {
		this.preferredName = new SimpleStringProperty(name);
	}
	
	public double getScore() {
		return score.doubleValue();
	}
	public void setScore(double score) {
		this.score = new SimpleDoubleProperty(score);
	}

	public String getUuId() {
		return uuId.toString();
	}
	public void setUuId(String uuId) {
		this.uuId = new SimpleStringProperty(uuId);
	}
	
	public int getId() {
		return id.intValue();
	}
	public void setId(int id) {
		this.id = new SimpleIntegerProperty(id);
	}

	public String getFsn() {
		return fsn.toString();
	}
	public void setFsn(String fsn) {
		this.fsn = new SimpleStringProperty(fsn);
	}

	public String getMatchingText() {
		return matchingText.toString();
	}
	public void setMatchingText(String matchingText) {
		this.matchingText = new SimpleStringProperty(matchingText);
	}

	public Status getStatus() {
		return status.get();
	}
	public void setStatus(Status status) {
		this.status = new SimpleObjectProperty<>(status);
	}

	public String getMatchingTextType() {
		return type.toString();
	}
	public void setMatchingTextType(String matchingTextType) {
		this.type = new SimpleStringProperty(matchingTextType);
	}
	@Override
	public String toString() {
		return "SearchResultModel [preferredName=" + preferredName + ", score="
				+ score + ", uuId=" + uuId + ", id=" + id + ", fsn=" + fsn
				+ ", matchingText=" + matchingText + ", status=" + status
				+ ", type=" + type + "]";
	}
}
