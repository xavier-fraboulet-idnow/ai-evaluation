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
import {ACCESS_TOKEN, CSC_BASE_URL} from '../../constants';
import axios from 'axios';
import LogsTable from '../logs/LogsTable';
import CreateCredentialComponent from '../certificates/CreateCertificateComponent';
import { ToastContainer } from 'react-toastify';


class Profile extends Component {
    constructor(props) {
        super(props);
        console.log(props);
        this.state = {numCredentials: 0, token: localStorage.getItem(ACCESS_TOKEN), 
                      inputAlias: '', showInputFields: false};
        
        this.createCredentialSubmit = this.createCredentialSubmit.bind(this);

        const headers = {
            'Authorization': 'Bearer '+this.state.token
        };

        axios.post(CSC_BASE_URL+'/credentials/list',{}, {
            headers: headers
        }).then(res => {
            this.setState({
                numCredentials: res.data.credentialInfo.length
            })
        }).catch(error => console.log(error));

    }

    /*handleClick(event) {
        if(this.props.currentUser.role === "ROLE_ADMIN")
            this.props.history.push("/userManagement")
        else
            this.props.history.push("/sign");
    }*/

    createCredentialSubmit(event){
        this.setState({ showInputFields: true });
    }


    /*
    {this.state.showInputFields && (
                                <div>
                                    <input
                                        name="alias"
                                        type="text"
                                        onChange={(e) => this.state.inputAlias = e.target.value}
                                        placeholder="Credential Alias"
                                    />
                                    <button onClick={this.createCredential}>Create</button>
                                </div>
                            )}     
                             */

    /*<div className="row mt-5">
                <div className="col-md-12 card shadow">
                    <table className="table table-hover">
                        <thead>
                            <tr>
                            <th scope="col">#</th>
                            <th scope="col">Time</th>
                            <th scope="col">Name</th>
                            <th scope="col">Hash</th>
                            <th scope="col">Signature Value</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                            <th scope="row">1</th>
                            <td>2024-02-12</td>
                            <td>File1</td>
                            <td>Data</td>
                            <td>Signature</td>
                            </tr>
                            <tr>
                                <th scope="row">2</th>
                                <td>2024-02-12</td>
                                <td>File2</td>
                                <td>Data</td>
                                <td>Signature</td>
                            </tr>
                            <tr>
                                <th scope="row">3</th>
                                <td>2024-02-12</td>
                                <td>File3</td>
                                <td>Data</td>
                                <td>Signature</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                        </div>*/

    render() {
        return (
        <div className="container" style={{marginTop:"3em"}}>
            <ToastContainer/>
            <div className="row mt-1 text-center">
                <div className="col-md-12">
                    <h3>Name: {this.props.currentUser.name}</h3>
                    <h4>You have {this.state.numCredentials} certificates</h4>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-12 text-center">
                    <a onClick={this.createCredentialSubmit} className="btn btn-success" id="createCert"><i className="bi bi-plus-circle"></i> 
                            Create Certificate
                        </a>  
                        {this.state.showInputFields && (
                            <CreateCredentialComponent/>
                        )} 
                    <a href="/certificates" className="btn btn-warning"><i className="bi bi-eye"></i> View Certificate</a>
                    <a href="/sign" className="btn btn-info"><i className="bi bi-vector-pen"></i> Sign Document</a>
                </div>
            </div>
            
            <LogsTable/>

        </div>
        );
    }
}

export default Profile