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
import eu.assina.common.ApiErrorResponse;
import eu.assina.csc.error.CSCInvalidRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


/**
 * Captures exceptions going out of the REST layer and converts ApiErrors into proper
 * error responses. The error response deliberatly matches that prescribed in the
 * CSC spec v1.0.4.0
 *
 * Example: (From 10.1 of CSC spec 1.0.4.0)
 *   HTTP/1.1 400 Bad Request
 *   Date: Mon, 03 Dec 2018 12:00:00 GMT Content-Type: application/json;charset=utf-8
 *   Content-Length: ...
 *   {
 *     "error": "invalid_request",
 *     "error_description": "The access token is not valid"
 *   }
 */
@ControllerAdvice
public class ApiExceptionHandler
		extends ResponseEntityExceptionHandler
{
	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(value = {ApiException.class})
	protected ResponseEntity<?> handleApiException(RuntimeException ae, WebRequest request) {
		ApiError apiError = ((ApiException)ae).getApiError();
		String message = ae.getMessage();
		return apiErrorToResponse(apiError, message, ae, request);
	}

	/**
	 * Handle validation errors that are triggered by @Valid in the controllors and named by validation
	 * constraints on the payload methods like @NotNull or @NotBlank.
	 *
	 * The custom messages in these annotations match enum names for CSC or API errors so that the
	 * validation error can be converted to the proper error response body (according to the CSC spec)
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
																  HttpHeaders headers,
																  HttpStatus status,
																  WebRequest request) {
		ApiError apiError;
		if (ex.hasFieldErrors()) {
			final String error = ex.getFieldError().getDefaultMessage();
			try {
				apiError = CSCInvalidRequest.valueOf(error);
			} catch (IllegalArgumentException e) {
				// this is a legitimate case: the validation exception is not a CSC error, try Assina
				try {
					apiError = AssinaError.valueOf(error);
				} catch (IllegalArgumentException e2) {
					apiError = AssinaError.UnexpectedValidationError;
				}
			}
		}
		else {
			apiError = AssinaError.UnexpectedValidationError;
		}
		return apiErrorToResponse(apiError, ex.getMessage(), ex, request);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(
			Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
		if (ex instanceof ApiException) {
			log.debug("Handled exception in Assina application", ex);
			log.warn("Handled Error: " + ex.getMessage(), (Object[]) ((ApiException)ex).getMessageParams());
		}
		else {
			log.error("Unhandled exception in Assina application", ex);
		}
		return super.handleExceptionInternal(ex, body, headers, status, request);
	}

	private ResponseEntity<Object> apiErrorToResponse(
			ApiError error, String message, Exception ex, WebRequest request) {
		ApiErrorResponse response = new ApiErrorResponse(error.getCode(), error.getDescription());
		HttpStatus httpStatus = HttpStatus.valueOf(error.getHttpCode());
		// log a warning for all messages
		log.warn("Responding to error: {} with status {}. Error description {}; message: {}",
				 error.getCode(), error.getHttpCode(), error.getDescription(), message);

		return handleExceptionInternal(ex, response, new HttpHeaders(), httpStatus, request);
	}
}
