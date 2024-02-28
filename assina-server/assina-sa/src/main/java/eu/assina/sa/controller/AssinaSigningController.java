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

package eu.assina.sa.controller;

import eu.assina.csc.payload.RedirectLinkResponse;
import eu.assina.sa.error.InvalidRequestException;
import eu.assina.sa.payload.SignedFileResponse;
import eu.assina.sa.services.AssinaSigningService;
import eu.assina.sa.services.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping(value = "/sa")
public class AssinaSigningController {
	private static final Logger log = LoggerFactory.getLogger(AssinaSigningController.class);

	private final AssinaSigningService signingService;
	private final FileStorageService fileStorageService;

	@Autowired
    public Environment env;

	public AssinaSigningController(AssinaSigningService signingService,
								   FileStorageService fileStorageService) {
		this.signingService = signingService;
		this.fileStorageService = fileStorageService;
	}

	@GetMapping("/getOIDRedirectLink")
	public ResponseEntity<RedirectLinkResponse> getOIDRedirectLink(@RequestHeader("Authorization") String authorizationHeader){
		RedirectLinkResponse res = this.signingService.getOIDRedirectLink(authorizationHeader);
		return ResponseEntity.ok(res);
	}

	// String credential: alias of a credential
	@PostMapping("/signFile")
	public Object uploadFile(@RequestHeader("Authorization") String authorizationHeader,
							 @RequestParam("file") MultipartFile file,
							 @RequestParam("pin") String pin,
							 @RequestParam("credential") String credential,
							 @RequestParam("nonce") String nonce,
							 @RequestParam("presentationId") String presentationId
	) {
		if (!StringUtils.hasText(authorizationHeader)) {
			throw new InvalidRequestException("Expected an authorization header");
		}

		String originalFileName = fileStorageService.storeFile(file);
		String signedFileName = signingService.signFile(originalFileName, pin, credential, authorizationHeader, nonce, presentationId);

		String saUrl = env.getProperty("ASSINA_SA_BASE_URL");
		if(saUrl == null) saUrl = "http://localhost:8083";

		String fileDownloadUri = saUrl+"/sa/downloadFile/"+signedFileName;

        return new SignedFileResponse(signedFileName, fileDownloadUri, file.getContentType(), file.getSize());
	}


	@GetMapping("/downloadFile/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
		// Load file as Resource
		Resource resource = fileStorageService.loadFileAsResource(fileName);

		// Try to determine file's content type
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			log.info("Could not determine file type.");
		}

		// Fallback to the default content type if type could not be determined
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok()
					   .contentType(MediaType.parseMediaType(contentType))
					   .header(HttpHeaders.CONTENT_DISPOSITION,
							   "attachment; filename=\"" + resource.getFilename() + "\"")
					   .body(resource);
	}

}
