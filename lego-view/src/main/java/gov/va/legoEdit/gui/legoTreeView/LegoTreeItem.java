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
package gov.va.legoEdit.gui.legoTreeView;

import gov.va.isaac.util.Utility;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;

/**
 * {@link LegoTreeItem} The actual data item for each node in the tree
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class LegoTreeItem extends TreeItem<String>
{
	protected LegoTreeNodeType ltnt_ = null;
//	private ConceptUsageType cut_ = null; //This is a cache
	protected Object extraData_ = null;
	protected Boolean isValid = null;
	protected Boolean areChildrenValid = null;
	protected String invalidReason_ = null;
	private LegoTreeNodeGraphic treeNodeGraphic = null;
	protected long validationTimestamp = -1;
	
	public LegoTreeItem getLegoParent()
	{
		return (LegoTreeItem)getParent();
	}

	public LegoTreeItem()
	{
		setValue("Hidden Root");
	}

//	public LegoListTreeItem(String value)
//	{
//		setValue(value);
//	}
//
//	public LegoListTreeItem(LegoTreeNodeType tct)
//	{
//		this.ltnt_ = tct;
//		setValue(null);
//	}
//
	public LegoTreeItem(String value, LegoTreeNodeType tct)
	{
		this.ltnt_ = tct;
		setValue(value);
	}
	
	public LegoTreeItem(String value, LegoTreeNodeType tct, Object extraData)
	{
		this.ltnt_ = tct;
		setValue(value);
		this.extraData_ = extraData;
	}
//
//	public LegoListTreeItem(Stamp value, LegoTreeNodeType tct)
//	{
//		this.ltnt_ = tct;
//		extraData_ = value;
//		setValue(null);
//	}
//
//	public LegoListTreeItem(String label, String value, LegoTreeNodeType tct)
//	{
//		this.ltnt_ = tct;
//		setValue(value);
//		extraData_ = label;
//	}


//	public LegoListTreeItem(Lego l, LegoTreeNodeType ltnt)
//	{
//		if (ltnt == LegoTreeNodeType.comment)
//		{
//			setValue("Comment");
//		}
//		else
//		{
//			throw new IllegalArgumentException();
//		}
//		extraData_ = l;
//		ltnt_ = ltnt;
//	}
//
//	public LegoListTreeItem(Assertion a)
//	{
//		setValue("Assertion");
//		ltnt_ = LegoTreeNodeType.assertion;
//		extraData_ = a;
//
//		for (AssertionComponent ac : a.getAssertionComponent())
//		{
//			getChildren().add(new LegoListTreeItem(ac));
//		}
//		if (a.getDiscernible() == null)
//		{
//			a.setDiscernible(new Discernible());
//		}
//
//		Expression de = a.getDiscernible().getExpression();
//		if (de == null)
//		{
//			de = new Expression();
//			a.getDiscernible().setExpression(de);
//		}
//		getChildren().add(new LegoListTreeItem(de, LegoTreeNodeType.expressionDiscernible));
//
//		if (a.getQualifier() == null)
//		{
//			a.setQualifier(new Qualifier());
//		}
//
//		Expression qe = a.getQualifier().getExpression();
//		if (qe == null)
//		{
//			qe = new Expression();
//			a.getQualifier().setExpression(qe);
//		}
//		getChildren().add(new LegoListTreeItem(qe, LegoTreeNodeType.expressionQualifier));
//
//		if (a.getTiming() != null)
//		{
//			getChildren().add(new LegoListTreeItem(a.getTiming(), "Timing"));
//		}
//		if (a.getValue() == null)
//		{
//			a.setValue(new Value());
//		}
//		getChildren().add(new LegoListTreeItem(a.getValue()));
//	}
//
//	public LegoListTreeItem(Value value)
//	{
//		setValue("Value");
//		ltnt_ = LegoTreeNodeType.value;
//		extraData_ = value;
//
//		if (value.getExpression() != null)
//		{
//			getChildren().add(new LegoListTreeItem(value.getExpression(), LegoTreeNodeType.expressionValue));
//		}
//		else if (value.getMeasurement() != null)
//		{
//			getChildren().add(new LegoListTreeItem(value.getMeasurement(), "Measurement"));
//		}
//		else if (value.getText() != null)
//		{
//			getChildren().add(new LegoListTreeItem(value, LegoTreeNodeType.text));
//		}
//		else if (value.isBoolean() != null)
//		{
//			getChildren().add(new LegoListTreeItem(value, LegoTreeNodeType.bool));
//		}
//	}
//
//	public LegoListTreeItem(Measurement measurement, String label)
//	{
//		extraData_ = measurement;
//		setValue(label);  //Expected to be Timing or Measurement
//		
//		if (measurement.getPoint() != null)
//		{
//			ltnt_ = LegoTreeNodeType.measurementPoint;
//		}
//		else if (measurement.getInterval() != null)
//		{
//			ltnt_ = LegoTreeNodeType.measurementInterval;
//		}
//		else if (measurement.getBound() != null)
//		{
//			ltnt_ = LegoTreeNodeType.measurementBound;
//		}
//		else
//		{
//			ltnt_ = LegoTreeNodeType.measurementEmpty;
//		}
//	}
//
//	public LegoListTreeItem(AssertionComponent ac)
//	{
//		setValue("Assertion Component");
//		ltnt_ = LegoTreeNodeType.assertionComponent;
//
//		getChildren().add(new LegoListTreeItem(ac.getAssertionUUID(), LegoTreeNodeType.assertionUUID));
//
//		Type t = ac.getType();
//		if (t == null)
//		{
//			t = new Type();
//			ac.setType(t);
//		}
//
//		Concept c = t.getConcept();
//		if (c == null)
//		{
//			c = new Concept();
//			t.setConcept(c);
//		}
//
//		//no node for type, it is rendered on the asssertion component line
//		extraData_ = ac;
//	}
//
//	public LegoListTreeItem(Concept concept, LegoTreeNodeType tct)
//	{
//		extraData_ = concept;
//		this.ltnt_ = tct;
//	}
//
//	public LegoListTreeItem(Expression expression, LegoTreeNodeType tct)
//	{
//		extraData_ = expression;
//		this.ltnt_ = tct;
//		setValue("Expression");
//
//		if (expression.getConcept() == null && expression.getExpression().size() == 0)
//		{
//			Concept c = new Concept();
//			expression.setConcept(c);
//		}
//		
//		//no node for single concept, that is rendered on the expression line
//
//		if (expression.getExpression().size() > 0)
//		{
//			while (expression.getExpression().size() < 2)
//			{
//				expression.getExpression().add(new Expression());
//			}
//
//			for (Expression e : expression.getExpression())
//			{
//				// If we are building a conjunction, and the type is Discernible or qualifier - the expressions become optional
//				getChildren().add(
//						new LegoListTreeItem(e,
//								(tct == LegoTreeNodeType.expressionDiscernible || tct == LegoTreeNodeType.expressionQualifier) ? LegoTreeNodeType.expressionOptional
//										: tct));
//			}
//		}
//
//		if (expression.getRelation() != null)
//		{
//			for (Relation r : expression.getRelation())
//			{
//				getChildren().add(new LegoListTreeItem(r));
//			}
//		}
//		if (expression.getRelationGroup() != null)
//		{
//			for (RelationGroup rg : expression.getRelationGroup())
//			{
//				LegoListTreeItem rgti = new LegoListTreeItem(rg);
//				getChildren().add(rgti);
//			}
//		}
//	}
//
//	public LegoListTreeItem(RelationGroup relationGroup)
//	{
//		extraData_ = relationGroup;
//		ltnt_ = LegoTreeNodeType.relationshipGroup;
//		setValue("Relation Group");
//		if (relationGroup.getRelation().size() == 0)
//		{
//			relationGroup.getRelation().add(new Relation());
//		}
//		for (Relation r : relationGroup.getRelation())
//		{
//			getChildren().add(new LegoListTreeItem(r));
//		}
//	}
//
//	public LegoListTreeItem(Relation r)
//	{
//		setValue("Relation");
//		ltnt_ = LegoTreeNodeType.relation;
//		extraData_ = r;
//
//		Type t = r.getType();
//		if (t == null)
//		{
//			t = new Type();
//			r.setType(t);
//		}
//		Concept c = t.getConcept();
//		if (c == null)
//		{
//			c = new Concept();
//			t.setConcept(c);
//		}
//
//		//No node for type, it is rendered on the Relation line
//		
//		Destination d = r.getDestination();
//		if (d == null)
//		{
//			d = new Destination();
//			r.setDestination(d);
//		}
//
//		if (d.getExpression() != null)
//		{
//			getChildren().add(new LegoListTreeItem(d.getExpression(), LegoTreeNodeType.expressionDestination));
//		}
//		else if (d.getMeasurement() != null)
//		{
//			getChildren().add(new LegoListTreeItem(d.getMeasurement(), "Measurement"));
//		}
//		else if (d.getText() != null)
//		{
//			getChildren().add(new LegoListTreeItem(d, LegoTreeNodeType.text));
//		}
//		else if (d.isBoolean() != null)
//		{
//			getChildren().add(new LegoListTreeItem(d, LegoTreeNodeType.bool));
//		}
//	}
//
//	public LegoListTreeItem(Value value, LegoTreeNodeType type)
//	{
//		setValue("");
//		ltnt_ = type;
//		extraData_ = value;
//	}
//
//	public LegoListTreeItem(Destination destination, LegoTreeNodeType type)
//	{
//		setValue("");
//		ltnt_ = type;
//		extraData_ = destination;
//	}
//
	public LegoTreeNodeType getNodeType()
	{
		return ltnt_;
	}
//
//	/**
//	 * use caution changing this... only here for measurement support.
//	 */
//	protected void setNodeType(LegoTreeNodeType type)
//	{
//		ltnt_ = type;
//		cut_ = null;
//	}
//	
	public Object getExtraData()
	{
		return extraData_;
	}

	public int getSortOrder()
	{
		if (ltnt_ != null)
		{
			return ltnt_.getSortOrder();
		}
		return 0;
	}
//	
//	/**
//	 * Callers responsibility to ensure the validator has run before calling this to get current results.
//	 */
//	protected String getInvalidReason()
//	{
//		return invalidReason_;
//	}
//	
	public void isValidThreaded(final BooleanCallback callback)
	{
		Utility.execute(new Runnable()
		{
			@Override
			public void run()
			{
				callback.sendResult(isValid());
			}
		});
	}
	
	protected long getValidationTimestamp()
	{
		return validationTimestamp;
	}
	
	private boolean isValid()
	{
		if (isValid == null)
		{
			//prevent multiple threads from duplicating work
			synchronized (this)
			{
				if (isValid == null)
				{
					validate();
				}
			}
		}
		return isValid;
	}
	
	private boolean areChildrenValid()
	{
		if (areChildrenValid == null)
		{
			synchronized (this)
			{
				//prevent multiple threads from duplicating work
				if (areChildrenValid == null)
				{
					validateChildren();
				}
			}
		}
		return areChildrenValid;
	}
	
	protected abstract void validate();
//	private void validate()
//	{
//		if (LegoTreeNodeType.pncsName  == ltnt_ || LegoTreeNodeType.pncsValue == ltnt_ || LegoTreeNodeType.legoListByReference == ltnt_
//				|| LegoTreeNodeType.blankLegoEndNode == ltnt_ || LegoTreeNodeType.blankLegoListEndNode == ltnt_ 
//				|| LegoTreeNodeType.legoReference == ltnt_ || LegoTreeNodeType.status == ltnt_ || LegoTreeNodeType.comment == ltnt_)
//		{
//			invalidReason_ = null;
//		}
//		else if (extraData_ == null)
//		{
//			invalidReason_ = null;
//		}
//		else
//		{
//			//This may be slow.  Turn on the progress indicator
//			Platform.runLater(new Runnable()
//			{
//				@Override
//				public void run()
//				{
//					if (treeNodeGraphic != null)
//					{
//						treeNodeGraphic.showProgress(true);
//					}
//				}
//			});
//			invalidReason_ = Validator.isValid(extraData_, this);
//			//This may be slow.  Turn on the progress indicator
//			Platform.runLater(new Runnable()
//			{
//				@Override
//				public void run()
//				{
//					if (treeNodeGraphic != null)
//					{
//						treeNodeGraphic.showProgress(false);
//					}
//				}
//			});
//		}
//		isValid = invalidReason_ == null;
//		validationTimestamp = System.currentTimeMillis();
//	}
//
	private void validateChildren()
	{
		boolean newValue = true;
		for (TreeItem<String> ti : getChildren())
		{
			LegoTreeItem lti = (LegoTreeItem)ti;
			if (!lti.isValid() || !lti.areChildrenValid())
			{
				newValue = false;
				break;
			}
		}
		
		areChildrenValid = newValue;
	}
	
	public void setTreeNodeGraphic(LegoTreeNodeGraphic node)
	{
		this.treeNodeGraphic = node;
	}
//	
//	public void revalidateToRootThreaded()
//	{
//		Utility.tpe.execute(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				revalidateToRoot();
//			}
//		});
//	}
//	
//	/**
//	 * Runs in current thread.
//	 */
//	public void revalidateToRoot()
//	{
//		validate();
//		validateChildren();
//		updateValidityImage();
//		
//		LegoListTreeItem parent = getLegoParent();
//		if (parent != null)
//		{
//			parent.revalidateToRoot();
//		}
//	}
//	
	public void updateValidityImageThreaded()
	{
		Utility.execute(new Runnable()
		{
			@Override
			public void run()
			{
				updateValidityImage();
			}
		});
	}
	
	private void updateValidityImage()
	{
		final LegoTreeNodeGraphic localTreeNodeGraphic = treeNodeGraphic;
		
		if (localTreeNodeGraphic != null && LegoTreeNodeType.concept != ltnt_ && LegoTreeNodeType.assertionUUID != ltnt_ && LegoTreeNodeType.text != ltnt_)
		{
			//run both of these in the thread that called us (probably a background thread)
			final boolean isValid = isValid(); 
			final boolean areChildrenValid = areChildrenValid();
	
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					localTreeNodeGraphic.showProgress(false);
					if (isValid && areChildrenValid)
		 			{
						localTreeNodeGraphic.showInvalid(false);
					}
					else
					{
						localTreeNodeGraphic.showInvalid(true);
						localTreeNodeGraphic.setInvalidReason((isValid ? "Error in child" : invalidReason_));
					}
				}
			});
		}
	}
//	
//	public ConceptUsageType getConceptUsageType()
//	{
//		if (cut_ != null)
//		{
//			return cut_;
//		}
//		if (LegoTreeNodeType.expressionDestination == ltnt_)
//		{
//			cut_ = ConceptUsageType.REL_DESTINATION;
//		}
//		else if (extraData_ != null)
//		{
//			if (extraData_ instanceof Relation || extraData_ instanceof AssertionComponent)
//			{
//				cut_ = ConceptUsageType.TYPE;
//			}
//			else if (extraData_ instanceof Measurement)
//			{
//				cut_ = ConceptUsageType.UNITS;
//			}
//			else if (extraData_ instanceof Expression)
//			{
//				if (LegoTreeNodeType.expressionDiscernible == ltnt_)
//				{
//					cut_ = ConceptUsageType.DISCERNIBLE;
//				}
//				else if (LegoTreeNodeType.expressionQualifier == ltnt_)
//				{
//					cut_ = ConceptUsageType.QUALIFIER;
//				}
//			}
//			else if (extraData_ instanceof Value)
//			{
//				cut_ = ConceptUsageType.VALUE;
//			}
//		}
//		if (cut_ == null)
//		{
//			// Didn't find it... recurse...
//			LegoListTreeItem parent = getLegoParent();
//			if (parent != null)
//			{
//				cut_ = parent.getConceptUsageType();
//			}
//		}
//		return cut_;
//	}
}
