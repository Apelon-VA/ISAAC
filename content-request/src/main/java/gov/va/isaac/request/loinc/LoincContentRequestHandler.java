package gov.va.isaac.request.loinc;

import gov.va.isaac.AppContext;
import gov.va.isaac.interfaces.gui.constants.SharedServiceNames;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.ContentRequestHandlerI;
import gov.va.isaac.interfaces.utility.DialogResponse;
import gov.va.isaac.request.ContentRequestHandler;
import gov.va.isaac.request.ContentRequestTrackingInfo;
import gov.va.isaac.util.CommonMenus;
import gov.va.isaac.util.WBUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.inject.Named;

import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringMember;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LOINC implementation of a {@link ContentRequestHandler} for the
 * "basic submission template"
 * @see http://loinc.org/submissions/new-terms
 * @author bcarlsenca
 */
@Service
@Named(value = SharedServiceNames.LOINC)
@PerLookup
public class LoincContentRequestHandler implements ContentRequestHandler,
    ContentRequestHandlerI {

  /** The nid. */
  private int nid;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CommonMenus.class);

  /** The Constant NEW_CONCEPT_HEADERS. */
  private final static String[] NEW_CONCEPT_HEADERS = new String[] {
      "Reference #", "Local observation code", "Local observation name",
      "Observation description", "Reference Info/URL", "Component", "Property",
      "Timing", "System", "Scale", "Method", "Answers", "Units", "Formula"
  };

  @Override
  public LoincContentRequestTrackingInfo submitContentRequest(int nid)
    throws Exception {
    LOG.debug("Submit content Request");

    ConceptChronicleBI concept = WBUtility.getConceptVersion(nid);

    // Ideally this would connect to a request submission
    // instance and dynamically create the request. In lieu
    // of that, we simply create a spreadsheet.

    // Here, we simply need to make a TSV to import into the LOINC spreadsheet.
    StringBuilder sb = new StringBuilder();

    // Handle new concept
    handleNewConcept(concept, sb);

    // Save the file
    LOG.info("Choose file to save");
    FileChooser fileChooser = new FileChooser();

    // Set extension filter.
    FileChooser.ExtensionFilter xmlFilter =
        new FileChooser.ExtensionFilter("Excel files", "*.xls", "*.xlsx");
    FileChooser.ExtensionFilter allFilter =
        new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
    fileChooser.getExtensionFilters().addAll(xmlFilter, allFilter);

    // Now determine
    LoincContentRequestTrackingInfo info =
        new LoincContentRequestTrackingInfo();
    info.setName(WBUtility.getConPrefTerm(concept.getNid()));

    // Show save file dialog.
    File file = fileChooser.showSaveDialog(null);
    LOG.info("  file = " + file);
    if (file != null) {
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
      out.print(sb.toString());
      out.flush();
      out.close();
      info.setIsSuccessful(true);
      info.setFile(file.toString());
      info.setDetail("Batch LOINC submission tab-separated-values file successfully created.");
    } else {
      // Assume user cancelled
      info.setIsSuccessful(false);
      info.setDetail("Submission cancelled.");
    }
    return info;
  }

  /**
   * Handle new concept spreadsheet tab.
   *
   * @param concept the concept
   * @param sb the string builder
   * @throws Exception the exception
   */
  private void handleNewConcept(ConceptChronicleBI concept, StringBuilder sb)
    throws Exception {
    LOG.debug("  Handle new concept tab");

    // Reference #
    sb.append(concept.getPrimordialUuid()).append("\t");

    // Local observation code - TODO: ??, do we have anything other than uuid?
    sb.append(concept.getPrimordialUuid()).append("\t");

    // Local observation name - concept preferred name
    sb.append(WBUtility.getConPrefTerm(concept.getNid())).append("\t");

    // Observation description - if there is a definition
    sb.append(getDescriptionText(concept, "Definition (core metadata concept)")).append("\t");

    // Reference Info/URL
    sb.append(concept.getPrimordialUuid()).append("\t");

    // Component
    sb.append(getDestinationText(concept, "Has_COMPONENT")).append("\t");

    // Property
    sb.append(getDestinationText(concept, "Has_PROPERTY")).append("\t");

    // Timing
    sb.append(getDestinationText(concept, "Has_TIME_ASPECT")).append("\t");

    // System
    sb.append(getDestinationText(concept, "Has_SYSTEM")).append("\t");

    // Scale
    sb.append(getDestinationText(concept, "Has_SCALE_TYP")).append("\t");

    // Method
    sb.append(getDestinationText(concept, "Has_METHOD_TYP")).append("\t");

    // Units
    sb.append(getAttributeText(concept, "EXAMPLE_UNITS")).append("\t");

    // Formula
    sb.append(getAttributeText(concept, "FORMULA")).append("\t");

    sb.append("\r\n");

    for (RefexChronicleBI<?> refex : concept.getAnnotations()) {
      LOG.debug("  refex = " + refex.toUserString());
    }

    for (IdBI id : concept.getAllIds()) {
      LOG.debug("  id = " + id.toString());
    }
  }

  /**
   * Returns the description text.
   *
   * @param concept the concept
   * @param type the type
   * @return the description text
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ContradictionException the contradiction exception
   */
  private String getDescriptionText(ConceptChronicleBI concept, String type)
    throws IOException, ContradictionException {
    for (DescriptionChronicleBI desc : concept.getDescriptions()) {
      DescriptionVersionBI<?> descVersion =
          desc.getVersion(WBUtility.getViewCoordinate());
      // WARNING:
      // LOINC is created using FSN and not PT for this, the
      // metadata concepts do not have PTs.
      String prefName =
          WBUtility.getConceptVersion(descVersion.getTypeNid())
              .getFullySpecifiedDescription().getText();
      if (descVersion.isActive() && prefName.equals(type)) {
        return descVersion.getText();
      }
    }
    return "";
  }

  /**
   * Returns the attribute text.
   *
   * @param concept the concept
   * @param type the type
   * @return the attribute text
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private String getAttributeText(ConceptChronicleBI concept, String type)
    throws Exception {
    for (RefexChronicleBI<?> refex : concept.getAnnotations()) {
      RefexVersionBI<?> refexVersion =
          refex.getVersion(WBUtility.getViewCoordinate());
      // WARNING:
      // LOINC is created using FSN and not PT for this, the
      // metadata concepts do not have PTs.
      String prefName =
          WBUtility.getConceptVersion(refexVersion.getAssemblageNid())
              .getFullySpecifiedDescription().getText();
      if (refexVersion.isActive() && refexVersion instanceof StringMember
          && prefName.equals(type)) {
        return ((StringMember) refexVersion).getString1();
      }
    }
    return "";
  }

  /**
   * Returns the destination text.
   *
   * @param concept the concept
   * @param type the type
   * @return the destination text
   * @throws Exception the exception
   */
  private String getDestinationText(ConceptChronicleBI concept, String type)
    throws Exception {
    for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {
      RelationshipVersionBI<?> relVersion =
          rel.getVersion(WBUtility.getViewCoordinate());
      String prefName = WBUtility.getConPrefTerm(relVersion.getTypeNid());
      if (relVersion.isActive() && prefName.equals(type)) {
        return WBUtility.getConPrefTerm(relVersion.getDestinationNid());
      }
    }
    return "";
  }

  @Override
  public ContentRequestTrackingInfo getContentRequestStatus(
    ContentRequestTrackingInfo info) {
    // TODO:
    throw new UnsupportedOperationException(
        "PLACEHOLDER for future functionality");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.va.isaac.interfaces.gui.views.PopupViewI#showView(javafx.stage.Window)
   */
  @Override
  public void showView(Window parent) {
    // No view, per se is needed, though we could
    // put a warning here if the request won't make sense
    ConceptVersionBI concept = WBUtility.getConceptVersion(nid);
    if (concept == null) {
      AppContext.getCommonDialogs().showErrorDialog("LOINC Content Request",
          "Unable to load concept for " + nid, "This should never happen");
      return;
    }

    // Check LOINC Path and current edit path
    if (!WBUtility.getConceptVersion(concept.getPathNid()).getPrimordialUuid()
        .toString().equals("b2b1cc96-9ca6-5513-aad9-aa21e61ddc29")
        && !WBUtility.getConceptVersion(concept.getPathNid())
            .getPrimordialUuid().toString()
            .equals(AppContext.getAppConfiguration().getDefaultEditPathUuid())) {
      DialogResponse response =
          AppContext.getCommonDialogs().showYesNoDialog(
              "LOINC Content Request",
              "The concept path is neither LOINC Path nor "
                  + AppContext.getAppConfiguration().getDefaultEditPathName()
                  + ". It is recommended that you only submit "
                  + "concepts edited on one of these paths to LOINC.\n\n"
                  + "Do you want to continue?");
      if (response == DialogResponse.NO) {
        return;
      }
    }

    try {
      LoincContentRequestTrackingInfo info = submitContentRequest(nid);
      if (info.isSuccessful()) {
        AppContext.getCommonDialogs().showInformationDialog(
            "LOINC Content Request",
            "Content request submission successful.\n\nDownload the "
                + "submission template from " + info.getUrl() + " and import "
                + "the data from " + info.getFile() + " and submit it.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      AppContext.getCommonDialogs().showErrorDialog("LOINC Content Request",
          "Unexpected error trying to submit request.", e.getMessage());
      return;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.
   * LoincContentRequestHandlerI#setConcept(int)
   */
  @Override
  public void setConcept(int conceptNid) {
    this.nid = conceptNid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.
   * LoincContentRequestHandlerI#getConceptNid()
   */
  @Override
  public int getConceptNid() {
    return nid;
  }

}
