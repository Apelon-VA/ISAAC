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
package gov.va.issac.drools.manager;

import gov.va.issac.drools.evaluators.IsGbMemberTypeOfEvaluatorDefinition;
import gov.va.issac.drools.evaluators.IsKindOfEvaluatorDefinition;
import gov.va.issac.drools.evaluators.IsMemberOfEvaluatorDefinition;
import gov.va.issac.drools.evaluators.IsMemberOfWithTypeEvaluatorDefinition;
import gov.va.issac.drools.evaluators.IsMissingDescForDialectEvaluatorDefinition;
import gov.va.issac.drools.evaluators.IsParentMemberOfEvaluatorDefinition;
import gov.va.issac.drools.evaluators.IsSynonymMemberTypeOfEvaluatorDefinition;
import gov.va.issac.drools.evaluators.IsUsMemberTypeOfEvaluatorDefinition;
import gov.va.issac.drools.evaluators.SatisfiesConstraintEvaluatorDefinition;
import gov.va.issac.drools.manager.DroolsExecutorsManager.ExtraEvaluators;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.EvaluatorOption;
import org.drools.conf.ConsequenceExceptionHandlerOption;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DroolsExecutor}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DroolsExecutor
{
	public static String drools_dialect_java_compiler = null;  //a mechanism for outside code to specify the compiler dialect that will be used (normally null)

	private String name_;
	private long compileTime_;

	private KnowledgeBase kbase_;
	private Collection<KnowledgePackage> knowledgePackages_;

	private static Logger logger = LoggerFactory.getLogger(DroolsExecutor.class);

	/**
	 * Only to be called by the {@link DroolsExecutorsManager}
	 * 
	 * Constructs from a pre-compiled drools file
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	protected DroolsExecutor(String name, File compiledRulesFile) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		name_ = name;
		compileTime_ = compiledRulesFile.lastModified();
		sharedSetup();
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(compiledRulesFile));
		knowledgePackages_ = (Collection<KnowledgePackage>) in.readObject();
		in.close();
		kbase_.addKnowledgePackages(knowledgePackages_);
	}

	/**
	 * Only to be called by the {@link DroolsExecutorsManager}
	 * 
	 * Compiles the specified files into an executor
	 * 
	 * @param name
	 * @param extraEvaluators
	 * @param kbFiles
	 * @throws RuntimeException
	 * @throws IOException
	 */
	protected DroolsExecutor(String name, EnumSet<ExtraEvaluators> extraEvaluators, File... kbFiles) throws RuntimeException, IOException
	{
		name_ = name;
		compileTime_ = System.currentTimeMillis();

		sharedSetup();
		knowledgePackages_ = loadKnowledgePackages(extraEvaluators, kbFiles);
		kbase_.addKnowledgePackages(knowledgePackages_);
	}

	private void sharedSetup()
	{
		logger.debug("Configuring KnowledgeBase Configuration for " + name_);
		KnowledgeBaseConfiguration kBaseConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();

		ConsequenceExceptionHandlerOption cehOption = ConsequenceExceptionHandlerOption.get(DroolsExceptionHandler.class);

		kBaseConfig.setOption(cehOption);
		if (drools_dialect_java_compiler != null)
		{
			kBaseConfig.setProperty("drools.dialect.java.compiler", drools_dialect_java_compiler);
		}

		kbase_ = KnowledgeBaseFactory.newKnowledgeBase(kBaseConfig);
	}

	private final Collection<KnowledgePackage> loadKnowledgePackages(EnumSet<ExtraEvaluators> extraEvaluators, File... kbFiles) throws RuntimeException, IOException
	{
		HashMap<Resource, ResourceType> resources = new HashMap<Resource, ResourceType>();
		for (File f : kbFiles)
		{
			resources.put(ResourceFactory.newFileResource(f), ResourceType.DRL);
		}
		Properties props = new Properties();
		if (drools_dialect_java_compiler != null)
		{
			props.setProperty("drools.dialect.java.compiler", drools_dialect_java_compiler);
		}
		KnowledgeBuilderConfiguration builderConfig = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(props, (ClassLoader) null);

		if (extraEvaluators.contains(ExtraEvaluators.IS_KIND_OF))
		{
			builderConfig.setOption(EvaluatorOption.get(IsKindOfEvaluatorDefinition.IS_KIND_OF.getOperatorString(), new IsKindOfEvaluatorDefinition()));
		}
		if (extraEvaluators.contains(ExtraEvaluators.SAFISFIES_CONSTRAINT))
		{
			builderConfig.setOption(EvaluatorOption.get(SatisfiesConstraintEvaluatorDefinition.SATISFIES_CONSTRAINT.getOperatorString(),
					new SatisfiesConstraintEvaluatorDefinition()));
		}
		if (extraEvaluators.contains(ExtraEvaluators.IS_MEMBER_OF))
		{
			builderConfig.setOption(EvaluatorOption.get(IsMemberOfEvaluatorDefinition.IS_MEMBER_OF.getOperatorString(), new IsMemberOfEvaluatorDefinition()));
		}
		if (extraEvaluators.contains(ExtraEvaluators.IS_MEMBER_OF_WITH_TYPE))
		{
			builderConfig.setOption(EvaluatorOption.get(IsMemberOfWithTypeEvaluatorDefinition.IS_MEMBER_OF_WITH_TYPE.getOperatorString(),
					new IsMemberOfWithTypeEvaluatorDefinition()));
		}
		if (extraEvaluators.contains(ExtraEvaluators.IS_PARENT_MEMBER_OF))
		{
			builderConfig.setOption(EvaluatorOption.get(IsParentMemberOfEvaluatorDefinition.IS_PARENT_MEMBER_OF.getOperatorString(),
					new IsParentMemberOfEvaluatorDefinition()));
		}
		if (extraEvaluators.contains(ExtraEvaluators.IS_MISSING_DESC_FOR))
		{
			builderConfig.setOption(EvaluatorOption.get(IsMissingDescForDialectEvaluatorDefinition.IS_MISSING_DESC_FOR.getOperatorString(),
					new IsMissingDescForDialectEvaluatorDefinition()));
		}

		if (extraEvaluators.contains(ExtraEvaluators.IS_GB_MEMBER_TYPE_OF))
		{
			builderConfig.setOption(EvaluatorOption.get(IsGbMemberTypeOfEvaluatorDefinition.IS_GB_MEMBER_TYPE_OF.getOperatorString(),
					new IsGbMemberTypeOfEvaluatorDefinition()));
		}

		if (extraEvaluators.contains(ExtraEvaluators.IS_US_MEMBER_TYPE_OF))
		{
			builderConfig.setOption(EvaluatorOption.get(IsUsMemberTypeOfEvaluatorDefinition.IS_US_MEMBER_TYPE_OF.getOperatorString(),
					new IsUsMemberTypeOfEvaluatorDefinition()));
		}

		if (extraEvaluators.contains(ExtraEvaluators.IS_SYNONYM_MEMBER_TYPE_OF))
		{
			builderConfig.setOption(EvaluatorOption.get(IsSynonymMemberTypeOfEvaluatorDefinition.IS_SYNONYM_MEMBER_TYPE_OF.getOperatorString(),
					new IsSynonymMemberTypeOfEvaluatorDefinition()));
		}

		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase_, builderConfig);
		for (Resource resource : resources.keySet())
		{
			synchronized (this)
			{
				kbuilder.add(resource, resources.get(resource));
			}
		}
		if (kbuilder.hasErrors())
		{
			throw new RuntimeException(kbuilder.getErrors().toString());
		}

		return kbuilder.getKnowledgePackages();
	}

	/**
	 * @return the name_
	 */
	public String getName()
	{
		return name_;
	}

	/**
	 * @return the compileTime_
	 */
	public long getCompileTime()
	{
		return compileTime_;
	}

	/**
	 * @return the knowledgePackages_
	 */
	protected Collection<KnowledgePackage> getKnowledgePackages()
	{
		return knowledgePackages_;
	}

	public void fireAllRules(Map<String, Object> globals, Collection<Object> facts) throws DroolsException, IOException
	{
		KnowledgeRuntimeLogger logger = null;
		StatefulKnowledgeSession ksession = null;
		try
		{
			ksession = kbase_.newStatefulKnowledgeSession();

			//enable this for debug...
			//TODO disable
			logger = KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

			for (Map.Entry<String, Object> e : globals.entrySet())
			{
				ksession.setGlobal(e.getKey(), e.getValue());
			}
			for (Object fact : facts)
			{
				ksession.insert(fact);
			}
			ksession.fireAllRules();
		}
		catch (Exception e)
		{
			if (e instanceof DroolsException)
			{
				throw e;
			}
			else
			{
				throw new IOException("Problem executing drools rules", e);
			}
		}
		finally
		{
			ksession.dispose();
			if (logger != null)
			{
				logger.close();
			}
		}
	}
}
