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
package gov.va.isaac.models.util;

import gov.va.isaac.util.WBUtility;

import java.io.IOException;

import javax.inject.Inject;

import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;

/**
 * Shared base class for all information model importers & exporters.
 *
 * @author ocarlsen
 */
public class CommonBase {

    @Inject
    private BdbTerminologyStore dataStore;

    protected CommonBase() throws ValidationException, IOException {
        super();
        Hk2Looker.get().inject(this);
    }

    protected final BdbTerminologyStore getDataStore() {
        return dataStore;
    }

    protected final EditCoordinate getEC() throws ValidationException, IOException {
        return WBUtility.getEC();
    }

    protected final ViewCoordinate getVC() throws IOException {
        return WBUtility.getViewCoordinate();
    }
}
