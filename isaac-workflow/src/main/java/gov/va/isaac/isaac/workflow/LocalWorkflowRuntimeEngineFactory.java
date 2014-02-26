/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.isaac.isaac.workflow;

import gov.va.isaac.isaac.workflow.impl.LocalWfEngine;
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
