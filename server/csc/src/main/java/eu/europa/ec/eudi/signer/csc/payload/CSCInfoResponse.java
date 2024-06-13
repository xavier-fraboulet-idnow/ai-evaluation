/*
 Copyright 2024 European Commission

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package eu.europa.ec.eudi.signer.csc.payload;

import eu.europa.ec.eudi.signer.csc.model.AbstractInfo;
import eu.europa.ec.eudi.signer.csc.model.CSCInfo;

/**
 * Info response
 * From section 11.1 of the CSC API V_1.0.4.0 spec
 */
public class CSCInfoResponse extends AbstractInfo {
    // all methods inherited from the abstract base class

    public CSCInfoResponse(CSCInfo other) {
        setDescription(other.getDescription());
        setLang(other.getLang());
        setName(other.getName());
        setMethods(other.getMethods());
        setRegion(other.getRegion());
        setSpecs(other.getSpecs());
    }
}
