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
package gov.va.isaac.ie;

import gov.va.isaac.gui.util.FxUtils;
import gov.va.isaac.model.InformationModelType;
import gov.va.isaac.models.InformationModel;
import gov.va.isaac.models.api.InformationModelService;
import gov.va.isaac.models.cem.CEMInformationModel;
import gov.va.isaac.models.fhim.FHIMInformationModel;
import gov.va.isaac.models.hed.HeDInformationModel;
import gov.va.isaac.models.util.ExporterBase;
import gov.va.isaac.util.OTFUtility;

import java.io.IOException;
import java.util.List;

import javax.validation.ValidationException;
import javax.xml.transform.TransformerConfigurationException;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Handles fetching imported information models.
 *
 * @author ocarlsen
 * @author bcarlsenca
 */
public class FetchHandler extends ExporterBase {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(FetchHandler.class);

  /**
   * Instantiates an empty {@link FetchHandler}.
   *
   * @throws ValidationException the validation exception
   */
  public FetchHandler() throws ValidationException {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.va.isaac.models.util.ExporterBase#getLogger()
   */
  @Override
  protected Logger getLogger() {
    return LOG;
  }

  /**
   * Method called by the ISAAC application to fetch the information models.
   *
   * @param modelType the model type
   * @return the list
   * @throws Exception the exception
   */
  public List<InformationModel> fetchModels(InformationModelType modelType)
    throws Exception {
    LOG.debug("modelType=" + (modelType != null ? modelType.name() : "All"));

    // Make sure NOT in application thread.
    FxUtils.checkBackgroundThread();

    if (modelType == null) {

      // Fetch all model types.
      List<InformationModel> allModelTypes = Lists.newArrayList();
      allModelTypes.addAll(fetchCEMModels());
      allModelTypes.addAll(fetchFHIMModels());
      allModelTypes.addAll(fetchHeDModels());
      return allModelTypes;

    } else {

      // Fetch individual model type.
      switch (modelType) {
        case CEM:
          return fetchCEMModels();
        case FHIM:
          return fetchFHIMModels();
        case HeD:
          return fetchHeDModels();
        default:
          throw new IllegalArgumentException("Unrecognized modelType: "
              + modelType);
      }
    }
  }

  /**
   * Fetch he d models.
   *
   * @return the list
   * @throws IOException
   * @throws ContradictionException
   * @throws TransformerConfigurationException 
   */
  private List<InformationModel> fetchHeDModels() throws IOException,
    ContradictionException, TransformerConfigurationException {

    List<InformationModel> models = Lists.newArrayList();
    InformationModelService service = getInformationModelService();
    ConceptVersionBI hedConcept =
        OTFUtility.getConceptVersion(InformationModelType.HeD.getUuid());
    for (ConceptVersionBI hedModel : OTFUtility.getAllChildrenOfConcept(
        hedConcept.getNid(), true)) {
      models.add(new HeDInformationModel(service.getInformationModel(hedModel
          .getPrimordialUuid())));
    }
    return models;

  }

  /**
   * Fetch fhim models.
   *
   * @return the list
   * @throws Exception the exception
   */
  private List<InformationModel> fetchFHIMModels() throws Exception {

    List<InformationModel> models = Lists.newArrayList();
    InformationModelService service = getInformationModelService();
    ConceptVersionBI fhimConcept =
        OTFUtility.getConceptVersion(InformationModelType.FHIM.getUuid());
    for (ConceptVersionBI fhimModel : OTFUtility.getAllChildrenOfConcept(
        fhimConcept.getNid(), true)) {
      models.add(new FHIMInformationModel(service.getInformationModel(fhimModel
          .getPrimordialUuid())));
    }
    return models;
  }

  /**
   * Fetch cem models.
   *
   * @return the list
   * @throws Exception the exception
   */
  private List<InformationModel> fetchCEMModels() throws Exception {

    List<InformationModel> models = Lists.newArrayList();
    InformationModelService service = getInformationModelService();
    ConceptVersionBI cemConcept =
        OTFUtility.getConceptVersion(InformationModelType.CEM.getUuid());
    for (ConceptVersionBI cemModel : OTFUtility.getAllChildrenOfConcept(
        cemConcept.getNid(), true)) {
      models.add(new CEMInformationModel(service.getInformationModel(cemModel
          .getPrimordialUuid())));
    }
    return models;
  }

}
