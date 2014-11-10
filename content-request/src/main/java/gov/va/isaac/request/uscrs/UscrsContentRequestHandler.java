package gov.va.isaac.request.uscrs;

import gov.va.isaac.AppContext;
import gov.va.isaac.gui.conceptViews.helpers.ConceptViewerHelper;
import gov.va.isaac.interfaces.gui.views.commonFunctionality.UscrsContentRequestHandlerI;
import gov.va.isaac.request.ContentRequestHandler;
import gov.va.isaac.request.ContentRequestTrackingInfo;
import gov.va.isaac.util.WBUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.glassfish.hk2.api.PerLookup;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.jvnet.hk2.annotations.Service;

/**
 * USCRS implementation of a {@link ContentRequestHandler}.
 *
 * @author bcarlsenca
 */
@Service
@PerLookup
public class UscrsContentRequestHandler implements ContentRequestHandler,
    UscrsContentRequestHandlerI {

  /** The nid. */
  private int nid;

  // This is subject to change
  /** The Constant TERMINOLOGY. */
  private final static String TERMINOLOGY = "SNOMED CT International";

  /** The Constant NEW_CONCEPT_HEADERS. */
  private final static String[] NEW_CONCEPT_HEADERS = new String[] {
      "Request Id", "Topic", "Local Code", "Local Term",
      "Fully Specified Name", "Semantic Tag", "Preferred Term",
      "Terminology(1)", "Parent Concept Id(1)", "Terminology(2)",
      "Parent Concept Id(2)", "Terminology(3)", "Parent Concept Id(3)",
      "UMLS CUI", "Definition", "Justification", "Note"
  };

  /** The Constant NEW_RELATIONSHIP_HEADERS. */
  private final static String[] NEW_RELATIONSHIP_HEADERS = new String[] {
      "Topic", "Source Terminology", "Source Concept Id", "Relationship Type",
      "Destination Terminology", "Destination Concept Id",
      "Characteristic Type", "Refinability", "Relationship Group",
      "Justification", "Note"
  };

  @Override
  public ContentRequestTrackingInfo submitContentRequest(int nid)
    throws Exception {

    ConceptChronicleBI concept = WBUtility.getConceptVersion(nid);

    // Ideally this would connect to a request submission
    // instance and dynamically create the request. In lieu
    // of that, we simply create a spreadsheet.

    // Create workbook
    Workbook wb = new HSSFWorkbook();

    // Handle new concept
    handleNewConcept(concept, wb);

    // Handle non-isa relationships
    handleNewRels(concept, wb);

    // Save the file
    FileChooser fileChooser = new FileChooser();

    // Set extension filter.
    FileChooser.ExtensionFilter xmlFilter =
        new FileChooser.ExtensionFilter("Excel files", "*.xls", "*.xlsx");
    FileChooser.ExtensionFilter allFilter =
        new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
    fileChooser.getExtensionFilters().addAll(xmlFilter, allFilter);

    // Now determine
    UscrsContentRequestTrackingInfo info =
        new UscrsContentRequestTrackingInfo();
    info.setName(WBUtility.getConPrefTerm(concept.getNid()));
    info.setDetail("Batch USCRS submission spreadsheet successfully created.");

    // Show save file dialog.
    File file = fileChooser.showSaveDialog(null);
    if (file != null) {
      FileOutputStream out = new FileOutputStream(file);
      wb.write(out);
      out.flush();
      out.close();
      info.setIsSuccessful(true);
      AppContext.getCommonDialogs()
          .showInformationDialog("USCRS Content Request",
              "Request spreadsheet successfully generated");
    } else {
      // Assume user cancelled
      info.setIsSuccessful(false);
    }
    return info;
  }

  /**
   * Handle new concept spreadsheet tab.
   *
   * @param concept the concept
   * @param wb the wb
   * @throws Exception the exception
   */
  private void handleNewConcept(ConceptChronicleBI concept, Workbook wb)
    throws Exception {

    CreationHelper createHelper = wb.getCreationHelper();
    // Set font
    Font font = wb.createFont();
    font.setFontName("Cambria");
    font.setFontHeightInPoints((short) 11);

    // Fonts are set into a style
    CellStyle style = wb.createCellStyle();
    style.setFont(font);

    Sheet sheet = wb.createSheet("New Concept");

    // Create header row and add cells
    int rownum = 0;
    int cellnum = 0;
    Row row = sheet.createRow(rownum++);
    Cell cell = null;

    for (String header : NEW_CONCEPT_HEADERS) {
      cell = row.createCell(cellnum++);
      cell.setCellStyle(style);
      cell.setCellValue(createHelper.createRichTextString(header));
    }

    // Add data row
    cellnum = 0;
    row = sheet.createRow(rownum++);

    // Request ID
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString("1"));

    // Topic - consider making the user enter this (TODO: )
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString("New concept"));

    // Local code
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString(concept
        .getPrimordialUuid().toString()));

    // Local term
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString(WBUtility
        .getConPrefTerm(concept.getNid())));

    // Fully Specified Name (without the semantic tag)
    String fsn = WBUtility.getFullySpecifiedName(concept);
    String st = fsn;
    if (fsn.indexOf('(') != -1) {
      fsn = fsn.substring(0, fsn.lastIndexOf('('));
    }
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString(fsn));

    // Semantic tag
    if (st.indexOf('(') != -1) {
      st = st.substring(st.lastIndexOf('(') + 1, st.lastIndexOf(')'));
    } else {
      throw new Exception(
          "Cannot submit a concept to USCRS without an FSN having a valid semantic tag.");
    }
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString(st));

    // Preferred term
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString(WBUtility
        .getConPrefTerm(concept.getNid())));

    // PARENTS
    List<String> parentIds = new ArrayList<>();
    for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {
      RelationshipVersionBI<?> relVersion =
          rel.getVersion(WBUtility.getViewCoordinate());
      // check for "isa" relationship type
      if (relVersion.getTypeNid() == Snomed.IS_A.getLenient().getNid()
          && relVersion.isActive()) {
        parentIds
            .add(ConceptViewerHelper.getSctId(
                WBUtility.getConceptVersion(relVersion.getDestinationNid()))
                .trim());
      }
    }

    for (int i = 1; i < 4; i++) {

      if (parentIds.size() < i) {
        // Terminology(i)
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString(""));

        // Parent Concept Id(i)
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString(""));

      } else {

        // Terminology(i)
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString(TERMINOLOGY));

        // Parent Concept Id(i)
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString(parentIds.get(i)));

      }
    }

    // UMLS CUI
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString(""));

    // Definition - consider making the user enter this (TODO: )
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper
        .createRichTextString("See logical definition in relationships"));

    // Proposed Use - consider making the user enter this (TODO: )
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString(""));

    // Justification - consider making the user enter this (TODO: )
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper
        .createRichTextString("Developed as part of extension namespace "
            + AppContext.getAppConfiguration().getExtensionNamespace()));

    // Note - add a note for any extra synonyms, to avoid worries about how many
    // column headers to have
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    List<String> synonyms = new ArrayList<>();
    for (DescriptionChronicleBI desc : concept.getDescriptions()) {
      DescriptionVersionBI<?> descVersion =
          desc.getVersion(WBUtility.getViewCoordinate());
      // find active, non FSN descriptions not matching the preferred name
      if (descVersion.isActive()
          && descVersion.getTypeNid() != Snomed.FULLY_SPECIFIED_DESCRIPTION_TYPE
              .getLenient().getNid()
          && !descVersion.getText().equals(
              WBUtility.getConPrefTerm(concept.getNid()))) {
        synonyms.add(descVersion.getText());
      }
    }
    if (synonyms.size() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append("NOTE: this concept also has the following synonyms: ");
      boolean firstSeen = false;
      for (String sy : synonyms) {
        if (firstSeen) {
          sb.append(", ");
        }
        sb.append(sy);
        firstSeen = true;
      }
    } else {
      cell.setCellValue(createHelper.createRichTextString(""));
    }

    for (int i = 2; i < NEW_CONCEPT_HEADERS.length; i++) {
      sheet.autoSizeColumn(i);
    }

  }

  /**
   * Handle new rels spreadsheet tab
   *
   * @param concept the concept
   * @param wb the wb
   * @throws Exception the exception
   */
  private void handleNewRels(ConceptChronicleBI concept, Workbook wb)
    throws Exception {

    CreationHelper createHelper = wb.getCreationHelper();
    // Set font
    Font font = wb.createFont();
    font.setFontName("Cambria");
    font.setFontHeightInPoints((short) 11);

    // Fonts are set into a style
    CellStyle style = wb.createCellStyle();
    style.setFont(font);

    Sheet sheet = wb.createSheet("New Relationship");

    // Create header row and add cells
    int rownum = 0;
    int cellnum = 0;
    Row row = sheet.createRow(rownum++);
    Cell cell = null;

    for (String header : NEW_RELATIONSHIP_HEADERS) {
      cell = row.createCell(cellnum++);
      cell.setCellStyle(style);
      cell.setCellValue(createHelper.createRichTextString(header));
    }

    for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {

      RelationshipVersionBI<?> relVersion =
          rel.getVersion(WBUtility.getViewCoordinate());
      // find active, non-ISA relationships
      if (relVersion.isActive()
          && relVersion.getTypeNid() != Snomed.IS_A.getLenient().getNid()) {

        // Add data row
        cellnum = 0;
        row = sheet.createRow(rownum++);

        // Topic - consider making the user enter this (TODO: )
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper
            .createRichTextString("See new concept request"));

        // Source terminology
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper
            .createRichTextString("Current Batch Requests"));

        // Source Concept Id - aligns with Request Id from the new concept
        // spreadsheet
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString("1"));

        // Relationship Type
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString(WBUtility
            .getConPrefTerm(relVersion.getTypeNid())));

        // Destination Termionlogy - TODO: here we're only supporting
        // things linked to SNOMED, in the future we may need to link
        // to things that have been previously created, but we need tracking
        // info integration to do that properly.
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString(TERMINOLOGY));

        // Destination Concept Id
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString(ConceptViewerHelper
            .getSctId(
                WBUtility.getConceptVersion(relVersion.getDestinationNid()))
            .trim()));

        // Characteristic Type
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper
            .createRichTextString("Defining relationship"));

        // Refinability
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString("Not refinable"));

        // Relationship Group
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString(""
            + relVersion.getGroup()));

        // Relationship Group
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString(""
            + relVersion.getGroup()));

        // Justification - consider making the user enter this (TODO: )
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper
            .createRichTextString("Developed as part of extension namespace "
                + AppContext.getAppConfiguration().getExtensionNamespace()));

        // Note
        cell = row.createCell(cellnum++);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper
            .createRichTextString("This is a defining relationship expressed for the "
                + "corresponding new concept request in the other tab"));
      }

      for (int i = 2; i < NEW_RELATIONSHIP_HEADERS.length; i++) {
        sheet.autoSizeColumn(i);
      }

    }
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
      AppContext.getCommonDialogs().showErrorDialog("USCRS Content Request",
          "Unable to load concept for " + nid, "This should never happen");
      return;
    }

    try {
      if (concept.getPathNid() != TermAux.SNOMED_CORE.getLenient().getNid()
          && !WBUtility
              .getConceptVersion(concept.getPathNid())
              .getPrimordialUuid()
              .toString()
              .equals(AppContext.getAppConfiguration().getDefaultEditPathUuid())) {
        AppContext.getCommonDialogs().showErrorDialog(
            "USCRS Content Request",
            "Unexpected path for USCRS content request submission.",
            "The concept path is neither Snomed CORE nor\n"
                + AppContext.getAppConfiguration().getDefaultEditPathName()
                + ". " + "It is recommended that you only submit\n"
                + "concepts edited on one of these paths to USCRS");
      }
    } catch (IOException e) {
      AppContext.getCommonDialogs().showErrorDialog("USCRS Content Request",
          "Unable to load concepts for path comparison.",
          "This should never happen");
      return;
    }

    try {
      ContentRequestTrackingInfo info = submitContentRequest(nid);
      if (info.isSuccessful()) {
        AppContext.getCommonDialogs().showInformationDialog("USCRS Content Request",
            "Content request submission successful.\nSee saved file and upload here: "
            + info.getUrl());        
      }
    } catch (Exception e) {
      AppContext.getCommonDialogs().showErrorDialog("USCRS Content Request",
          "Unexpected error trying to submit request.",
          e.getMessage());
      return;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.
   * UscrsContentRequestHandlerI#setConcept(int)
   */
  @Override
  public void setConcept(int conceptNid) {
    this.nid = conceptNid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.interfaces.gui.views.commonFunctionality.
   * UscrsContentRequestHandlerI#getConceptNid()
   */
  @Override
  public int getConceptNid() {
    return nid;
  }

}
