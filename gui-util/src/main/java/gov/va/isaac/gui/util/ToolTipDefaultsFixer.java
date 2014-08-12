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
package gov.va.isaac.gui.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.slf4j.LoggerFactory;

/**
 * {@link ToolTipDefaultsFixer}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ToolTipDefaultsFixer
{
	/**
	 * Returns true if successful.
	 * Current defaults are 1000, 5000, 200;
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean setTooltipTimers(long openDelay, long visibleDuration, long closeDelay)
	{
		try
		{
			Field f = Tooltip.class.getDeclaredField("BEHAVIOR");
			f.setAccessible(true);
			
			
			Class[] classes = Tooltip.class.getDeclaredClasses();
			for (Class clazz : classes)
			{
				if (clazz.getName().equals("javafx.scene.control.Tooltip$TooltipBehavior"))
				{
					Constructor ctor = clazz.getDeclaredConstructor(Duration.class, Duration.class, Duration.class, boolean.class);
					ctor.setAccessible(true);
					Object tooltipBehavior = ctor.newInstance(new Duration(openDelay), new Duration(visibleDuration), new Duration(closeDelay), false);
					f.set(null, tooltipBehavior);
					break;
				}
			}
		}
		catch (Exception e)
		{
			LoggerFactory.getLogger(ToolTipDefaultsFixer.class).error("Unexpected", e);
			return false;
		}
		return true;
	}
}
