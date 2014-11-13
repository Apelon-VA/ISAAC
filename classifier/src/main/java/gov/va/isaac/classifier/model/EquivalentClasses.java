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
package gov.va.isaac.classifier.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class EquivalentClasses.
 * 
 * This class is responsible to acumulate equivalent concepts.
 */
public class EquivalentClasses extends ArrayList<ConceptGroup> {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new equiv concept.
   */
  public EquivalentClasses() {
    super();
  }

  /**
   * Counts total concepts in EquivalentClasses.
   *
   * @return <code><b>int</b></code> - total concepts
   */
  public int count() {
    int count = 0;
    int max = this.size();
    for (int i = 0; i < max; i++) {
      count += this.get(i).size();
    }
    return count;
  }

}
