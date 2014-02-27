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
import gov.va.isaac.models.cem.exporter.CEMExporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the ISAAC export functionality.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ExportHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ExportHandler.class);

    public ExportHandler() throws ValidationException, IOException {
        super();
    }

    /**
     * Method called by the ISAAC application to perform the export. Will be
     * invoked on a background thread.
     */
    public void doExport(File file) throws Exception {
        LOG.debug("doExport: file=" + file.getName());

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        // Get "Blood pressure taking (procedure)" concept.
        UUID conceptUUID = UUID.fromString("215fd598-e21d-3e27-a0a2-8e23b1b36dfc");

        // Export CEM model to file.
        CEMExporter exporter = new CEMExporter(new FileOutputStream(file));
        exporter.exportModel(conceptUUID);
    }
}
