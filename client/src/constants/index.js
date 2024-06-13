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

// TODO ASSINA Update this to point to the deployed address of the server
//      See https://create-react-app.dev/docs/adding-custom-environment-variables/
export const ASSINA_RSSP_BASE_URL =
    process.env.REACT_APP_ASSINA_RSSP_BASE_URL || "http://localhost:8082";
export const ASSINA_SA_BASE_URL =
    process.env.REACT_APP_ASSINA_SA_BASE_URL || "http://localhost:8083";
export const ASSINA_CLIENT_BASE_URL =
    process.env.REACT_APP_ASSINA_CLIENT_BASE_URL || "http://localhost:3000";

export const API_BASE_URL = ASSINA_RSSP_BASE_URL + "/api/v1";
export const CSC_BASE_URL = ASSINA_RSSP_BASE_URL + "/csc/v1";
export const SA_BASE_URL = ASSINA_SA_BASE_URL + "/sa";

// TODO ASSINA Update this to point to the deployed address of the assina-client react server
export const OAUTH2_REDIRECT_URI = ASSINA_CLIENT_BASE_URL + "/oauth2/redirect";

export const GOOGLE_AUTH_URL =
    ASSINA_RSSP_BASE_URL +
    "/oauth2/authorize/google?redirect_uri=" +
    OAUTH2_REDIRECT_URI;

console.log("API_BASE_URL:        " + API_BASE_URL);
console.log("CSC_BASE_URL:        " + CSC_BASE_URL);
console.log("ASSINA_SA_BASE_URL:  " + ASSINA_SA_BASE_URL);
console.log("OAUTH2_REDIRECT_URI: " + OAUTH2_REDIRECT_URI);

export const ACCESS_TOKEN = "accessToken";
