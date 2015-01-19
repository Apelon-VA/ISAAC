package gov.va.isaac.ie.exporter;

import gov.va.isaac.util.ProgressReporter;
import gov.va.isaac.util.OTFUtility;

import java.io.IOException;

import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

/**
 * Generically represents an Exporter.
 */
public interface Exporter extends ProgressReporter {

  /**
   * Exports the nid.
   *
   * @param pathNid the path nid
   * @throws Exception
   */
  public void export(int pathNid) throws Exception;

  /**
   * Cancel.
   */
  public void cancel();

  /**
   * Indicates whether or not the concept qualifies as having a component
   * with the indicated path.
   *
   * @param conceptNid the concept nid
   * @param pathNid the path nid
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws IOException
   * @throws ContradictionException
   */
  public static boolean isQualifying(int conceptNid, int pathNid)
    throws ContradictionException, IOException {
    ConceptVersionBI cv = OTFUtility.getConceptVersion(conceptNid);
    if (cv.getPathNid() == pathNid)
      return true;

    ViewCoordinate vc = OTFUtility.getViewCoordinate();

    // atts
    ConceptAttributeVersionBI<?> att =
        cv.getConceptAttributes().getVersion(vc);
    if (att == null)
      return false;
    if (att.getPathNid() == pathNid)
      return true;
    
    // descs
    for (DescriptionChronicleBI dc : cv.getDescriptions()) {
      DescriptionVersionBI<?> dv = dc.getVersion(vc);
      if (dv.getPathNid() == pathNid) 
        return true;
    }

    // rels
    for (RelationshipChronicleBI rc : cv.getRelationshipsOutgoing()) {
      RelationshipVersionBI<?> rv = rc.getVersion(vc);
      if (rv.getPathNid() == pathNid) 
        return true;
    }
    
    // refex
    for (RefexChronicleBI<?> rc : cv.getAnnotations()) {
      RefexVersionBI<?> rv = rc.getVersion(vc);
      if (rv.getPathNid() == pathNid) 
        return true;
    }
    
    return false;

  }
}
