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

import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;

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
	//These variables are used when we are working with a refex that already exists
	private RefexDynamicVersionBI<? extends RefexDynamicVersionBI<?>> refex_;
	private boolean isCurrent_;
	
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
}
