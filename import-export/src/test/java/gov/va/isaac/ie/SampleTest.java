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

import gov.va.isaac.AppContext;
import gov.va.isaac.models.util.ExporterBase;
import gov.va.isaac.util.WBUtility;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid.NidMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Utility class for bootstrapping a test.
 *
 * @author bcarlsenca
 */
public class SampleTest extends ExporterBase {

  /**  The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(SampleTest.class);

  /**
   * Instantiates an empty {@link SampleTest}.
   *
   * @throws Exception the exception
   */
  private SampleTest() throws Exception {
    super();
  }

  /* (non-Javadoc)
   * @see gov.va.isaac.models.util.ExporterBase#getLogger()
   */
  @Override
  protected Logger getLogger() {
    return LOG;
  }

  /**
   * Shutdown.
   */
  public void shutdown() {
    getDataStore().shutdown();
  }

  /**
   * Application entry point.
   *
   * @param args the command line arguments
   * @throws Exception the exception
   */
  public static void main(String[] args) throws Exception {

    // Set up like ISAAC App.
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    AppContext.setup();
    // TODO OTF fix: this needs to be fixed so I don't have to hack it with
    // reflection....(https://jira.ihtsdotools.org/browse/OTFISSUE-11)
    Field f = Hk2Looker.class.getDeclaredField("looker");
    f.setAccessible(true);
    f.set(null, AppContext.getServiceLocator());
    System.setProperty(BdbTerminologyStore.BDB_LOCATION_PROPERTY, new File(
        "../isaac-app/berkeley-db").getAbsolutePath());

    // FHIM Models RS.
    SampleTest tester = new SampleTest();
    
    tester.shutdown();
  }

}
