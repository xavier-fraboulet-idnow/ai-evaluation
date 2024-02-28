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

import React, { Component } from 'react';
import axios from 'axios';
import {ACCESS_TOKEN, CSC_BASE_URL, API_BASE_URL} from '../../constants';
import CreateCredentialComponent from './CreateCertificateComponent';
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

class Certificates extends Component {
    constructor(props) {
        super(props);

        
        this.state = {file: '', msg: '', pin: '', credentials: [], token: '', selectedCredential: '', answerFromOID4VP: false};
        this.state.token = localStorage.getItem(ACCESS_TOKEN);
        const headers = {
	        'Authorization': 'Bearer '+this.state.token
        };

        axios.post(CSC_BASE_URL + '/credentials/list',
        {},
        {
            headers: headers
        }).then(res=>{
            console.log(res.data);
        if(res.data.credentialInfo.length === 0) {
            console.log("NO CREDENTIALS");
        }
        else {
            this.setState({
                credentials: res.data.credentialInfo
            })
            console.log(this.state.credentials);
        }
        }).catch(error=>{
            console.log(error);
        })

        this.createCredentialSubmit = this.createCredentialSubmit.bind(this);
        this.deleteCredential = this.deleteCredential.bind(this);
    }

    createCredentialSubmit(event){
        this.setState({ showInputFields: true });
    }

    deleteCredential(event, credential){
        event.preventDefault();
        console.log(credential);

        const headers = {'Authorization': 'Bearer '+this.state.token};
        const url = API_BASE_URL+'/credentials/'+credential.alias;
        console.log(url);
        axios.delete(
            url, 
            { headers: headers },
            {}
        ).then(
            res => {
                toast.success("The Credential "+credential.alias+" was deleted.");
                console.log(res);
                window.location.reload();
            }
        ).catch(
            err => {
                console.log(err);
                toast.error("The Credential "+credential.alias+" could not be deleted.")
            }
        )
    }


    render() {
        return (
            <div className="container" style={{marginTop:"1.5em"}}>
                <ToastContainer/>
                <div className="row mt-1">
                    <div className="col-sm-4">
                        <strong >Certificates</strong>
                    </div>
                    <div className="col-md-8">
                            <a onClick={this.createCredentialSubmit} className="btn btn-success float-end"><i className="bi bi-plus-circle">
                                </i> Create new Certificate
                            </a>
                            { this.state.showInputFields && (
                                <CreateCredentialComponent/>
                            )}
                        </div>
                </div>
                <div className="row certificateContainer card shadow mt-3">
                    <div className="col-md-12">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>Certificate Name</th>
                                    <th>Subject DN</th>
                                    <th>Issuer DN</th>
                                    <th>Valid From</th>
                                    <th>Valid To</th>
                                </tr>
                            </thead>
                            <tbody>
                                {this.state.credentials.map((c, index) => (
                                    <tr key={index}>
                                        <td>{c.alias}</td>
                                        <td>{c.subjectDN}</td>
                                        <td>{c.issuerDN}</td>
                                        <td>{c.validFrom}</td>
                                        <td>{c.validTo}</td>
                                        <td>
                                            <button onClick={e => this.deleteCredential(e, c)} type="button" className="btn btn-danger">
                                                <i className="bi bi-trash"></i>
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        );
    }
}

export default Certificates
