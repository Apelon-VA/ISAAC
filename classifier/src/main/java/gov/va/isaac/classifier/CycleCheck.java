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

import org.apache.log4j.Logger;

/**
 * The Class CycleCheck. This class is responsible to detect cyclic isa
 * relationships
 *
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class CycleCheck {

  /** The concepts. Map of id->seen */
  private Map<Long, Boolean> concepts;

  /** The isa relationships map. Map of sourceId->destinationIds */
  private Map<Long, List<Long>> isarelationships;

  /** The concept in loop. */
  private Set<Long> conceptInLoop;

  /** The output file. */
  private String outputFile;

  /** The reviewed flag. */
  private int reviewed;

  /** The logger. */
  private Logger LOG;

  /**
   * Instantiates an empty {@link CycleCheck}.
   */
  public CycleCheck() {
    LOG = Logger.getLogger(getClass());
  }

  /**
   * Cycle detected.  Must set concepts and isa
   * relationships data structures before running.
   *
   * @return true, if successful
   * @throws FileNotFoundException the file not found exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ClassificationException the classification exception
   */
  public boolean cycleDetected() throws FileNotFoundException, IOException,
    ClassificationException {
    conceptInLoop = new HashSet<>();
    for (Long con : concepts.keySet()) {
      if (!concepts.get(con)) {
        List<Long> desc = new ArrayList<>();
        findCycle(con, desc);
        desc.remove(con);
        reviewed++;
        concepts.put(con, true);
      }
    }
    if (conceptInLoop.size() > 0) {
      LOG.info("CYCLE DETECTED - Concepts reviewed: " + reviewed);
      LOG.info("Please get conceptId for detected cycles in file:"
          + outputFile);
      return true;
    }
    LOG.info("*******NO CYCLE DETECTED***** - Concepts reviewed: "
        + reviewed);
    return false;
  }

  /**
   * Find cycle.
   *
   * @param con the con
   * @param desc the desc
   * @throws ClassificationException the classification exception
   */
  private void findCycle(Long con, List<Long> desc)
    throws ClassificationException {
    List<Long> parents = isarelationships.get(con);
    if (parents != null) {
      desc.add(con);
      for (Long parent : parents) {
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
  public Map<Long, Boolean> getConcepts() {
    return concepts;
  }

  /**
   * @param concepts the concepts to set
   */
  public void setConcepts(Map<Long, Boolean> concepts) {
    this.concepts = concepts;
  }

  /**
   * @return the isarelationships
   */
  public Map<Long, List<Long>> getIsarelationships() {
    return isarelationships;
  }

  /**
   * @param isarelationships the isarelationships to set
   */
  public void setIsarelationships(Map<Long, List<Long>> isarelationships) {
    this.isarelationships = isarelationships;
  }

  /**
   * @return the conceptInLoop
   */
  public Set<Long> getConceptInLoop() {
    return conceptInLoop;
  }

  /**
   * @param conceptInLoop the conceptInLoop to set
   */
  public void setConceptInLoop(HashSet<Long> conceptInLoop) {
    this.conceptInLoop = conceptInLoop;
  }
}
