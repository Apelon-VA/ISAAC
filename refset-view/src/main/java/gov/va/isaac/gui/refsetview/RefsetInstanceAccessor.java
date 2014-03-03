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
package gov.va.isaac.gui.refsetview;

import gov.va.isaac.AppContext;
import gov.va.isaac.models.cem.importer.CEMMetadataBinding;
import gov.va.isaac.util.WBUtility;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import javafx.beans.property.SimpleStringProperty;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RefsetInstanceAccessor
 *
 * @author <a href="jefron@apelon.com">Jesse Efron</a>
 */
public class RefsetInstanceAccessor {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat();
	private static final Logger logger = LoggerFactory.getLogger(RefsetInstanceAccessor.class);

	public static class RefsetInstance {
		private SimpleStringProperty refCompConFsn;
		private SimpleStringProperty refCompConUuid;
		private int refCompConNid;
		private int memberNid;
		
		private String status, time, author, module, path;

		private RefsetInstance(ConceptVersionBI con, int nid) {
			memberNid = nid;

			if (con == null) {
				this.refCompConUuid = new SimpleStringProperty(
						"Add Reference Component UUID");
				this.refCompConFsn = new SimpleStringProperty(
						"Add Reference Component UUID");
				this.refCompConNid = 0;
			} else {
				this.refCompConUuid = new SimpleStringProperty(con
						.getPrimordialUuid().toString());
				this.refCompConNid = con.getNid();
				this.status = con.getStatus().toString();
				this.time = sdf.format(new Date(con.getTime()));
				try
				{
					this.author = WBUtility.getDescription(AppContext.getService(TerminologyStoreDI.class).getConcept(con.getAuthorNid()).getPrimordialUuid());
				}
				catch (Exception e1)
				{
					logger.error("Error formatting stamp component", e1);
					this.author = con.getAuthorNid() + "";
				}
				try
				{
					this.module = WBUtility.getDescription(AppContext.getService(TerminologyStoreDI.class).getConcept(con.getModuleNid()).getPrimordialUuid());
				}
				catch (Exception e1)
				{
					logger.error("Error formatting stamp component", e1);
					this.module = con.getModuleNid() + "";
				}
				try
				{
					this.path = WBUtility.getDescription(AppContext.getService(TerminologyStoreDI.class).getConcept(con.getPathNid()).getPrimordialUuid());
				}
				catch (Exception e1)
				{
					logger.error("Error formatting stamp component", e1);
					this.path = con.getPathNid() + "";
				}
				
				
				try {
					this.refCompConFsn = new SimpleStringProperty(con
							.getPreferredDescription().getText());
				} catch (Exception e) {
					this.refCompConFsn = new SimpleStringProperty("Bad Concept");
					e.printStackTrace();
				}
			}
		}

		public UUID getRefCompConUuid() {
			return UUID.fromString(refCompConUuid.get());
		}

		public void setRefCompConUuid(UUID uuid) {
			this.refCompConUuid.set(uuid.toString());
		}

		public int getRefCompConNid() {
			return refCompConNid;
		}

		public void setRefCompConNid(int nid) {
			this.refCompConNid = nid;
		}

		public String getRefCompConFsn() {
			return refCompConFsn.get();
		}

		public void setRefCompConFsn(String fsn) {
			this.refCompConFsn.set(fsn);
		}

		public int getMemberNid() {
			return memberNid;
		}

		public void setMemberNid(int nid) {
			memberNid = nid;
		}

		/**
		 * @return the status
		 */
		public String getStatus()
		{
			return status;
		}

		/**
		 * @return the time
		 */
		public String getTime()
		{
			return time;
		}

		/**
		 * @return the author
		 */
		public String getAuthor()
		{
			return author;
		}

		/**
		 * @return the module
		 */
		public String getModule()
		{
			return module;
		}

		/**
		 * @return the path
		 */
		public String getPath()
		{
			return path;
		}
	}

	public static class MemberRefsetInstance extends RefsetInstance {
		private MemberRefsetInstance(ConceptVersionBI refCompCon,
				RefexVersionBI member) {
			super(refCompCon, member.getNid());
		}

		public MemberRefsetInstance() {
			super(null, 0);
		}
	}

	public static class StrExtRefsetInstance extends RefsetInstance {
		private SimpleStringProperty strExt;

		private StrExtRefsetInstance(ConceptVersionBI refCompCon, RefexVersionBI member) {
			super(refCompCon, member.getNid());
			RefexStringVersionBI ext = (RefexStringVersionBI) member;

			strExt = new SimpleStringProperty(ext.getString1());
		}

		public StrExtRefsetInstance() {
			super(null, 0);
			strExt = new SimpleStringProperty("Add String Value");
		}

		public String getStrExt() {
			return strExt.get();
		}

		public void setStrExt(String fsn) {
			this.strExt.set(fsn);
		}
	}

	public static class NidExtRefsetInstance extends RefsetInstance {
		private SimpleStringProperty cidExtFsn;
		private SimpleStringProperty cidExtUuid;

		private NidExtRefsetInstance(ConceptVersionBI refCompCon,
				RefexVersionBI member) {
			super(refCompCon, member.getNid());
			RefexNidVersionBI ext = (RefexNidVersionBI) member;
			ConceptVersionBI component = WBUtility
					.lookupSnomedIdentifierAsCV(ext.getNid1());

			this.cidExtUuid = new SimpleStringProperty(component
					.getPrimordialUuid().toString());
			try {
				this.cidExtFsn = new SimpleStringProperty(component
						.getPreferredDescription().getText());
			} catch (Exception e) {
				this.cidExtFsn = new SimpleStringProperty("Bad Concept");
				e.printStackTrace();
			}
		}

		public NidExtRefsetInstance() {
			super(null, 0);
			this.cidExtUuid = new SimpleStringProperty("Add Component UUID");
			this.cidExtFsn = new SimpleStringProperty("Add Component UUID");
		}

		public String getCidExtUuid() {
			return cidExtUuid.get();
		}

		public void setCidExtUuid(UUID uuid) {
			this.cidExtUuid.set(uuid.toString());
		}

		public String getCidExtFsn() {
			return cidExtFsn.get();
		}

		public void setCidExtFsn(String fsn) {
			this.cidExtFsn.set(fsn.toString());
		}
	}

	public static class NidStrExtRefsetInstance extends NidExtRefsetInstance  {
		private SimpleStringProperty strExt;

		private NidStrExtRefsetInstance(ConceptVersionBI refCompCon, RefexVersionBI member) {
			super(refCompCon, member);

			RefexNidStringVersionBI ext = (RefexNidStringVersionBI)member;
			this.strExt = new SimpleStringProperty(ext.getString1());
		}


		public NidStrExtRefsetInstance() {
			super();
			strExt = new SimpleStringProperty("Add String Value");
		}

		public String getStrExt() {
			return strExt.get();
		}
	
		public void setStrExt(String fsn) {
			this.strExt.set(fsn);
		}
	}

	public static class CEMCompositRefestInstance extends NidStrExtRefsetInstance {
		private SimpleStringProperty constraintPathExt;
		private SimpleStringProperty constraintValExt;
		private SimpleStringProperty valueExt = new SimpleStringProperty("");
		private int constraintMemberNid;
		private int constraintValMemberNid;
		private int constraintPathMemberNid;
		private int valMemberNid;
		private ComponentChronicleBI constraintRefsetCon = null;

		private CEMCompositRefestInstance(ConceptVersionBI refCompCon, RefexVersionBI member) {
			super(refCompCon, member);

			try {
//				setupMemberNids();
				
				Collection<? extends RefexVersionBI<?>> parentAnnots = member.getAnnotationsActive(WBUtility.getViewCoordinate());
				for (RefexVersionBI parentAnnot : parentAnnots) {
					if (parentAnnot.getAssemblageNid() == CEMMetadataBinding.CEM_CONSTRAINTS_REFSET.getNid()) {
						constraintMemberNid = parentAnnot.getNid();
						
						Collection<? extends RefexVersionBI<?>> refAnnots = parentAnnot.getAnnotationsActive(WBUtility.getViewCoordinate());
						for (RefexVersionBI annot : refAnnots) {
							if (annot.getAssemblageNid() == CEMMetadataBinding.CEM_CONSTRAINTS_PATH_REFSET.getNid()) {
								constraintPathExt = new SimpleStringProperty(((RefexStringVersionBI)annot).getString1());
							} else if (annot.getAssemblageNid() == CEMMetadataBinding.CEM_CONSTRAINTS_VALUE_REFSET.getNid()) {
								constraintValExt = new SimpleStringProperty(((RefexStringVersionBI)annot).getString1());
							} else if (annot.getAssemblageNid() == CEMMetadataBinding.CEM_VALUE_REFSET.getNid()) {
								valueExt = new SimpleStringProperty(((RefexStringVersionBI)annot).getString1());
							} 
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
//		private void setupMemberNids() {
//			try {
//				constraintPathMemberNid = CEMMetadataBinding.CEM_CONSTRAINTS_PATH_REFSET.getNid();
//				constraintValMemberNid = CEMMetadataBinding.CEM_CONSTRAINTS_VALUE_REFSET.getNid();
//				valMemberNid = CEMMetadataBinding.CEM_VALUE_REFSET.getNid();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
		public CEMCompositRefestInstance() {
			super();
//			setupMemberNids();
			
			constraintPathExt = new SimpleStringProperty("Add String Value");
			constraintValExt = new SimpleStringProperty("Add String Value");
			valueExt = new SimpleStringProperty("Add String Value");
		}

		public String getConstraintPathExt() {
			return constraintPathExt.get();
		}
	
		public void setConstraintPathExt(String val) {
			this.constraintPathExt.set(val);
		}

		public String getConstraintValExt() {
			return constraintValExt.get();
		}
	
		public void setConstraintValExt(String val) {
			this.constraintValExt.set(val);
		}

		public String getValueExt() {
			return valueExt.get();
		}
	
		public void setValueExt(String val) {
			this.valueExt.set(val);
		}

		public int getConstraintMemberNid() {
			// TODO Auto-generated method stub
			return constraintMemberNid;
		}
		public void setConstraintMemberNid(int val) {
			constraintMemberNid = val;
		}

		public int getConstraintValMemberNid() {
			// TODO Auto-generated method stub
			return constraintValMemberNid;
		}
		public void setConstraintValMemberNid(int val) {
			constraintValMemberNid = val;
		}

		public int getConstraintPathMemberNid() {
			// TODO Auto-generated method stub
			return constraintPathMemberNid;
		}
		public void setConstraintPathMemberNid(int val) {
			constraintPathMemberNid = val;
		}

		public int getValueMemberNid() {
			// TODO Auto-generated method stub
			return valMemberNid;
		}
		public void setValueMemberNid(int val) {
			valMemberNid = val;
		}
	}

	public static RefsetInstance getInstance(ConceptVersionBI refCompCon,
			RefexVersionBI member, RefexType refsetType) {
		if (refsetType == RefexType.MEMBER) {
			return new MemberRefsetInstance(refCompCon, member);
		} else if (refsetType == RefexType.STR) {
			return new StrExtRefsetInstance(refCompCon, member);
		} else if (refsetType == RefexType.CID) {
			return new NidExtRefsetInstance(refCompCon, member);
		} else if (refsetType == RefexType.CID_STR) {
			return new NidStrExtRefsetInstance(refCompCon, member);
		} else if (refsetType == RefexType.UNKNOWN) {
			return new CEMCompositRefestInstance(refCompCon, member);
		}

		return null;
	}

	public static RefsetInstance createNewInstance(RefexType refsetType) {
		if (refsetType == RefexType.MEMBER) {
			return new MemberRefsetInstance();
		} else if (refsetType == RefexType.STR) {
			return new StrExtRefsetInstance();
		} else if (refsetType == RefexType.CID) {
			return new NidExtRefsetInstance();
		} else if (refsetType == RefexType.CID_STR) {
			return new NidStrExtRefsetInstance();
		}

		return null;
	}
}
