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
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.validation.ValidationException;

import gov.va.isaac.models.owl.exporter.OWLExporter;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
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
    public void doExport(int pathNid, ExportType exportType, File folder, boolean zip) throws Exception {

        // Make sure NOT in application thread.
        FxUtils.checkBackgroundThread();

        if (exportType == ExportType.ECONCEPT) {
            String fileName = "eConcepts.jbin";
            File file = new File(folder, fileName);            
            
            OutputStream outputStream = null;
			if(zip) {
            	ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file + ".zip"));
            	ZipEntry entry = new ZipEntry(fileName);
            	entry.setMethod(ZipEntry.DEFLATED);
            	zos.putNextEntry(entry);
            	outputStream = zos;
			} else {
				outputStream = new FileOutputStream(file);
			}
			
			EConceptExporter exporter = new EConceptExporter(outputStream);
            exporter.export(pathNid);
            outputStream.flush();
            outputStream.close();
        } else if (exportType == ExportType.OWL) {
            //Can only export SNOMED at this time
            if (pathNid != Snomed.SNOMED_RELEASE_PATH.getNid()) {
                throw new UnsupportedOperationException("OWL Exporter only supports SNOMED path at this time.");
            }
            String fileName = "snomed.owl";
            File file = new File(folder, fileName);
            OutputStream outputStream = null;
			if(zip) {
            	ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file + ".zip"));
            	ZipEntry entry = new ZipEntry(fileName);
            	entry.setMethod(ZipEntry.DEFLATED);
            	zos.putNextEntry(entry);
            	outputStream = zos;
			} else {
				outputStream = new FileOutputStream(file);
			}

			OWLExporter exporter = new OWLExporter(outputStream);
            exporter.export(pathNid);
            outputStream.flush();
            outputStream.close();
        } else {
            throw new UnsupportedOperationException(exportType.getDisplayName() +
                    " export not yet supported in ISAAC.");
        }
    }
}
