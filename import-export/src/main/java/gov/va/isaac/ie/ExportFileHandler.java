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

import gov.va.isaac.constants.ISAAC;
import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.ie.exporter.EConceptExporter;
import gov.va.isaac.ie.exporter.Rf2Export;
import gov.va.isaac.ie.exporter.Rf2File.ReleaseType;
import gov.va.isaac.model.ExportType;
import gov.va.isaac.models.owl.exporter.OWLExporter;
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
        } else if (exportType == ExportType.RF2) {
        	// TODO: use VA namespace
        	String namespace = "1000000";
        	ReleaseType releaseType = ReleaseType.SNAPSHOT;
        	Collection<Integer> taxonomyParentNids = new ArrayList<Integer>();
        	taxonomyParentNids.add(ISAAC.ISAAC_ROOT.getNid());
        	File releaseFolder = new File(folder, releaseType.suffix);
			Rf2Export rf2Export = new Rf2Export(releaseFolder, releaseType, LanguageCode.EN_US, COUNTRY_CODE.US, namespace, Calendar.getInstance().getTime(), pathNid, WBUtility.getViewCoordinate(), taxonomyParentNids );
			rf2Export.export();
			rf2Export.writeOneTimeFiles();
			rf2Export.close();
			if(zip) {
				File file = new File(folder, releaseType.suffix + ".zip");
            	JarOutputStream output = new JarOutputStream(new FileOutputStream(file));
            	recursiveAddToZip(output, releaseFolder, releaseType.suffix);
            	output.close();
            	removeDirectory(releaseFolder);
			}
        } else {
            throw new UnsupportedOperationException(exportType.getDisplayName() +
                    " export not yet supported in ISAAC.");
        }
    }
    
    public static void recursiveAddToZip(ZipOutputStream output, File parent, String prefix) throws IOException {
        if (parent == null) {
            return;
        }
        for (File child : parent.listFiles()) {
            if (child.isDirectory()) {
            	ZipEntry entry = new ZipEntry(prefix + File.separator + child.getName() + File.separator);
            	output.putNextEntry(entry);
                recursiveAddToZip(output, child, prefix + File.separator + child.getName());
            } else {
                addToZip(prefix, child, output, null);
            }
        }
    }

    public static void addToZip(String prefix, File f, ZipOutputStream output, String comment) throws IOException {
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

    public static boolean removeDirectory(File directory) {

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
    	      if (entry.isDirectory())
    	      {
    	        if (!removeDirectory(entry))
    	          return false;
    	      }
    	      else
    	      {
    	        if (!entry.delete())
    	          return false;
    	      }
    	    }
    	  }

    	  return directory.delete();
    	}
}
