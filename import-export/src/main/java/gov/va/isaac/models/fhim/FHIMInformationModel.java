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
package gov.va.isaac.models.fhim;

import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;

import java.util.List;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

import com.google.common.collect.Lists;

public class FHIMInformationModel implements InformationModel {

    public static final class Attribute {
        private final String name;
        /** Foundation metadata concept. */
        private final ConceptSpec type;
        private final String defaultValue;
        public Attribute(String name, ConceptSpec type, String defaultValue) {
            super();
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }
        public String getName() {
            return name;
        }
        public ConceptSpec getType() {
            return type;
        }
        public String getDefaultValue() {
            return defaultValue;
        }
    }

    public static abstract class Relationship {

    }

    public static final class Association extends Relationship {
        private final Class source;
        private final Class target;
        public Association(Class source, Class target) {
            super();
            this.source = source;
            this.target = target;
        }
        public Class getSource() {
            return source;
        }
        public Class getTarget() {
            return target;
        }
    }

    public static final class Generalization extends Relationship {
        private final Class general;
        private final Class specific;
        public Generalization(Class general, Class specific) {
            super();
            this.general = general;
            this.specific = specific;
        }
        public Class getGeneral() {
            return general;
        }
        public Class getSpecific() {
            return specific;
        }
    }

    public static final class Dependency extends Relationship {
        private final Class client;
        private final Enumeration supplier;
        public Dependency(Class client, Enumeration supplier) {
            super();
            this.client = client;
            this.supplier = supplier;
        }
        public Class getClient() {
            return client;
        }
        public Enumeration getSupplier() {
            return supplier;
        }
    }

    public static abstract class Type {
        private final String name;
        public Type(String name) {
            super();
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

    public static final class Class extends Type {
        private final List<Attribute> attributes = Lists.newArrayList();
        private final List<Generalization> generalizations = Lists.newArrayList();
        private final List<Association> associations = Lists.newArrayList();
        private final List<Dependency> dependencies = Lists.newArrayList();
        public Class(String name) {
            super(name);
        }
        public void addAttribute(Attribute attribute) {
            attributes.add(attribute);
        }
        public List<Attribute> getAttributes() {
            return attributes;
        }
        public void addGeneralization(Generalization generalization) {
            generalizations.add(generalization);
        }
        public List<Generalization> getGeneralizations() {
            return generalizations;
        }
        public void addAssociation(Association association) {
            associations.add(association);
        }
        public List<Association> getAssociations() {
            return associations;
        }
        public void addDependency(Dependency dependency) {
            dependencies.add(dependency);
        }
        public List<Dependency> getDependencies() {
            return dependencies;
        }

    }

    public static final class Enumeration extends Type {
        /** Foundation metadata concept. */
        private final ConceptSpec type;
        private final ConceptSpec[] members;
        public Enumeration(String name, ConceptSpec type, ConceptSpec[] members) {
            super(name);
            this.type = type;
            this.members = members;
        }
        public ConceptSpec getType() {
            return type;
        }
        public ConceptSpec[] getMembers() {
            return members;
        }
    }

    private final String name;
    private final Metadata metadata;
    private final List<Type> types = Lists.newArrayList();

    public FHIMInformationModel(String name,
            Metadata metadata) {
        super();
        this.name = name;
        this.metadata = metadata;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public InformationModelType getType() {
        return InformationModelType.FHIM;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    public List<Type> getTypes() {
        return types;
    }

    public void addType(Type type) {
        types.add(type);
    }
}
