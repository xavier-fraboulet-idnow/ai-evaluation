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

package eu.assina.rssp.common.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="certificate_chain")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 2000)
    private String certificate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="assina_credential_id", nullable=false)
    private AssinaCredential credential;

    public Certificate(){}

    public Certificate(long id, String certificate){
        this.id = id;
        this.certificate = certificate;
    }

    public String getCertificate(){
        return this.certificate;
    }

    public void setCertificate(String certificate){
        this.certificate = certificate;
    }

    public AssinaCredential getAssinaCredential(){
        return this.credential;
    }

    public void setAssinaCertificate(AssinaCredential credential){
        this.credential = credential;
    }
}
