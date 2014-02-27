package gov.va.isaac.gui.refsetview;

import gov.va.isaac.util.WBUtility;

import java.util.UUID;

import javafx.beans.property.SimpleStringProperty;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;

public class RefsetInstanceAccessor {
	public static class MemberRefsetInstance extends RefsetInstance {
		private MemberRefsetInstance(ConceptVersionBI refCon) {
	    	super(refCon);
		}
	}
	
	public static class StrExtRefsetInstance extends RefsetInstance {
	    private SimpleStringProperty strExt;
	 
	    private StrExtRefsetInstance(ConceptVersionBI refCon, RefexVersionBI member) {
	    	super(refCon);
			RefexStringVersionBI ext = (RefexStringVersionBI)member;

			strExt = new SimpleStringProperty(ext.getString1());
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

	    private NidExtRefsetInstance(ConceptVersionBI refCon, RefexVersionBI member) {
	    	super(refCon);
			RefexNidVersionBI ext = (RefexNidVersionBI)member;
			ConceptVersionBI component = WBUtility.lookupSnomedIdentifierAsCV(ext.getNid1());
			
	        this.cidExtUuid = new SimpleStringProperty(component.getPrimordialUuid().toString());
	        try {
				this.cidExtFsn = new SimpleStringProperty(component.getPreferredDescription().getText());
			} catch (Exception e) {
		        this.cidExtFsn = new SimpleStringProperty("Bad Concept");
				e.printStackTrace();
			}
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
	    	this.cidExtFsn.set("FSN: " + fsn.toString());
	    }
	}
	
	public static class NidNidExtRefsetInstance extends NidExtRefsetInstance {
	    private SimpleStringProperty cid2ExtFsn;
	    private SimpleStringProperty cid2ExtUuid;

	    private NidNidExtRefsetInstance(ConceptVersionBI refCon, RefexVersionBI member) {
	    	super(refCon, member);

			RefexNidNidVersionBI ext = (RefexNidNidVersionBI)member;
			ConceptVersionBI component2 = WBUtility.lookupSnomedIdentifierAsCV(ext.getNid2());
			
	        this.cid2ExtUuid = new SimpleStringProperty(component2.getPrimordialUuid().toString());
	        try {
				this.cid2ExtFsn = new SimpleStringProperty(component2.getPreferredDescription().getText());
			} catch (Exception e) {
		        this.cid2ExtFsn = new SimpleStringProperty("Bad Concept");
				e.printStackTrace();
			}
		}

	    public String getCid2ExtUuid() {
	        return cid2ExtUuid.get();
	    }
	    
	    public void setCid2ExtUuid(UUID uuid) {
	    	this.cid2ExtUuid.set(uuid.toString());
	    }
	        
	    public String getCid2ExtFsn() {
	        return cid2ExtFsn.get();
	    }
	    
	    public void setCid2ExtFsn(String fsn) {
	    	this.cid2ExtFsn.set("FSN: " + fsn.toString());
	    }
	}
	
	public static class RefsetInstance {
	    private SimpleStringProperty refConFsn;
	    private SimpleStringProperty refConUuid;
	 
	    private RefsetInstance(ConceptVersionBI con) {
	        this.refConUuid = new SimpleStringProperty(con.getPrimordialUuid().toString());
	        try {
				this.refConFsn = new SimpleStringProperty(con.getPreferredDescription().getText());
			} catch (Exception e) {
		        this.refConFsn = new SimpleStringProperty("Bad Concept");
				e.printStackTrace();
			}
		}

		public String getRefConUuid() {
	        return refConUuid.get();
	    }
		
	    public void setRefConUuid(UUID uuid) {
	    	this.refConUuid.set(uuid.toString());
	    }
	        
	    public String getRefConFsn() {
	        return refConFsn.get();
	    }
	    
	    public void setRefConFsn(UUID fsn) {
	    	this.refConFsn.set("FSN: " + fsn.toString());
	    }	    
	}

	public static RefsetInstance getInstance(ConceptVersionBI refCon, RefexVersionBI member) {
		if (member.getRefexType() == RefexType.MEMBER) {
			return new MemberRefsetInstance(refCon);
		} else if (member.getRefexType() == RefexType.STR) {
			return new StrExtRefsetInstance(refCon, member);
		} else if (member.getRefexType() == RefexType.CID) {
			return new NidExtRefsetInstance(refCon, member);
		} else if (member.getRefexType() == RefexType.CID_CID) {
			return new NidNidExtRefsetInstance(refCon, member);
		}
		
		return null;
	}
}
