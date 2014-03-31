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

import static gov.va.isaac.models.cem.CEMInformationModel.ComponentType.ATT;
import static gov.va.isaac.models.cem.CEMInformationModel.ComponentType.MOD;
import static gov.va.isaac.models.cem.CEMInformationModel.ComponentType.QUAL;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.util.AbstractInformationModel;

import java.util.List;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

import com.google.common.collect.Lists;

/**
 * A concrete {@link InformationModel} for displaying CEM models.
 *
 * @author ocarlsen
 */
public class CEMInformationModel extends AbstractInformationModel implements InformationModel {

    public static final class Constraint {
        private final String path;
        private final String value;
        public Constraint(String path, String value) {
            super();
            this.path = path;
            this.value = value;
        }
        public String getPath() {
            return path;
        }
        public String getValue() {
            return value;
        }
    }

    public static enum ComponentType {
        QUAL, MOD, ATT;
    }

    public static final class Composition {
        private final ComponentType componentType;
        private final String component;
        private Constraint constraint;
        private String value;
        private Composition(ComponentType componentType, String component) {
            super();
            this.componentType = componentType;
            this.component = component;
        }
        public ComponentType getComponentType() {
            return componentType;
        }
        public String getComponent() {
            return component;
        }
        public Constraint getConstraint() {
            return constraint;
        }
        public void setConstraint(Constraint constraint) {
            this.constraint = constraint;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }

    private final List<Composition> qualComponents = Lists.newArrayList();
    private final List<Composition> modComponents = Lists.newArrayList();
    private final List<Composition> attComponents = Lists.newArrayList();
    private final List<Constraint> constraints = Lists.newArrayList();

    private String key;
    private ConceptSpec dataType;

    public CEMInformationModel(String name) {
        super(name, null, InformationModelType.CEM);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ConceptSpec getDataType() {
        return dataType;
    }

    public void setDataType(ConceptSpec dataType) {
        this.dataType = dataType;
    }

    public Composition addQualComponent(String component) {
        Composition qualComponent = new Composition(QUAL, component);
        qualComponents.add(qualComponent);
        return qualComponent;
    }

    public List<Composition> getQualComponents() {
        return qualComponents;
    }

    public Composition addModComponent(String component) {
        Composition modComponent = new Composition(MOD, component);
        modComponents.add(modComponent);
        return modComponent;
    }

    public List<Composition> getModComponents() {
        return modComponents;
    }

    public Composition addAttComponent(String component) {
        Composition attComponent = new Composition(ATT, component);
        attComponents.add(attComponent);
        return attComponent;
    }

    public List<Composition> getAttComponents() {
        return attComponents;
    }

    public void addConstraint(Constraint constraint) {
        constraints.add(constraint);
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }
}
