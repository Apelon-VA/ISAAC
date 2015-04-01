package gov.va.isaac.gui;

import java.util.function.Function;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;

/**
 * An extension of SimpleDisplayConcept that can be used when you need to change
 * the description shown in combos and lists.
 * 
 * @author dtriglianos
 *
 */
public class RenameableDisplayConcept extends SimpleDisplayConcept {

	public void setDescription(String description) {
		this.description_ = description;
	}

	public RenameableDisplayConcept(String description, int nid,
			boolean ignoreChange) {
		super(description, nid, ignoreChange);
		// TODO Auto-generated constructor stub
	}

	public RenameableDisplayConcept(ConceptVersionBI c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	public RenameableDisplayConcept(ConceptVersionBI c,
			Function<ConceptVersionBI, String> descriptionReader) {
		super(c, descriptionReader);
		// TODO Auto-generated constructor stub
	}

	public RenameableDisplayConcept(ConceptChronicleDdo c,
			Function<ConceptVersionBI, String> descriptionReader) {
		super(c, descriptionReader);
		// TODO Auto-generated constructor stub
	}

	public RenameableDisplayConcept(ConceptChronicleDdo c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	public RenameableDisplayConcept(ConceptChronicleBI c,
			Function<ConceptVersionBI, String> descriptionReader) {
		super(c, descriptionReader);
		// TODO Auto-generated constructor stub
	}

	public RenameableDisplayConcept(ConceptSpec c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	public RenameableDisplayConcept(String description) {
		super(description);
		// TODO Auto-generated constructor stub
	}

	public RenameableDisplayConcept(String description, int nid) {
		super(description, nid);
		// TODO Auto-generated constructor stub
	}

}
