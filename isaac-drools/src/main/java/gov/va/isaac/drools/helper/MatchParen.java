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
package gov.va.isaac.drools.helper;

/**
 * 
 * {@link MatchParen}
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MatchParen
{
	/**
	 * This variable becomes false if we ever see a right parenthesis
	 * whose corresponding left parenthesis is missing or of the wrong shape.
	 */
	private boolean matching = true;

	/** A stack of all the left parentheses remaining to be matched. */
	private java.util.Stack<Character> s = new java.util.Stack<>();  // 

	/******* PUBLIC METHODS *******/

	/**
	 * Returns true if the sequence of chars added so far
	 * makes up a string with balanced parentheses.
	 */
	public boolean isMatching()
	{
		return matching && s.isEmpty();
	}

	/**
	 * Add a single new character. A user can add the
	 * characters one at a time, and call isMatching after
	 * each character to see whether there is an error yet.
	 */

	public void add(char c)
	{
		Character m = leftMatch(c);   // or use leftMatch2
		if (m == null)                // c is not a parenthesis
			;                              // do nothing (i.e., ignore it)
		else if (m.charValue() == c)  // c is some kind of left parenthesis
			s.push(m);                     // push a Character version, m
		else
		{                        // c is some kind of right paren
			if (s.isEmpty() || !s.pop().equals(m))
				matching = false;            // couldn't pop a matching left paren, m
		}
	}

	/**
	 * Adds a whole string of characters. This is convenient, but
	 * doesn't give a way to find out which character "caused" the
	 * error. (How would you provide a way for the user to find this
	 * out? Hint: isMatching() gives some information but not enough.)
	 */

	public void add(String s)
	{
		for (int i = 0; i < s.length(); i++)
			add(s.charAt(i));
	}

	/******* PROTECTED AND PRIVATE METHODS *******/

	/** All the knowledge about matching parentheses is
	 * encapsulated fairly safely in this static method.  
	 * 
	 * It maps left parentheses ( [ { to themselves
	 * It maps right parentheses ) ] } to the corresponding left parentheses
	 * It maps everything else to null
	 *
	 * The implementation doesn't completely capture the abstraction of
	 * matched pairs.  It would be too easy for someone to modify it
	 * wrong by carelessly adding a new pair like this:
	 *   case '<':
	 *   case '>':
	 *     return Character(')');   // should be '<'
	 */

	protected static Character leftMatch(char c)
	{
		switch (c)
		{
			case '(':
			case ')':
				return new Character('(');
			case '[':
			case ']':
				return new Character('[');
			case '{':
			case '}':
				return new Character('{');
			default:
				return null;
		}
	}

	/**
	 * A somewhat safer and less redundant version of leftMatch. It's
	 * easier to check this by eye. It's still not as safe or clean
	 * as a special class (perhaps extending Character) that supports a
	 * method for adding new pairs.
	 */

	protected static Character leftMatch2(char c)
	{
		Character d = null;
		if (d == null)
			d = leftMatchMaybe(c, '(', ')');
		if (d == null)
			d = leftMatchMaybe(c, '[', ']');
		if (d == null)
			d = leftMatchMaybe(c, '{', '}');
		return d;
	}

	/*
	 * Support for our particular implementation of leftMatch2.
	 * We make it private, which gives us the freedom to change
	 * or eliminate it in the future without the risk of breaking
	 * subclasses that might call it directly.
	 */

	private static Character leftMatchMaybe(char c, char cl, char cr)
	{
		return (c == cl || c == cr) ? new Character(cl) : null;
	}
}
