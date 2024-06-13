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

package eu.europa.ec.eudi.signer.csc.model;

import java.util.Locale;

/**
 * Constants holding certain signer-specific decisions on optional features of
 * the CSC specification
 */
public interface CSCConstants {
    // language supported by Assina - in CSC requests
    String CSC_LANG = Locale.US.toLanguageTag();

    // Max signatures that Assina can sign with in a single request.
    // This is returned in credential/info and validated in credential/authorize
    int CSC_MAX_REQUEST_SIGNATURES = 1;
    // One of implicit | explicit | oauth2code
    // Signer uses 'explicit' to indicate PIN authorization of credential
    String CSC_AUTH_MODE = "explicit";

    // SCAL : per the CSC spec:
    // One of 1 | 2
    // Specifies if the RSSP will generate for this credential a signature
    // activation
    // data (SAD) that contains a link to the hash to-be-signed:
    // • “1”: The hash to-be-signed is not linked to the signature activation data.
    // • “2”: The hash to-be-signed is linked to the signature activation data.
    // This value is OPTIONAL and the default value is “1”.
    // NOTE: As decribed in section 8.2, one difference between SCAL1 and SCAL2, as
    // described in CEN TS 119 241-1 [i.5], is that for SCAL2, the signature
    // activation
    // data needs to have a link to the data to-be-signed. The value “2” only gives
    // information on the link between the hash and the SAD, it does not give
    // information if a full SCAL2 as described in CEN TS 119 241-1 [i.5] is
    // implemented.
    String CSC_SCAL = "1";
}
