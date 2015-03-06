/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
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
package gov.va.isaac.classifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CycleCheck. This class is responsible to detect cyclic isa
 * relationships
 *
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class CycleCheck {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CycleCheck.class);

  /** The concepts. Map of id->seen */
  private Map<Integer, Boolean> concepts;

  /** The isa relationships map. Map of sourceId->destinationIds */
  private Map<Integer, List<Integer>> isarelationships;

  /** The concept in loop. */
  private Set<Integer> conceptInLoop;

  /** The output file. */
  private String outputFile;

  /** The reviewed flag. */
  private int reviewed;

  /**
   * Instantiates an empty {@link CycleCheck}.
   */
  public CycleCheck() {
    //
  }

  /**
   * Cycle detected. Must set concepts and isa relationships data structures
   * before running.
   *
   * @return true, if successful
   * @throws FileNotFoundException the file not found exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ClassificationException the classification exception
   */
  public boolean cycleDetected() throws FileNotFoundException, IOException,
    ClassificationException {
    conceptInLoop = new HashSet<>();
    for (Integer con : concepts.keySet()) {
      if (!concepts.get(con)) {
        List<Integer> desc = new ArrayList<>();
        findCycle(con, desc);
        desc.remove(con);
        reviewed++;
        concepts.put(con, true);
      }
    }
    if (conceptInLoop.size() > 0) {
      LOG.info("CYCLE DETECTED - Concepts reviewed: " + reviewed);
      LOG.info("Please get conceptId for detected cycles in file:" + outputFile);
      return true;
    }
    LOG.info("*******NO CYCLE DETECTED***** - Concepts reviewed: " + reviewed);
    return false;
  }

  /**
   * Find cycle.
   *
   * @param con the con
   * @param desc the desc
   * @throws ClassificationException the classification exception
   */
  private void findCycle(Integer con, List<Integer> desc)
    throws ClassificationException {
    List<Integer> parents = isarelationships.get(con);
    if (parents != null) {
      desc.add(con);
      for (Integer parent : parents) {
        if (desc.contains(parent)) {
          conceptInLoop.add(parent);
        } else {
          Boolean aBoolean = concepts.get(parent);
          if (aBoolean != null) {
            if (!aBoolean) {
              findCycle(parent, desc);
              desc.remove(parent);
              reviewed++;
              concepts.put(parent, true);
            }
          } else {
            throw new ClassificationException("SCTID " + parent
                + " is declared as a parent of " + con
                + " but is missing or inactive in the concept file.");
          }
        }
      }
    }
  }

  /**
   * @return the concepts
   */
  public Map<Integer, Boolean> getConcepts() {
    return concepts;
  }

  /**
   * @param concepts the concepts to set
   */
  public void setConcepts(Map<Integer, Boolean> concepts) {
    this.concepts = concepts;
  }

  /**
   * @return the isarelationships
   */
  public Map<Integer, List<Integer>> getIsarelationships() {
    return isarelationships;
  }

  /**
   * @param isarelationships the isarelationships to set
   */
  public void setIsarelationships(Map<Integer, List<Integer>> isarelationships) {
    this.isarelationships = isarelationships;
  }

  /**
   * @return the conceptInLoop
   */
  public Set<Integer> getConceptInLoop() {
    return conceptInLoop;
  }

  /**
   * @param conceptInLoop the conceptInLoop to set
   */
  public void setConceptInLoop(HashSet<Integer> conceptInLoop) {
    this.conceptInLoop = conceptInLoop;
  }
}
