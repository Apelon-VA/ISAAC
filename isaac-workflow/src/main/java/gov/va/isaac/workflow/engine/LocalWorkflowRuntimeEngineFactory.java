/* 
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.workflow.engine;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.workflow.LocalWorkflowRuntimeEngineBI;
import gov.va.isaac.workflow.engine.LocalWfEngine;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alo
 */
public final class LocalWorkflowRuntimeEngineFactory {
	private LocalWorkflowRuntimeEngineFactory() {}

    private static LocalWorkflowRuntimeEngineBI lwf;

    //TODO DAN this needs to be converted to HK2.  And I assume we only want one existing in the runtime at once... this shouldn't create a new 
    //one with each call.

    //TODO DAN this needs to prompt the user for the credentials if the built in ones fail...
    public static LocalWorkflowRuntimeEngineBI getRuntimeEngine() {
        if (lwf == null) {
            try {
                // TODO: implement, create engine based on app configuration
                lwf = new LocalWfEngine(new URL("http://162.243.255.43:8080/kie-wb/"),
                        ExtendedAppContext.getCurrentlyLoggedInUser().getWorkflowUsername(), ExtendedAppContext.getCurrentlyLoggedInUser().getWorkflowPassword(),
                        "gov.va.isaac.demo:terminology-authoring:1.4");
                lwf.getLocalTaskService().createSchema();
                lwf.getProcessInstanceService().createSchema();
            } catch (MalformedURLException ex) {
                Logger.getLogger(LocalWorkflowRuntimeEngineFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return lwf;
    }  
}
