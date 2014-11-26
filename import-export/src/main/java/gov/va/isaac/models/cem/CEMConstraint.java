
package gov.va.isaac.models.cem;

import gov.va.isaac.AppContext;
import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.constants.InformationModels;
import gov.va.isaac.util.WBUtility;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a CEM constraint.
 */
public class CEMConstraint {
  private static final Logger LOGGER = LoggerFactory.getLogger(WBUtility.class);

  /**  The path. */
  private String path;

  /**  The value. */
  private String value;

  /**
   * Instantiates a {@link CEMConstraint}.
   */
  public CEMConstraint() {
    // do nothing
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
   * Sets the path.
   *
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
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
   * Returns the ceml tag name.
   *
   * @return the ceml tag name
   */
  public static String getCemlTagName() {
    return "constraint";
  }
  
  /**
   * Sets the value.
   *
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CEMConstraint other = (CEMConstraint) obj;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
  
	public void handleConstraintEnumerationRefex() {
		if (value.endsWith("_VALUESET_CODE")) {
			if (!valueSetExists()) {
				try {
					// Create Enumeration
					AppContext.getRuntimeGlobals().disableAllCommitListeners();
					RefexDynamicUsageDescriptionBuilder.createNewRefexDynamicUsageDescriptionConcept(value, value, "Value Set Sememe for " + value, 
																									 new RefexDynamicColumnInfo[] {},
																									 InformationModels.CEM_ENUMERATIONS.getUuids()[0], false, null);
				} catch (IOException | ContradictionException | InvalidCAB
						| PropertyVetoException e) {
					LOGGER.error("Unable to create CEM enumeration for " + value);
				}
				finally
				{
					AppContext.getRuntimeGlobals().enableAllCommitListeners();
				}
			}
		}
	}
	
	private boolean valueSetExists() {
			// Get UUID
			UUID uuid = WBUtility.getUuidForFsn(value, value);
//			UUID uuid = UuidT5Generator.get(UUID PATH_ID_FROM_FS_DESC, value);
	
			return ExtendedAppContext.getDataStore().hasUuid(uuid);
	}
}
