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

package eu.europa.ec.eudi.signer.rssp.util;

import java.text.SimpleDateFormat;

import java.util.Date;

public class CertificateUtils {
    
    /**
     * Formats a date as a string according to x509 RFC 5280
     * Assumes the given date is UTC
     * 
     * @param date
     * @return null if the date is null otherwise formatted as YYYMMMDDHHMMSSZ
     */
    public static String x509Date(Date date) {
        if (date == null)
            return null;

        SimpleDateFormat X509DateFormat = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
        return X509DateFormat.format(date);
    }
}
