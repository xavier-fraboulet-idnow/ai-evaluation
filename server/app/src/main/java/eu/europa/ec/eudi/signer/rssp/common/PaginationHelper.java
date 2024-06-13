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

package eu.europa.ec.eudi.signer.rssp.common;

import eu.europa.ec.eudi.signer.rssp.common.config.CSCProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Base64;

@Component
public class PaginationHelper {

    private static final Logger log = LoggerFactory.getLogger(PaginationHelper.class);

    private final CSCProperties cscProperties;

    public PaginationHelper(@Autowired CSCProperties cscProperties) {
        this.cscProperties = cscProperties;
    }

    public String pageableToNextPageToken(Pageable pageable) {
        // TODO make sure this is empty when there is no next page
        Pageable nextPage = pageable.next();
        return pageableToString(nextPage);
    }

    public Pageable pageTokenToPageable(String pageToken, int requestedPageSize) {
        int pageSize;
        if (requestedPageSize > 0) {
            int maxAllowed = cscProperties.getApi().getMaxPageSize();
            if (requestedPageSize > maxAllowed) {
                log.warn("Requested page size too large: limiting page size to {}", maxAllowed);
                pageSize = maxAllowed;
            } else {
                pageSize = requestedPageSize;
            }
        } else {
            // use default page size
            pageSize = cscProperties.getApi().getPageSize();
        }

        Pageable pageable;
        if (StringUtils.hasText(pageToken)) {
            pageable = stringToPageable(pageToken, pageSize);
        } else {
            pageable = PageRequest.of(0, pageSize);
        }
        return pageable;
    }

    // unmarshall pageable from string
    private Pageable stringToPageable(String pageToken, int pageSize) {
        String plainText = new String(Base64.getDecoder().decode(pageToken));
        final String[] parts = plainText.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid pageToken: " + plainText);
        }
        int tokenPageNumber = Integer.parseInt(parts[0]);
        int tokenPageSize = Integer.parseInt(parts[1]);
        int usePageSize = Math.min(pageSize, tokenPageSize);
        return PageRequest.of(tokenPageNumber, usePageSize);
    }

    // marshall pageable into opaque string
    private String pageableToString(Pageable pageable) {
        // consider adding sorting here
        String plainText = String.format("%d,%d", pageable.getPageNumber(), pageable.getPageSize());
        // nextPageToken must be opaque so base64 it
        final String pageToken = Base64.getEncoder().encodeToString(plainText.getBytes());
        return pageToken;
    }
}
