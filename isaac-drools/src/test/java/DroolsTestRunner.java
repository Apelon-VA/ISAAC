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

import gov.va.isaac.AppContext;
import gov.va.isaac.drools.helper.ResultsCollector;
import gov.va.isaac.drools.helper.ResultsItem;
import gov.va.isaac.drools.helper.templates.AbstractTemplate;
import gov.va.isaac.drools.helper.templates.DescriptionTemplate;
import gov.va.isaac.drools.manager.DroolsExecutor;
import gov.va.isaac.drools.manager.DroolsExecutorsManager;
import gov.va.isaac.drools.testmodel.DrDescription;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;

/**
 * {@link DroolsTestRunner}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DroolsTestRunner
{

	public static void main(String[] args) throws ClassNotFoundException, IOException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, InterruptedException
	{
		AppContext.setup();
		Field f = Hk2Looker.class.getDeclaredField("looker");
		f.setAccessible(true);
		f.set(null, AppContext.getServiceLocator());

		DroolsExecutorsManager dem = AppContext.getService(DroolsExecutorsManager.class);
		
		for (String s : dem.getLoadedExecutors())
		{
			System.out.println(s);
			DroolsExecutor de = dem.getDroolsExecutor(s);

			ArrayList<Object> facts = new ArrayList<>();
			
			DrDescription d = new DrDescription();
			d.setLang("en");
			d.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0].toString());
			d.setText("a test    value");
			
			facts.add(d);
			
			Map<String, Object> globals = new HashMap<>();
			
			ResultsCollector rc = new ResultsCollector();
			
			globals.put("resultsCollector", rc);
			
			int fireCount = de.fireAllRules(globals, facts);
			
			System.out.println("Fire count was " + fireCount);
			
			if (rc.getResultsItems().size() > 0)
			{
				System.out.println("Failed test info:");
				for (ResultsItem r : rc.getResultsItems())
				{
					System.out.println("resultsItem: ");
					System.out.println(r.getMessage());
					System.out.println(r.getSeverity());
					System.out.println(r.getRuleUuid());
					System.out.println(r.getErrorCode());
				}
				
				for (AbstractTemplate  t :rc.getTemplates())
				{
					
					if (t instanceof DescriptionTemplate)
					{
						System.out.println("Suggested fix: '" + ((DescriptionTemplate)t).getText() + "'");
					}
					else
					{
						System.out.println(t.toString());
					}
				}
			}
		}
	}
}
