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

package eu.europa.ec.eudi.signer.rssp.ejbca;

import java.util.List;

public class Pkcs10EnrollResponseBody {
    private String certificate;
    private String serial_number;
    private String response_format;
    private List<String> certificate_chain = null;

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public String getResponse_format() {
        return response_format;
    }

    public void setResponse_format(String response_format) {
        this.response_format = response_format;
    }

    public List<String> getCertificate_chain() {
        return certificate_chain;
    }

    public void setCertificate_chain(List<String> certificate_chain) {
        this.certificate_chain = certificate_chain;
    }
}
