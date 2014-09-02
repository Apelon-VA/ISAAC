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
import gov.va.isaac.model.ExportType;

import java.io.File;
import java.io.FileOutputStream;

import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the ISAAC export functionality.
 *
 * @author tnaing
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ExportFileHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ExportFileHandler.class);

    public ExportFileHandler() throws ValidationException {
        super();
    }

    /**
     * Method called by the ISAAC application to perform the export. Will be
     * invoked on a background thread.
     */
    public void doExport(int pathNid, ExportType exportType, File file) throws Exception {
        LOG.debug("exportType=" + exportType);
        LOG.debug("file=" + file);

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        if (exportType == ExportType.ECONCEPT) {
            EConceptExporter exporter = new EConceptExporter(new FileOutputStream(file));
            exporter.export(pathNid);
        } else {
            throw new UnsupportedOperationException(exportType.getDisplayName() +
                    " export not yet supported in ISAAC.");
        }
    }
}
