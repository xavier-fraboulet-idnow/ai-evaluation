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

public class ApiException extends RuntimeException {
	private ApiError apiError;
	private String[] messageParams;

	public ApiException(ApiError apiError) {
		super(apiError.getDescription());
		this.apiError = apiError;
	}

	public ApiException(ApiError apiError, String message, String... args) {
		super(message);
		this.apiError = apiError;
		this.messageParams = args;
	}

	public ApiException(ApiError apiError, String message, Exception cause) {
		super(message, cause);
		this.apiError = apiError;
	}

	public ApiException(ApiError apiError, Exception cause) {
		super(apiError.getDescription(), cause);
		this.apiError = apiError;
	}

	public ApiError getApiError() {
		return apiError;
	}

	public String[] getMessageParams() {
		return messageParams;
	}
}
