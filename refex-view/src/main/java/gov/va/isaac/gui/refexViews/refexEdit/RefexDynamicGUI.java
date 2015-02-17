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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.refexViews.refexEdit;

import gov.va.isaac.ExtendedAppContext;
import gov.va.isaac.util.AlphanumComparator;
import gov.va.isaac.util.NumberUtilities;
import gov.va.isaac.util.OTFUtility;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.function.ToIntFunction;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.media.MediaVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicArrayBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicByteArrayBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicDoubleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicFloatBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicIntegerBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicLongBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicNidBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RefexDynamicGUI}
 * 
 * A Wrapper for a RefexDynamicVersionBI - because the versioned refex provides no information
 * about whether or not it is an old version, or if it is the latest version.  Add a flag for 
 * is latest.
 * 
 * Also used in cases where we are constructing a new Refex - up front, we know a NID (which is either the assemblyNid or 
 * the referenced component nid.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class RefexDynamicGUI
{
	private static Logger logger_ = LoggerFactory.getLogger(RefexDynamicGUI.class);
	
	//These variables are used when we are working with a refex that already exists
	private RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>> refex_;
	private boolean isCurrent_;
	private HashMap<String, AbstractMap.SimpleImmutableEntry<String, String>> stringCache_ = new HashMap<>();
	
	//These variables are used when we are creating a new refex which doesn't yet exist.
	private Integer buildFromReferenceNid_;
	private boolean referenceIsAssemblyNid_;
	
	protected RefexDynamicGUI(RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>> refex, boolean isCurrent)
	{
		refex_ = refex;
		isCurrent_ = isCurrent;
	}
	
	protected RefexDynamicGUI(int buildFromReferenceNid, boolean referenceIsAssemblyNid)
	{
		refex_ = null;
		isCurrent_ = false;
		buildFromReferenceNid_ = buildFromReferenceNid;
		referenceIsAssemblyNid_ = referenceIsAssemblyNid;
	}

	/**
	 * Contains the refex reference when this object was constructed based on an existing refex
	 */
	public RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>> getRefex()
	{
		return refex_;
	}

	/**
	 * If this was constructed based off of an existing refex, is this the most current refex?  Or a historical one?
	 * This is meaningless if {@link #getRefex()} return null.
	 */
	public boolean isCurrent()
	{
		return isCurrent_;
	}

	/**
	 * If this was constructed with just a nid (building a new refex from scratch) this returns it - otherwise, returns null.
	 */
	public Integer getBuildFromReferenceNid()
	{
		return buildFromReferenceNid_;
	}

	/**
	 * If this was constructed with just a nid - this returns true of the nid is pointing to an assemblage concept - false if it is
	 * pointing to a component reference.  The value is meaningless if {@link #getBuildFromReferenceNid()} returns null.
	 */
	public boolean getReferenceIsAssemblyNid()
	{
		return referenceIsAssemblyNid_;
	}
	
	
	/**
	 * For cases when it was built from an existing refex only
	 * @param attachedDataColumn - optional - ignored (can be null) except applicable to {@link DynamicRefexColumnType#ATTACHED_DATA}
	 */
	public int compareTo(DynamicRefexColumnType columnTypeToCompare, Integer attachedDataColumn, RefexDynamicGUI other)
	{
		try
		{
			switch (columnTypeToCompare)
			{
				case STATUS_CONDENSED:
				{
					//sort by uncommitted first, then current / historical, then active / inactive
					if (this.getRefex().getTime() == Long.MAX_VALUE)
					{
						return -1;
					}
					else if (other.getRefex().getTime() == Long.MAX_VALUE)
					{
						return 1;
					}
					
					if (this.isCurrent() && !other.isCurrent())
					{
						return -1;
					}
					else if (!this.isCurrent() && other.isCurrent())
					{
						return 1;
					}
					
					if (this.getRefex().isActive() && !other.getRefex().isActive())
					{
						return -1;
					}
					else if (!this.getRefex().isActive() && other.getRefex().isActive())
					{
						return 1;
					}
					return 0;
				}
				case TIME:
				{
					if (this.getRefex().getTime() < other.getRefex().getTime())
					{
						return -1;
					}
					else if (this.getRefex().getTime() > other.getRefex().getTime())
					{
						return -1;
					}
					else
					{
						return 0;
					}
				}
				case COMPONENT: case ASSEMBLAGE: case STATUS_STRING: case AUTHOR: case MODULE: case PATH:
				{
					String myString = this.getDisplayStrings(columnTypeToCompare, null).getKey();
					String otherString = other.getDisplayStrings(columnTypeToCompare, null).getKey();
					return AlphanumComparator.compare(myString, otherString, true);
				}
				case ATTACHED_DATA:
				{
					if (attachedDataColumn == null)
					{
						throw new RuntimeException("API misuse");
					}
					RefexDynamicDataBI myData = this.refex_.getData().length > attachedDataColumn ? this.refex_.getData()[attachedDataColumn] : null;
					RefexDynamicDataBI otherData = other.refex_.getData().length > attachedDataColumn ? other.refex_.getData()[attachedDataColumn] : null;
					
					if (myData == null && otherData != null)
					{
						return -1;
					}
					else if (myData != null && otherData == null)
					{
						return 1;
					}
					else if (myData == null && otherData == null)
					{
						return 0;
					}
					else if (myData instanceof RefexDynamicFloatBI && otherData instanceof RefexDynamicFloatBI)
					{
						return NumberUtilities.compare(((RefexDynamicFloatBI) myData).getDataFloat(), ((RefexDynamicFloatBI) otherData).getDataFloat());
					}
					else if (myData instanceof RefexDynamicDoubleBI && otherData instanceof RefexDynamicDoubleBI) 
					{
						return NumberUtilities.compare(((RefexDynamicDoubleBI) myData).getDataDouble(), ((RefexDynamicDoubleBI) otherData).getDataDouble());
					}
					else if (myData instanceof RefexDynamicIntegerBI && otherData instanceof RefexDynamicIntegerBI) 
					{
						return NumberUtilities.compare(((RefexDynamicIntegerBI) myData).getDataInteger(), ((RefexDynamicIntegerBI) otherData).getDataInteger());
					}
					else if (myData instanceof RefexDynamicLongBI && otherData instanceof RefexDynamicLongBI)
					{
						return NumberUtilities.compare(((RefexDynamicLongBI) myData).getDataLong(), ((RefexDynamicLongBI) otherData).getDataLong());
					}
					else
					{
						String myString = this.getDisplayStrings(columnTypeToCompare, attachedDataColumn).getKey();
						String otherString = other.getDisplayStrings(columnTypeToCompare, attachedDataColumn).getKey();
						return AlphanumComparator.compare(myString, otherString, true);
					}
				}

				default:
					throw new RuntimeException("Missing implementation: " + columnTypeToCompare);
			}
		}
		catch (IOException e)
		{
			//TODO (artf231428) how to handle?
			throw new RuntimeException("Sort error", e);
		}
	}
	
	/**
	 * Returns the string for display, and the tooltip, if applicable.  Either / or may be null.
	 * Key is for the display, value is for the tooltip.
	 * @param attachedDataColumn should be null for most types - applicable to {@link DynamicRefexColumnType#ATTACHED_DATA}
	 */
	public AbstractMap.SimpleImmutableEntry<String, String> getDisplayStrings(DynamicRefexColumnType desiredColumn, Integer attachedDataColumn)
	{
		String cacheKey = desiredColumn.name() + attachedDataColumn;  //null is ok on the attachedDataColumn...
		
		AbstractMap.SimpleImmutableEntry<String, String> returnValue = stringCache_.get(cacheKey);
		if (returnValue != null)
		{
			return returnValue;
		}
		
		
		switch (desiredColumn)
		{
			case STATUS_CONDENSED:
			{
				//Just easier to leave the impl in StatusCell for this one.  We don't need filters on this column either.
				throw new RuntimeException("No text for this field");
			}
			case COMPONENT: case ASSEMBLAGE: case AUTHOR: case PATH: case MODULE:
			{
				String text = getComponentText(getNidFetcher(desiredColumn, attachedDataColumn));
				returnValue = new AbstractMap.SimpleImmutableEntry<String, String>(text, text);
				break;
			}
			case STATUS_STRING:
			{
				returnValue = new AbstractMap.SimpleImmutableEntry<String, String>(refex_.getStatus().toString(), null);
				break;
			}
			case TIME:
			{
				returnValue = new AbstractMap.SimpleImmutableEntry<String, String>((refex_.getTime() == Long.MAX_VALUE ? "-Uncommitted-" : 
					new Date(refex_.getTime()).toString()), null);
				break;
			}
			case ATTACHED_DATA:
			{
				if (attachedDataColumn == null)
				{
					throw new RuntimeException("API misuse");
				}
				RefexDynamicDataBI data = this.refex_.getData().length > attachedDataColumn ? this.refex_.getData()[attachedDataColumn] : null;
				if (data != null)
				{
					if (data instanceof RefexDynamicByteArrayBI)
					{
						returnValue = new AbstractMap.SimpleImmutableEntry<String, String>("[Binary]", null);
					}
					else if (data instanceof RefexDynamicNidBI)
					{
						String desc = getComponentText(((RefexDynamicNidBI)data).getDataNid());
						returnValue = new AbstractMap.SimpleImmutableEntry<String, String>(desc, data.getDataObject().toString());
					}
					else if (data instanceof RefexDynamicUUIDBI)
					{
						String desc;
						if (ExtendedAppContext.getDataStore().hasUuid(((RefexDynamicUUIDBI)data).getDataUUID()))
						{
							desc = getComponentText(ExtendedAppContext.getDataStore().getNidForUuids(((RefexDynamicUUIDBI)data).getDataUUID()));
						}
						else
						{
							desc = ((RefexDynamicUUIDBI)data).getDataUUID() + "";
						}
						returnValue = new AbstractMap.SimpleImmutableEntry<String, String>(desc, data.getDataObject().toString());
					}
					else if (data instanceof RefexDynamicArrayBI<?>)
					{
						RefexDynamicArrayBI<?> instanceData = (RefexDynamicArrayBI<?>)data;
						switch (instanceData.getArrayDataType())
						{
							case ARRAY:
								//Could recurse... but I can't imagine a use case at the moment.
								returnValue = new AbstractMap.SimpleImmutableEntry<String, String>("[" + instanceData.getDataArray().length + " nested arrays]",
										"An array of nested arrays");
								break;
							case STRING: case BOOLEAN: case DOUBLE: case FLOAT: case INTEGER: case LONG: case NID: case UUID:
							{
								//NID and UUID could be turned into strings... but, unusual use case... leave like this for now.
								StringBuilder sb = new StringBuilder();
								sb.append("[");
								for (RefexDynamicDataBI d : instanceData.getDataArray())
								{
									sb.append(d.getDataObject().toString());
									sb.append(", ");
								}
								if (sb.length() > 1)
								{
									sb.setLength(sb.length() - 2);
								}
								sb.append("]");
								returnValue = new AbstractMap.SimpleImmutableEntry<String, String>(sb.toString(), "Array of " + instanceData.getDataArray().length + " items: " + sb.toString());
								break;
							}
							
							case BYTEARRAY:
								returnValue = new AbstractMap.SimpleImmutableEntry<String, String>("[" + instanceData.getDataArray().length + " Binary items]",
										"An array of binary objects");
								break;
							case UNKNOWN: case POLYMORPHIC:
							{
								//shouldn't happen - but just do the toString
								returnValue = new AbstractMap.SimpleImmutableEntry<String, String>("[" + instanceData.getDataArray().length + " items]",
										"An array of unknown data elements");
								break;
							}
							default:
								logger_.error("Unhandled case: {}, {}", instanceData, Arrays.toString(instanceData.getDataArray()));
								returnValue = new AbstractMap.SimpleImmutableEntry<String, String>("-ERROR-", "Internal error computing value");
								break;
						}
					}
					else
					{
						returnValue = new AbstractMap.SimpleImmutableEntry<String, String>(data.getDataObject().toString(), data.getDataObject().toString());
					}
				}
				else
				{
					returnValue = new AbstractMap.SimpleImmutableEntry<String, String>("", null);
				}
				break;
			}

			default:
				throw new RuntimeException("Missing implementation: " + desiredColumn);
		}
		
		stringCache_.put(cacheKey, returnValue);
		return returnValue;
		
	}
	
	private String getComponentText(ToIntFunction<RefexDynamicVersionBI<?>> nidFetcher)
	{
		return getComponentText(nidFetcher.applyAsInt(this.refex_));
	}
	
	private String getComponentText(int nid)
	{
		String text;
		
		try
		{
			ConceptVersionBI c = OTFUtility.getConceptVersion(nid);
			if (c == null) 
			{
				//This may be a different component - like a description, or another refex... need to handle.
				ComponentVersionBI cv = ExtendedAppContext.getDataStore().getComponentVersion(OTFUtility.getViewCoordinate(), nid);
				if (cv == null)
				{
					text = "[NID] " + nid;
				}
				
				else if (cv instanceof DescriptionVersionBI<?>)
				{
					DescriptionVersionBI<?> dv = (DescriptionVersionBI<?>) cv;
					text = "Description: " + dv.getText();
				}
				else if (cv instanceof RelationshipVersionBI<?>)
				{
					RelationshipVersionBI<?> rv = (RelationshipVersionBI<?>) cv;
					text = "Relationship: " + OTFUtility.getDescription(rv.getOriginNid()) + "->" 
							+ OTFUtility.getDescription(rv.getTypeNid()) + "->"
							+ OTFUtility.getDescription(rv.getDestinationNid());
				}
				else if (cv instanceof RefexDynamicVersionBI<?>)
				{
					RefexDynamicVersionBI<?> rdv = (RefexDynamicVersionBI<?>) cv;
					text = "Nested Sememe Dynamic: using assemblage " + OTFUtility.getDescription(rdv.getAssemblageNid());
				}
				else if (cv instanceof RefexVersionBI<?>)
				{
					RefexVersionBI<?> rv = (RefexVersionBI<?>) cv;
					text = "Nested Sememe: using assemblage " + OTFUtility.getDescription(rv.getAssemblageNid());
				}
				else if (cv instanceof MediaVersionBI<?>)
				{
					MediaVersionBI<?> mv = (MediaVersionBI<?>) cv;
					text = "Media of format " + mv.getFormat();
				}
				else
				{
					logger_.warn("The component type " + cv + " is not handled yet!");
					//Not sure what else there may be?
					text = cv.toUserString();
				}
			}
			else
			{
				text = OTFUtility.getDescription(c);
			}
		}
		catch (Exception e)
		{
			logger_.error("Unexpected error", e);
			text = "-ERROR-";
		}
		return text;
	}
	
	/**
	 * 
	 * @param attachedDataColumn null for most types - applicable to {@link DynamicRefexColumnType#ATTACHED_DATA}
	 * @return
	 */
	public ToIntFunction<RefexDynamicVersionBI<?>> getNidFetcher(DynamicRefexColumnType desiredColumn, Integer attachedDataColumn)
	{
		switch (desiredColumn)
		{
			case STATUS_CONDENSED:
			{
				throw new RuntimeException("Improper API usage");
			}
			case COMPONENT:
			{
				return new ToIntFunction<RefexDynamicVersionBI<?>>()
				{
					@Override
					public int applyAsInt(RefexDynamicVersionBI<?> value)
					{
						return refex_.getReferencedComponentNid();
					}
				};
			}
			case ASSEMBLAGE:
			{
				return new ToIntFunction<RefexDynamicVersionBI<?>>()
				{
					@Override
					public int applyAsInt(RefexDynamicVersionBI<?> value)
					{
						return refex_.getAssemblageNid();
					}
				};
			}
			case AUTHOR:
			{
				return new ToIntFunction<RefexDynamicVersionBI<?>>()
				{
					@Override
					public int applyAsInt(RefexDynamicVersionBI<?> value)
					{
						return refex_.getAuthorNid();
					}
				};
			}
			case MODULE:
			{
				return new ToIntFunction<RefexDynamicVersionBI<?>>()
				{
					@Override
					public int applyAsInt(RefexDynamicVersionBI<?> value)
					{
						return refex_.getModuleNid();
					}
				};
			}
			case PATH:
			{
				return new ToIntFunction<RefexDynamicVersionBI<?>>()
				{
					@Override
					public int applyAsInt(RefexDynamicVersionBI<?> value)
					{
						return refex_.getPathNid();
					}
				};
			}
			
			case ATTACHED_DATA:
			{
				if (attachedDataColumn == null)
				{
					throw new RuntimeException("API misuse");
				}
				return new ToIntFunction<RefexDynamicVersionBI<?>>()
				{
					@Override
					public int applyAsInt(RefexDynamicVersionBI<?> value)
					{
						RefexDynamicDataBI data = refex_.getData().length > attachedDataColumn ? refex_.getData()[attachedDataColumn] : null;
						if (data != null)
						{
							if (data instanceof RefexDynamicNidBI)
							{
								return ((RefexDynamicNidBI)data).getDataNid();
							}
							else if (data instanceof RefexDynamicUUIDBI)
							{
								if (ExtendedAppContext.getDataStore().hasUuid(((RefexDynamicUUIDBI)data).getDataUUID()))
								{
									return ExtendedAppContext.getDataStore().getNidForUuids(((RefexDynamicUUIDBI)data).getDataUUID());
								}
							}
						}
						return 0;
					}
				};
				
			}

			default:
				throw new RuntimeException("Missing implementation: " + desiredColumn);
		}
	}
}
