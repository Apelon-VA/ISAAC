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

/**
 * The Class Concept. Represents a concept.
 */
public class Concept implements Comparable<Object> {

  /** The id. */
  public int id;

  /** The is defined. */
  public boolean isDefined;

  /**
   * Instantiates a new concept.
   */
  public Concept() {
    id = Integer.MIN_VALUE;
    isDefined = false;
  }

  /**
   * Instantiates a new concept.
   *
   * @param id the id
   * @param isDefined true if the concept is Fully Defined, false if primitive
   */
  public Concept(int id, boolean isDefined) {
    this.id = id;
    this.isDefined = isDefined;
  }

  /**
   * Sets the id.
   *
   * @param id the id
   */
  public void SetId(int id) {
    this.id = id;
  }

  /**
   * Sets the is defined.
   *
   * @param defined the defined
   */
  public void SetIsDefined(boolean defined) {
    this.isDefined = defined;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Object object) {
    Concept other = (Concept) object;
    if (this.id > other.id) {
      return 1; // this is greater than received
    } else if (this.id < other.id) {
      return -1; // this is less than received
    } else {
      return 0; // this == received
    }
  }
}
