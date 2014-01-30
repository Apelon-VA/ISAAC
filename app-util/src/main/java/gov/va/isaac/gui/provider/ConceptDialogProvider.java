package gov.va.isaac.gui.provider;

import java.util.UUID;

import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;

/**
 * Contract for showing a concept detail dialog.
 *
 * @author ocarlsen
 */
public interface ConceptDialogProvider {

    public void showSnomedConceptDialog(UUID conceptUUID);

    public void showSnomedConceptDialog(ConceptChronicleDdo concept);
}
