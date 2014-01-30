/**
 * Copyright 2013
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
package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Bound;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Destination;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Interval;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.model.schemaModel.Point;
import gov.va.legoEdit.model.schemaModel.PointDouble;
import gov.va.legoEdit.model.schemaModel.PointLong;
import gov.va.legoEdit.model.schemaModel.PointMeasurementConstant;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.model.schemaModel.Type;
import gov.va.legoEdit.model.schemaModel.Units;
import gov.va.legoEdit.model.schemaModel.Value;
import gov.va.legoEdit.util.TimeConvert;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The toString methods more or less follow the XML schema structure, and are very verbose.  Mostly used for debugging, 
 * and also for the copy to clipboard functions.
 * 
 * The summary methods are a much more compact summary, used in top of the lego tree view.
 * SchemaToString
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 *
 */
public class SchemaToString
{
	private static Logger logger = LoggerFactory.getLogger(SchemaToString.class);
	public final static String eol = System.getProperty("line.separator");
	public final static String infinity = "\u221e";
	public final static String center = " \u2716 ";
	public final static String downRightArrow = " \u21b3 ";
	public final static String rightArrow = " \u2192 ";
	public final static String ltEq = "\u2264";

	public static String toString(Object o)
	{
		if (o instanceof Lego)
		{
			return toString((Lego) o);
		}
		else if (o instanceof Assertion)
		{
			return toString((Assertion) o, "");
		}
		else if (o instanceof Discernible)
		{
			return toString((Discernible) o, "");
		}
		else if (o instanceof Qualifier)
		{
			return toString((Qualifier) o, "");
		}
		else if (o instanceof Value)
		{
			return toString((Value) o, "");
		}
		else if (o instanceof Expression)
		{
			return toString((Expression) o, "");
		}
		else
		{
			logger.warn("Unsupported use of SchemaToString for " + o);
			return o.toString();
		}
	}

	public static String summary(Object o)
	{
		if (o instanceof Assertion)
		{
			return summary((Assertion) o, "");
		}
		else if (o instanceof Discernible)
		{
			return summary((Discernible) o, "");
		}
		else if (o instanceof Qualifier)
		{
			return summary((Qualifier) o, "");
		}
		else if (o instanceof Value)
		{
			return summary((Value) o, "");
		}
		else if (o instanceof Lego)
		{
			return summary((Lego) o, "");
		}
		else if (o instanceof LegoList)
		{
			return summary((LegoList) o);
		}
		else
		{
			logger.warn("Unsupported use of SchemaToString summary for " + o);
			return o.toString();
		}
	}
	
	public static String summary(LegoList ll)
	{
		if (ll == null)
		{
			return "<null LegoList>";
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append("LegoList " + ll.getLegoListUUID());
			sb.append(eol + "  Group Name " + ll.getGroupName());
			if (ll.getGroupDescription() != null && ll.getGroupDescription().length() > 0)
			{
				sb.append(eol + "  Group Description " + ll.getGroupDescription());
			}
			if (ll.getComment() != null && ll.getComment().length() > 0)
			{
				sb.append(eol + "  Comment: " + ll.getComment());
			}
			for (Lego l : ll.getLego())
			{
				sb.append(eol + summary(l, "  "));
			}
			return sb.toString();
		}
	}
	
	public static String summary(Lego l, String prefix)
	{
		if (l == null)
		{
			return prefix + "<null Lego>";
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(prefix + "Lego " + l.getLegoUUID());
			if (l.getComment() != null && l.getComment().length() > 0)
			{
				sb.append(eol + prefix + "  Comment: " + l.getComment());
			}
			sb.append(eol + toString(l.getPncs(), prefix + "  "));
			sb.append(eol + toString(l.getStamp(), prefix + "  "));
			for (Assertion a : l.getAssertion())
			{
				sb.append(eol + toString(a, prefix + "  "));
			}

			return sb.toString();
		}
	}

	public static String toString(Lego l)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Lego " + l.getLegoUUID());
		if (l.getComment() != null && l.getComment().length() > 0)
		{
			sb.append(eol + "  Comment: " + l.getComment());
		}
		sb.append(eol + toString(l.getPncs(), "  "));
		sb.append(eol + toString(l.getStamp(), "  "));
		for (Assertion a : l.getAssertion())
		{
			sb.append(eol + toString(a, "  "));
		}

		return sb.toString();
	}

	public static String toString(Pncs pncs, String prefix)
	{
		if (pncs == null)
		{
			return "";
		}
		return prefix + "PNCS: " + pncs.getName() + " (" + pncs.getId() + ") " + pncs.getValue();
	}

	public static String toString(Stamp stamp, String prefix)
	{
		if (stamp == null)
		{
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(prefix + "Stamp:");
		sb.append(eol + prefix + "  Status: " + stamp.getStatus());
		sb.append(eol + prefix + "  Time: " + new Date(TimeConvert.convert(stamp.getTime())).toString());
		sb.append(eol + prefix + "  Author: " + stamp.getAuthor());
		sb.append(eol + prefix + "  Module: " + stamp.getModule());
		sb.append(eol + prefix + "  Path: " + stamp.getPath());
		sb.append(eol + prefix + "  UUID: " + stamp.getUuid());

		return sb.toString();
	}

	public static String toString(Assertion a, String prefix)
	{
		if (a == null)
		{
			return prefix + "<null Assertion>";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(prefix + "Assertion " + a.getAssertionUUID());
		sb.append(eol + toString(a.getDiscernible(), prefix + "  "));
		sb.append(eol + toString(a.getQualifier(), prefix + "  "));
		String timing = toString(a.getTiming(), prefix + "  ", "Timing");
		if (timing.length() > 0)
		{
			sb.append(eol + timing);
		}
		sb.append(eol + toString(a.getValue(), prefix + "  "));
		if (a.getAssertionComponent().size() > 0)
		{
			sb.append(eol + prefix + "  Assertion Components");
			for (AssertionComponent ac : a.getAssertionComponent())
			{
				sb.append(eol + toString(ac, prefix + "  "));
			}
		}
		sb.append(eol);
		return sb.toString();
	}
	
	public static String summary(Assertion a, String prefix)
	{
		if (a == null)
		{
			return prefix + "<null Assertion>";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(prefix + "Assertion");
		sb.append(eol + summary(a.getDiscernible(), prefix + "  "));
		sb.append(eol + summary(a.getQualifier(), prefix + "  "));
		String timing = toString(a.getTiming(), prefix + "  ", "Timing:");
		if (timing.length() > 0)
		{
			sb.append(eol + timing);
		}
		sb.append(eol + summary(a.getValue(), prefix + "  "));
		if (a.getAssertionComponent().size() > 0)
		{
			for (AssertionComponent ac : a.getAssertionComponent())
			{
				sb.append(eol + toString(ac, prefix + "  "));
			}
		}
		sb.append(eol);
		return sb.toString();
	}

	public static String toString(AssertionComponent ac, String prefix)
	{
		if (ac == null)
		{
			return "";
		}
		return prefix + "Assertion Component: " + toString(ac.getType(), "") + rightArrow + ac.getAssertionUUID();
	}

	public static String toString(Type t, String prefix)
	{
		if (t == null)
		{
			return "";
		}
		if (t.getConcept() != null)
		{
			return prefix + toString(t.getConcept(), "");
		}
		else
		{
			return prefix + "<null Concept>";
		}
	}

	public static String toString(Discernible d, String prefix)
	{
		if (d == null || d.getExpression() == null)
		{
			return prefix + "<null Discernible>";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(prefix + "Discernible " + eol);
		sb.append(toString(d.getExpression(), prefix + "  "));

		return sb.toString();
	}
	
	public static String summary(Discernible d, String prefix)
	{
		if (d == null || d.getExpression() == null)
		{
			return prefix + "<null Discernible>";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(prefix + "Discernible: ");
		sb.append(summary(d.getExpression(), prefix + "  ", false));

		return sb.toString();
	}

	public static String toString(Qualifier q, String prefix)
	{
		if (q == null || q.getExpression() == null)
		{
			return prefix + "<null Discernible>";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(prefix + "Qualifier" + eol);
		sb.append(toString(q.getExpression(), prefix + "  "));

		return sb.toString();
	}
	
	public static String summary(Qualifier q, String prefix)
	{
		if (q == null || q.getExpression() == null)
		{
			return prefix + "<null Discernible>";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(prefix + "Qualifier: ");
		sb.append(summary(q.getExpression(), prefix + "  ", false));

		return sb.toString();
	}

	public static String toString(Value v, String prefix)
	{
		if (v == null || (v.getExpression() == null && v.getMeasurement() == null && v.getText() == null && v.isBoolean() == null))
		{
			return prefix + "<null Value>";
		}

		StringBuilder sb = new StringBuilder();
		if (v.getExpression() != null)
		{
			sb.append(prefix + "Value" + eol);
			sb.append(toString(v.getExpression(), prefix + "  "));
		}
		else if (v.getMeasurement() != null)
		{
			sb.append(prefix + "Value" + eol);
			sb.append(toString(v.getMeasurement(), prefix + "  ", ""));
		}
		else if (v.getText() != null)
		{
			sb.append(prefix + "Value ");
			sb.append(v.getText());
		}
		else if (v.isBoolean() != null)
		{
			sb.append(prefix + "Value ");
			sb.append(v.isBoolean());
		}

		return sb.toString();
	}
	
	public static String summary(Value v, String prefix)
	{
		if (v == null || (v.getExpression() == null && v.getMeasurement() == null && v.getText() == null && v.isBoolean() == null))
		{
			return prefix + "<null Value>";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(prefix + "Value: ");
		if (v.getExpression() != null)
		{
			sb.append(summary(v.getExpression(), prefix + "  ", false));
		}
		else if (v.getMeasurement() != null)
		{
			sb.append(summary(v.getMeasurement(), ""));
		}
		else if (v.getText() != null)
		{
			sb.append(v.getText());
		}
		else if (v.isBoolean() != null)
		{
			sb.append(v.isBoolean());
		}
		return sb.toString();
	}

	public static String toString(Measurement m, String prefix, String type)
	{
		if (m == null)
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(prefix + (type != null && type.length() > 0 ? type : "Measurement"));

		sb.append(" ");
		if (m.getBound() != null)
		{
			sb.append(toString(m.getBound(), "", true));
		}

		else if (m.getInterval() != null)
		{
			sb.append(toString(m.getInterval(), ""));
		}

		else if (m.getPoint() != null)
		{
			sb.append(toString(m.getPoint(), ""));
		}

		if (m.getUnits() != null)
		{
			sb.append(toString(m.getUnits(), " "));
		}
		return sb.toString();
	}
	
	public static String summary(Measurement m, String prefix)
	{
		if (m == null)
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();
		if (m.getBound() != null)
		{
			sb.append(toString(m.getBound(), "", true));
		}

		else if (m.getInterval() != null)
		{
			sb.append(toString(m.getInterval(), ""));
		}

		else if (m.getPoint() != null)
		{
			sb.append(toString(m.getPoint(), ""));
		}

		if (m.getUnits() != null)
		{
			sb.append(toString(m.getUnits(), " "));
		}
		return sb.toString();
	}

	public static String toString(Interval interval, String prefix)
	{
		StringBuilder sb = new StringBuilder();
		if (interval != null)
		{
			sb.append(prefix);
			if (interval.getLowerBound() != null)
			{
				sb.append(toString(interval.getLowerBound(), "", false));
			}
			sb.append(" " + ltEq + center + ltEq + " ");
			if (interval.getUpperBound() != null)
			{
				sb.append(toString(interval.getUpperBound(), "", false));
			}
		}
		return sb.toString();
	}

	public static String toString(Bound bound, String prefix, boolean useInfinities)
	{
		StringBuilder sb = new StringBuilder();
		if (bound != null)
		{
			sb.append(prefix);
			if ((bound.getLowerPoint() != null || useInfinities) && (bound.getUpperPoint() != null || useInfinities))
			{
				sb.append(bound.isLowerPointInclusive() == null || bound.isLowerPointInclusive() ?  "[" : "(");
			}
			sb.append(bound.getLowerPoint() == null && useInfinities ? "-" + infinity : toString(bound.getLowerPoint(), ""));
			if ((bound.getLowerPoint() != null || useInfinities) && (bound.getUpperPoint() != null || useInfinities))
			{
				sb.append(", ");
			}
			sb.append(bound.getUpperPoint() == null && useInfinities ? infinity : toString(bound.getUpperPoint(), ""));
			if ((bound.getLowerPoint() != null || useInfinities) && (bound.getUpperPoint() != null || useInfinities))
			{
				sb.append(bound.isUpperPointInclusive() == null || bound.isUpperPointInclusive() ?  "]" : ")");
			}
		}
		return sb.toString();
	}

	public static String toString(Point p, String prefix)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		if (p != null)
		{
			if (p instanceof PointLong)
			{
				sb.append(((PointLong) p).getValue());
			}
			else if (p instanceof PointDouble)
			{
				sb.append(((PointDouble) p).getValue());
			}
			else if (p instanceof PointMeasurementConstant)
			{
				//This happens after we removed some enum types when you read old data.
				if (((PointMeasurementConstant) p).getValue() != null)
				{
					sb.append(((PointMeasurementConstant) p).getValue().value());
				}
			}
		}
		return sb.toString();
	}

	public static String toString(Units u, String prefix)
	{
		if (u == null)
		{
			return prefix + "";
		}
		else
		{
			return toString(u.getConcept(), prefix);
		}
	}

	public static String toString(Concept c, String prefix)
	{
		if (c == null)
		{
			return prefix + "";
		}
		else
		{
			return prefix
					+ (c.getDesc() != null && c.getDesc().length() > 0 ? c.getDesc() : (c.getSctid() != null ? c.getSctid().toString() : (c.getUuid() != null ? c
							.getUuid() : "")));
		}
	}

	public static String toString(Expression e, String prefix)
	{
		if (e == null)
		{
			return prefix + "<null Expression>";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(prefix + "Expression");

		if (e.getConcept() != null)
		{
			sb.append(eol + toString(e.getConcept(), prefix + "  "));
		}
		else
		{
			if (e.getExpression().size() > 0)
			{
				sb.append(eol + prefix + "  Conjunction");
			}
			for (Expression e1 : e.getExpression())
			{
				sb.append(eol + toString(e1, prefix + "  |  "));
			}
		}
		for (Relation r : e.getRelation())
		{
			sb.append(eol + prefix + downRightArrow);
			sb.append(toString(r.getType(), ""));
			sb.append(rightArrow);
			sb.append(toString(r.getDestination(), prefix + "    ", false));
		}
		for (RelationGroup rg : e.getRelationGroup())
		{
			sb.append(eol + prefix + downRightArrow +  "  Relation Group");
			for (Relation r : rg.getRelation())
			{
				sb.append(eol + prefix + "  " + downRightArrow);
				sb.append(toString(r.getType(), ""));
				sb.append(rightArrow);
				sb.append(toString(r.getDestination(), prefix + "      ", false));
			}
		}
		return sb.toString();
	}

	public static String summary(Expression e, String prefix, boolean prefixIndividualConcept)
	{
		if (e == null)
		{
			return prefix + "<null Expression>";
		}
		
		int conceptLength = 0;

		StringBuilder sb = new StringBuilder();
		if (e.getConcept() != null)
		{
			sb.append(toString(e.getConcept(), (prefixIndividualConcept ? prefix : "")));
			conceptLength += sb.length();
		}
		else
		{
			if (e.getExpression().size() > 0)
			{
				sb.append(eol + prefix + "  Conjunction");
			}
			for (Expression e1 : e.getExpression())
			{
				sb.append(eol + summary(e1, prefix + "  |  ", true));
			}
		}
		for (Relation r : e.getRelation())
		{
			sb.append(eol + prefix + downRightArrow);
			String temp = toString(r.getType(), "");
			StringBuilder indent = new StringBuilder();
			indent.append(prefix);
			for (int i = 0; i < conceptLength + temp.length(); i++)
			{
				indent.append(" ");
			}
			sb.append(temp);
			sb.append(rightArrow);
			sb.append(summary(r.getDestination(), indent.toString()));
		}
		for (RelationGroup rg : e.getRelationGroup())
		{
			sb.append(eol + prefix + downRightArrow + "Relation Group");
			for (Relation r : rg.getRelation())
			{
				sb.append(eol + prefix + "  " + downRightArrow);
				String temp = toString(r.getType(), "");
				StringBuilder indent = new StringBuilder();
				indent.append(prefix);
				for (int i = 0; i < conceptLength + temp.length(); i++)
				{
					indent.append(" ");
				}
				sb.append(temp);
				sb.append(rightArrow);
				sb.append(summary(r.getDestination(), indent.toString() + "  "));
			}
		}
		return sb.toString();
	}
	
	public static String toString(Destination d, String prefix, boolean usePrefixOnFirstLine)
	{
		if (d == null)
		{
			return (usePrefixOnFirstLine ? prefix : "") + "<null Destination>";
		}

		if (d.isBoolean() != null)
		{
			return (usePrefixOnFirstLine ? prefix : "") + d.isBoolean();
		}
		else if (d.getText() != null)
		{
			return (usePrefixOnFirstLine ? prefix : "") + d.getText();
		}
		else if (d.getExpression() != null)
		{
			return eol + toString(d.getExpression(), prefix);
		}
		else if (d.getMeasurement() != null)
		{
			return toString(d.getMeasurement(), (usePrefixOnFirstLine ? prefix : ""), "");
		}
		else
		{
			return "";
		}
	}
	
	public static String summary(Destination d, String prefix)
	{
		if (d == null)
		{
			return "<null Destination>";
		}

		if (d.isBoolean() != null)
		{
			return d.isBoolean() + "";
		}
		else if (d.getText() != null)
		{
			return d.getText();
		}
		else if (d.getExpression() != null)
		{
			return summary(d.getExpression(), prefix, false);
		}
		else if (d.getMeasurement() != null)
		{
			return summary(d.getMeasurement(), "");
		}
		else
		{
			return "";
		}
	}
}
