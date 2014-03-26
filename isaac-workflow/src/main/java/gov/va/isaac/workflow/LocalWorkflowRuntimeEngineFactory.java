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
package gov.va.isaac.workflow;

import gov.va.isaac.workflow.impl.LocalWfEngine;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alo
 */
public class LocalWorkflowRuntimeEngineFactory {
    
    public static LocalWorkflowRuntimeEngineBI getRuntimeEngine() {
        LocalWfEngine lwf = null;
        try {
            // TODO: implement, create engine based on app configuration
            lwf = new LocalWfEngine(new URL("http://162.243.255.43:8080/kie-wb/"),
                    "alejandro", "alejandro", "gov.va.isaac.demo:terminology-authoring:1.2");
        } catch (MalformedURLException ex) {
            Logger.getLogger(LocalWorkflowRuntimeEngineFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lwf;
    }
    
}
