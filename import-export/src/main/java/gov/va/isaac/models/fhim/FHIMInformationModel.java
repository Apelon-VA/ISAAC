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
import gov.va.isaac.models.util.DefaultInformationModel;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.VisibilityKind;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class FHIMInformationModel extends DefaultInformationModel implements InformationModel {

    /**
     * For unlimited values, see {@link LiteralUnlimitedNatural}
     */
    public static final class Multiplicity {
        private final int lower;
        private final int upper;
        public Multiplicity(int lower, int upper) {
            super();
            this.lower = lower;
            this.upper = upper;
        }
        public int getLower() {
            return lower;
        }
        public int getUpper() {
            return upper;
        }
        @Override
        public String toString() {
            if (lower == upper) {
                return Integer.toString(lower);
            } else {
                return String.format("%d..%d", lower, upper);
            }
        }
    }

    public static final class Attribute extends Type {
        private final Type type;
        private String defaultValue;
        private Multiplicity multiplicity;
        private VisibilityKind visibilityKind;
        public Attribute(String name, Type type) {
            super(name);
            this.type = type;
        }
        public Type getType() {
            return type;
        }
        public String getDefaultValue() {
            return defaultValue;
        }
        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
        public Multiplicity getMultiplicity() {
            return multiplicity;
        }
        public void setMultiplicity(Multiplicity multiplicity) {
            this.multiplicity = multiplicity;
        }
        public VisibilityKind getVisibility() {
            return visibilityKind;
        }
        public void setVisibility(VisibilityKind visibilityKind) {
            this.visibilityKind = visibilityKind;
        }
    }

    public static final class Association extends Type {
        private final List<Attribute> memberEnds = Lists.newArrayList();
        private final Set<Attribute> ownedEnds = Sets.newHashSet();
        public Association(String name) {
            super(name);
        }
        public List<Attribute> getMemberEnds() {
            return memberEnds;
        }
        public void addMemberEnd(Attribute memberEnd) {
            memberEnds.add(memberEnd);
        }
        public void setOwned(Attribute memberEnd, boolean owned) {
            if (owned) {
                ownedEnds.add(memberEnd);
            }
        }
        public boolean isOwned(Attribute memberEnd) {
            return ownedEnds.contains(memberEnd);
        }
    }

    public static final class Generalization {
        private final Type source;
        private final Type target;
        public Generalization(Type source, Type target) {
            this.source = source;
            this.target = target;
        }
        public Type getSource() {
            return source;
        }
        public Type getTarget() {
            return target;
        }
    }

    public static final class Dependency extends Type {
        private final Type client;
        private final Type supplier;
        public Dependency(String name, Type client, Type supplier) {
            super(name);
            this.client = client;
            this.supplier = supplier;
        }
        public Type getClient() {
            return client;
        }
        public Type getSupplier() {
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
        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("name", name)
                    .toString();
        }
    }

    public static final class Class extends Type {
        private final List<Attribute> attributes = Lists.newArrayList();
        private final List<Generalization> generalizations = Lists.newArrayList();
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
    }

    public static final class Enumeration extends Type {
        private final List<String> literals = Lists.newArrayList();
        public Enumeration(String name) {
            super(name);
        }
        public List<String> getLiterals() {
            return literals;
        }
        public void addLiteral(String literal) {
            literals.add(literal);
        }
    }

    public static final class External extends Type {
        private final ConceptSpec conceptSpec;
        public External(String name, ConceptSpec conceptSpec) {
            super(name);
            this.conceptSpec = conceptSpec;
        }
        public ConceptSpec getConceptSpec() {
            return conceptSpec;
        }
    }

    private final List<Enumeration> enumerations = Lists.newArrayList();
    private final List<Class> classes = Lists.newArrayList();
    private final List<Dependency> dependencies = Lists.newArrayList();
    private final List<Association> associations = Lists.newArrayList();

    public FHIMInformationModel(InformationModel model) {
      super(model);
  }

    public FHIMInformationModel(String name, UUID uuid) {
      super(name, null, uuid, InformationModelType.FHIM);
  }

    public List<Enumeration> getEnumerations() {
        return enumerations;
    }

    public void addEnumeration(Enumeration enumeration) {
        enumerations.add(enumeration);
    }

    public List<Class> getClasses() {
        return classes;
    }

    public void addClass(Class clazz) {
        classes.add(clazz);
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void addDependency(Dependency dependency) {
        dependencies.add(dependency);
    }

    public List<Association> getAssociations() {
        return associations;
    }

    public void addAssociation(Association association) {
        associations.add(association);
    }
}
