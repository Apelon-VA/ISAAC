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
package gov.va.isaac.models.cem;

import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.util.DefaultInformationModel;

import java.util.List;

/**
 * An {@link InformationModel} for allowing applications to interact more
 * naturally with CEM models.
 *
 * @author ocarlsen
 * @author bcarslen
 */
@SuppressWarnings("static-method")
public class CEMInformationModel extends DefaultInformationModel {
  
  /**
   * Instantiates an empty {@link CEMInformationModel}.
   */
  public CEMInformationModel() {
    // do nothing
  }

  /**
   * Adds the qual component.
   *
   * @param component the component
   * @return the composition
   */
  public Composition addQualComponent(String component) {
    // TODO
    return null;
  }

  /**
   * Returns the qual components.
   *
   * @return the qual components
   */
  public List<Composition> getQualComponents() {
    // TODO
    return null;
  }

  /**
   * Adds the mod component.
   *
   * @param component the component
   * @return the composition
   */
  public Composition addModComponent(String component) {
    // TODO
    return null;
  }

  /**
   * Returns the mod components.
   *
   * @return the mod components
   */
  public List<Composition> getModComponents() {
    // TODO
    return null;
  }

  /**
   * Adds the att component.
   *
   * @param component the component
   * @return the composition
   */
  public Composition addAttComponent(String component) {
    // TODO
    return null;
  }

  /**
   * Returns the att components.
   *
   * @return the att components
   */
  public List<Composition> getAttComponents() {
    // TODO
    return null;
  }

  /**
   * Adds the constraint.
   *
   * @param constraint the constraint
   * @return the constraint
   */
  public Constraint addConstraint(Constraint constraint) {
    // TODO
    return null;
  }

  /**
   * Returns the constraints.
   *
   * @return the constraints
   */
  public List<Constraint> getConstraints() {
    // TODO
    return null;
  }

  //
  // INNER CLASSES
  //

  /**
   * Represents a CEM "constraint"
   */
  public static final class Constraint {

    /** The path. */
    private final String path;

    /** The value. */
    private final String value;

    /**
     * Instantiates a {@link Constraint} from the specified parameters.
     *
     * @param path the path
     * @param value the value
     */
    public Constraint(String path, String value) {
      super();
      this.path = path;
      this.value = value;
    }

    /**
     * Returns the path.
     *
     * @return the path
     */
    public String getPath() {
      return path;
    }

    /**
     * Returns the value.
     *
     * @return the value
     */
    public String getValue() {
      return value;
    }
  }

  /**
   * The Enum ComponentType.
   *
   * @author ${author}
   */
  public static enum ComponentType {

    /** The qual. */
    QUAL,
    /** The mod. */
    MOD,
    /** The att. */
    ATT;
  }

  /**
   * The Class Composition.
   *
   * @author ${author}
   */
  public static final class Composition {

    /** The component type. */
    private final ComponentType componentType;

    /** The component. */
    private final String component;

    /** The constraint. */
    private Constraint constraint;

    /** The value. */
    private String value;

    /**
     * Instantiates a {@link Composition} from the specified parameters.
     *
     * @param componentType the component type
     * @param component the component
     */
    private Composition(ComponentType componentType, String component) {
      super();
      this.componentType = componentType;
      this.component = component;
    }

    /**
     * Returns the component type.
     *
     * @return the component type
     */
    public ComponentType getComponentType() {
      return componentType;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public String getComponent() {
      return component;
    }

    /**
     * Returns the constraint.
     *
     * @return the constraint
     */
    public Constraint getConstraint() {
      return constraint;
    }

    /**
     * Sets the constraint.
     *
     * @param constraint the constraint
     */
    public void setConstraint(Constraint constraint) {
      this.constraint = constraint;
    }

    /**
     * Returns the value.
     *
     * @return the value
     */
    public String getValue() {
      return value;
    }

    /**
     * Sets the value.
     *
     * @param value the value
     */
    public void setValue(String value) {
      this.value = value;
    }
  }

}
