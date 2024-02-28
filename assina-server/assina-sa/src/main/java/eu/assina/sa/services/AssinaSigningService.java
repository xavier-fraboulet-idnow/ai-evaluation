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

package eu.assina.sa.services;

import eu.assina.csc.payload.RedirectLinkResponse;
import eu.assina.sa.client.AssinaRSSPClient;
import eu.assina.sa.client.ClientContext;
import eu.assina.sa.config.RSSPClientConfig;
import eu.assina.sa.error.InternalErrorException;
import eu.assina.sa.pdf.PdfSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

@Service
public class AssinaSigningService {

	private static final Logger log = LoggerFactory.getLogger(AssinaSigningService.class);
	final AssinaRSSPClient rsspClient;
	PdfSupport pdfSupport;
	private FileStorageService fileStorageService;

	public AssinaSigningService(RSSPClientConfig rsspClientConfig,
								FileStorageService fileStorageService) {
		rsspClient = new AssinaRSSPClient(rsspClientConfig);
		pdfSupport = new PdfSupport(rsspClient);
		this.fileStorageService = fileStorageService;
	}

	public RedirectLinkResponse getOIDRedirectLink(String authorizationHeader){
		ClientContext context = new ClientContext();
		context.setAuthorizationHeader(authorizationHeader);
		rsspClient.setContext(context);
		return pdfSupport.getOIDRedirectLink();
	}


	public String signFile(String originalFileName, String PIN, String credentialAlias, String authorizationHeader, String nonce, String presentation_id) {
		String signedFileName;
		try {
			Path originalFilePath = fileStorageService.getFilePath(originalFileName);
			String baseName = originalFilePath.getFileName().toString();
			signedFileName = StringUtils.stripFilenameExtension(baseName) + "_signed.pdf";
			Path signedFilePath = fileStorageService.newFilePath(signedFileName);
			ClientContext context = new ClientContext();
			context.setAuthorizationHeader(authorizationHeader);
			context.setPIN(PIN);
			context.setCredentialID(credentialAlias);
			context.setNonce(nonce);
			context.setPresentation_id(presentation_id);
			rsspClient.setContext(context);
			pdfSupport.signDetached(originalFilePath.toFile(), signedFilePath.toFile());
		} catch (IOException | NoSuchAlgorithmException e) {
			log.error(e.getMessage());
			log.error("Internal error in Signing Application", e);
			throw new InternalErrorException("Internal error in Signing Application", e);
		} finally {
			rsspClient.setContext(null); // clear it just in case it gets resused
		}
		return signedFileName;
	}
}
