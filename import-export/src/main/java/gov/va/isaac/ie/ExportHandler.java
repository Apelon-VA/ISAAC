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
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.cem.exporter.CEMExporter;
import gov.va.isaac.models.fhim.exporter.FHIMExporter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the ISAAC information model export functionality.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author bcarlsenca
 */
public class ExportHandler {

  /**  The Constant LOG. */
  private static final Logger LOG = LoggerFactory
      .getLogger(ExportHandler.class);

  /**
   * Instantiates an empty {@link ExportHandler}.
   *
   * @throws ValidationException the validation exception
   */
  public ExportHandler() throws ValidationException {
    super();
  }

  /**
   * Method called by the ISAAC application to perform the export. Will be
   * invoked on a background thread.
   *
   * @param informationModel the information model
   * @param file the file
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public void doExport(InformationModel informationModel, File file)
    throws Exception {
    InformationModelType modelType = informationModel.getType();
    LOG.debug("modelType=" + modelType);
    LOG.debug("file=" + file);

    // Make sure NOT in application thread.
    FxUtils.checkBackgroundThread();

    if (modelType == InformationModelType.CEM) {

      FileOutputStream out = new FileOutputStream(file);
      CEMExporter exporter = new CEMExporter(out);
      UUID modelUUID = informationModel.getUuid();
      exporter.exportModel(modelUUID);
      out.close();

    } else if (modelType == InformationModelType.FHIM) {

      FileOutputStream out = new FileOutputStream(file);
      FHIMExporter exporter = new FHIMExporter(out);
      UUID modelUUID = informationModel.getUuid();
      exporter.exportModel(modelUUID);
      out.close();

    } else {
      throw new UnsupportedOperationException(modelType.getDisplayName()
          + " export not yet supported in ISAAC.");
    }
  }
}
