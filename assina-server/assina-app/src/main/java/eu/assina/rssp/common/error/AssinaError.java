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

package eu.assina.rssp.common.error;

import eu.assina.common.ApiError;

/**
 * Enum holding all of the Assina error codes and descriptions
 */
public enum AssinaError implements ApiError {

  CredentialNotFound("credential_not_found",
          "No credential was found matching this query", 404),

  CredentialAlreadyExists("Assina-CredentialAlreadyExists",
          "A credential matching this description already exists", 409),
  CredentialRequestMissingRequiredProperty("Assina-MissingRequiredProperty",
      "The object in the request is missing a required property", 400),
  CredentialRequestInvalidProperty("Assina-InvalidProperty",
      "The object in the request has an invalid property", 400),
  CredentialRequestInvalid("Assina-InvalidObject",
      "The object in the request is invalid", 400),

  // Failing all else general CredentialStore error
  UnexpectedValidationError("assina_unexpected_validiation",
          "An unexpected validation error occurred in the Assina RSSP", 400),

  // Failing all else general CredentialStore error
  UnexpectedError("assina_unexpected_error",
      "An unexpected internal error occurred in the Assina RSSP", 500),

  UserNotFound("user_not_found", "Could not find the requested user in Assina", 404),
  UserDoesNotMatch("user_doesnot_match", "The user ID provided does not match that in the user object", 400),

  UserEmailAlreadyUsed("already_used",
          "The username or email address belongs to an existing user", 409),

  OauthUnauthorizedRedirect("unauthorized_redirect", "Sorry! We've got an Unauthorized Redirect " +
                             "URI and can't proceed with authentication", 400),
  FailedCreatingCredential("failed_creating_credential",
          "An error occurred while generating certificate and keys", 500),

  FailedUnmarshallingPEM("failed_unmarshalling_pem",
          "An error occurred while converting a PEM string to  key or certificate", 500),
  FailedMarshallingPEM("failed_marshalling_pem",
          "An error occurred while converting a key or certificate to a PEM string", 500),
  FailedSigningData("failed_signing",
          "An error occurred while trying to sign data with a certificate and private key", 500);


  private final String code;
  private final int httpCode;
  private String desc;

  // TODO expand into CSC errors
  AssinaError(String code, String desc, int httpCode) {
    this.code = code;
    this.desc = desc;
    this.httpCode = httpCode;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public int getHttpCode() {
    return httpCode;
  }

  @Override
  public String getDescription() {
    return desc;
  }
}

