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
package gov.va.isaac.interfaces.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.jvnet.hk2.annotations.Contract;

/**
 * {@link IsaacAppConfigI}
 * 
 * This interface only exists as a mechanism to provide read-only access to the
 * items generated out of the AppConfigSchema.xsd file. This interface should be
 * kept in sync with the definitions within the AppConfigSchema.xsd file - as
 * that is what end users will be writing - which eventually populates the
 * values that return from these getters.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface IsaacAppConfigI {

  /**
   * Archetype group id.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getArchetypeGroupId();

  /**
   * Archetype artifact id.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getArchetypeArtifactId();

  /**
   * Archetype version.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getArchetypeVersion();

  /**
   * ISAAC toolkit version.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getIsaacVersion();

  /**
   * Code base for the PA project.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getScmConnection();

  /**
   * Browsable URL for PA project code.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getScmUrl();

  /**
   * Browsable URL for PA project code.
   * 
   * @return a URL
   */
  public URL getScmUrlAsURL();

  /**
   * Distribution repository id.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDistReposId();

  /**
   * Distribution repository name.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDistReposName();

  /**
   * Distribution repository Url.
   * 
   * @return possible object is {@link String }
   */
  public String getDistReposUrl();

  /**
   * Distribution repository Url.
   * 
   * @return the URL
   */
  public URL getDistReposUrlAsURL();

  /**
   * Distribution snapshot repository id.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDistReposSnapId();

  /**
   * Distribution snapshot repository name.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDistReposSnapName();

  /**
   * Distribution snapshot repository Url.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDistReposSnapUrl();

  /**
   * Returns the dist repos snap url as url.
   *
   * @return the dist repos snap url as url
   */
  public URL getDistReposSnapUrlAsURL();

  /**
   * Database group id.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDbGroupId();

  /**
   * Database artifact id.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDbArtifactId();

  /**
   * Database version.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDbVersion();

  /**
   * Database classifier.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDbClassifier();

  /**
   * Drools group id.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDroolsGroupId();

  /**
   * Drools artifact id.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDroolsArtifactId();

  /**
   * Drools version.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDroolsVersion();

  /**
   * Drools classifier.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDroolsClassifier();

  /**
   * The text string that is displayed in the ISAAC title bar, about box, and
   * other typical locations.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getApplicationTitle();

  /**
   * Previous release version.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getPreviousReleaseVersion();

  /**
   * Release version.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getReleaseVersion();

  /**
   * Extension namespace.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getExtensionNamespace();

  /**
   * The SVN or GIT URL that will be used to synchronize user profiles and
   * changesets for this bundle.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getChangeSetUrl();

  /**
   * The SVN or GIT URL that will be used to synchronize user profiles and
   * changesets for this bundle.
   * 
   * @return the URL
   * 
   */
  public URL getChangeSetUrlAsURL();

  /**
   * Default edit path name.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDefaultEditPathName();

  /**
   * Default edit path uuid.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDefaultEditPathUuid();

  /**
   * Default view path name.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDefaultViewPathName();

  /**
   * Default view path uuid.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDefaultViewPathUuid();

  /**
   * The URI to this document.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getUserSchemaLocation();

  /**
   * The full URL for the REST API of the KIE Workflow server.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getWorkflowServerUrl();

  /**
   * The full URL for the REST API of the KIE Workflow server.
   * 
   * @return possible object is {@link String }
   * 
   */
  public URL getWorkflowServerUrlAsURL();

  /**
   * The deployment ID for the KIE workflow server.
   *
   * @return possible object is {@link String }
   */
  public String getWorkflowServerDeploymentId();

  /**
   * The UUID for the Path to which content published via Workflow will
   * automatically be promoted to.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getPromotionPath();

  /**
   * The UUID for the Path to which content published via Workflow will
   * automatically be promoted to.
   * 
   * @return the UUID
   */
  public UUID getPromotionPathAsUUID();

  /**
   * Returns the url for string.
   *
   * @param value the value
   * @return the url for string
   */
  public static URL getUrlForString(String value) {
    try {
      return new URL(value);
    } catch (MalformedURLException e) {
      return null;
    }
  }


  /**
   * Returns the uuid for string.
   *
   * @param value the value
   * @return the uuid for string
   */
  public static UUID getUuidForString(String value) {
    if (value == null) {
      return null;
    }
    return UUID.fromString(value);
  }
}
