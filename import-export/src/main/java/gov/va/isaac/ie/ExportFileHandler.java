/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.ie;

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.ie.exporter.EConceptExporter;
import gov.va.isaac.ie.exporter.Exporter;
import gov.va.isaac.ie.exporter.OWLExporter;
import gov.va.isaac.ie.exporter.Rf2Export;
import gov.va.isaac.ie.exporter.Rf2File.ReleaseType;
import gov.va.isaac.model.ExportType;
import gov.va.isaac.util.ProgressListener;
import gov.va.isaac.util.WBUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.validation.ValidationException;

import org.ihtsdo.otf.tcc.api.country.COUNTRY_CODE;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the ISAAC export to file functionality.
 *
 * @author tnaing
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author bcarlsenca
 */
public class ExportFileHandler {

  /** The Constant LOG. */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory
      .getLogger(ExportFileHandler.class);

  /** The path nid. */
  private int pathNid;

  /** The export type. */
  private ExportType exportType;

  /** The folder. */
  private File folder;

  /** The zip. */
  private boolean zip;

  /**  The exporter. */
  private Exporter exporter;
  
  /**
   * Instantiates an empty {@link ExportFileHandler}.
   * @param pathNid the path nid to export
   * @param exportType the type of export (e.g. format)
   * @param folder the folder to export to
   * @param zip zip flag (compress output)
   *
   * @throws ValidationException the validation exception
   */
  public ExportFileHandler(int pathNid, ExportType exportType, File folder,
      boolean zip) throws ValidationException {
    super();
    this.pathNid = pathNid;
    this.exportType = exportType;
    this.folder = folder;
    this.zip = zip;
  }

  /**
   * Method called by the ISAAC application to perform the export. Will be
   * invoked on a background thread.
   * @param listener the listener, leave null if not needed
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public void doExport(ProgressListener listener) throws Exception {

    // Make sure NOT in application thread.
    FxUtils.checkBackgroundThread();

    // Handle eConcept
    if (exportType == ExportType.ECONCEPT) {
      String fileName = "eConcepts.jbin";
      File file = new File(folder, fileName);
      checkExists(file);

      OutputStream outputStream = null;
      if (zip) {
        ZipOutputStream zos =
            new ZipOutputStream(new FileOutputStream(file + ".zip"));
        ZipEntry entry = new ZipEntry(fileName);
        entry.setMethod(ZipEntry.DEFLATED);
        zos.putNextEntry(entry);
        outputStream = zos;
      } else {
        outputStream = new FileOutputStream(file);
      }

      EConceptExporter econceptExporter = new EConceptExporter(outputStream);
      exporter = econceptExporter;
      econceptExporter.addProgressListener(listener);
      econceptExporter.export(pathNid);
      outputStream.flush();
      outputStream.close();

    }

    // Handle Owl
    else if (exportType == ExportType.OWL) {
      // Can only export SNOMED at this time
      if (pathNid != Snomed.SNOMED_RELEASE_PATH.getNid()) {
        throw new UnsupportedOperationException(
            "OWL Exporter only supports SNOMED path at this time.");
      }
      String fileName = "snomed.owl";
      File file = new File(folder, fileName);
      checkExists(file);
      OutputStream outputStream = null;
      if (zip) {
        ZipOutputStream zos =
            new ZipOutputStream(new FileOutputStream(file + ".zip"));
        ZipEntry entry = new ZipEntry(fileName);
        entry.setMethod(ZipEntry.DEFLATED);
        zos.putNextEntry(entry);
        outputStream = zos;
      } else {
        outputStream = new FileOutputStream(file);
      }

      OWLExporter owlExporter = new OWLExporter(outputStream);
      exporter = owlExporter;
      owlExporter.addProgressListener(listener);
      owlExporter.export(pathNid);
      outputStream.flush();
      outputStream.close();

    }

    // Handle RF2
    else if (exportType == ExportType.RF2) {

      // Settings for RF2 Export
      // TODO: can get these from AppContext.getAppConfiguration() in the future
      // adding new properties involves tweaking AppConfigSchema.xsd in isaac-app, and then 
      // following the changes through the implementation... IsaacAppConfigWrapper and IsaacAppConfigI
      String namespace = "1000000";
      ReleaseType releaseType = ReleaseType.SNAPSHOT;
      Collection<Integer> taxonomyParentNids = new ArrayList<>();
      taxonomyParentNids.add(Taxonomies.SNOMED.getLenient().getNid());
      File releaseFolder = new File(folder, releaseType.suffix);
      checkExists(releaseFolder);
      Rf2Export rf2Export =
          new Rf2Export(releaseFolder, releaseType, LanguageCode.EN_US,
              COUNTRY_CODE.US, namespace, Calendar.getInstance().getTime(),
              pathNid, WBUtility.getViewCoordinate(), taxonomyParentNids);
      exporter = rf2Export;
      rf2Export.addProgressListener(listener);
      rf2Export.export(pathNid);
      rf2Export.writeOneTimeFiles();
      rf2Export.close();
      if (zip) {
        File file = new File(folder, releaseType.suffix + ".zip");
        JarOutputStream output =
            new JarOutputStream(new FileOutputStream(file));
        recursiveAddToZip(output, releaseFolder, releaseType.suffix);
        output.close();
        removeDirectory(releaseFolder);
      }
    } else {
      throw new UnsupportedOperationException(exportType.getDisplayName()
          + " export not yet supported in ISAAC.");
    }
  }

  /**
   * Recursive add to zip.
   *
   * @param output the output
   * @param parent the parent
   * @param prefix the prefix
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static void recursiveAddToZip(ZipOutputStream output, File parent,
    String prefix) throws IOException {
    if (parent == null) {
      return;
    }
    for (File child : parent.listFiles()) {
      if (child.isDirectory()) {
        ZipEntry entry =
            new ZipEntry(prefix + File.separator + child.getName()
                + File.separator);
        output.putNextEntry(entry);
        recursiveAddToZip(output, child,
            prefix + File.separator + child.getName());
      } else {
        addToZip(prefix, child, output, null);
      }
    }
  }

  /**
   * Check exists.
   *
   * @param file the file
   * @throws Exception the exception if it exists.
   */
  private void checkExists(File file) throws Exception {
    if (file.exists()) {
      throw new IOException("Output already exists, clean up first. "
          + file);
    }
  }

  
  /**
   * Adds the to zip.
   *
   * @param prefix the prefix
   * @param f the f
   * @param output the output
   * @param comment the comment
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static void addToZip(String prefix, File f, ZipOutputStream output,
    String comment) throws IOException {
    ZipEntry entry = new ZipEntry(prefix + File.separator + f.getName());
    if (f.exists()) {
      entry.setSize(f.length());
      entry.setTime(f.lastModified());
    } else {
      entry.setSize(0);
      entry.setTime(System.currentTimeMillis());
    }
    entry.setComment(comment);
    output.putNextEntry(entry);
    if (f.exists()) {
      FileInputStream fis = new FileInputStream(f);
      byte[] buffer = new byte[10240];
      int length;
      while ((length = fis.read(buffer)) > 0) {
        output.write(buffer, 0, length);
      }
      fis.close();
    }
    output.closeEntry();
  }

  /**
   * Removes the directory.
   *
   * @param directory the directory
   * @return true, if successful
   */
  private static boolean removeDirectory(File directory) {

    if (directory == null)
      return false;
    if (!directory.exists())
      return true;
    if (!directory.isDirectory())
      return false;

    String[] list = directory.list();

    if (list != null) {
      for (int i = 0; i < list.length; i++) {
        File entry = new File(directory, list[i]);
        if (entry.isDirectory()) {
          if (!removeDirectory(entry))
            return false;
        } else {
          if (!entry.delete())
            return false;
        }
      }
    }

    return directory.delete();
  }

  /**
   * Do cancel.
   */
  public void doCancel() {
    exporter.cancel();
  }
}
