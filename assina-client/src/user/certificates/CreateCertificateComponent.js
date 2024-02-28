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
import {ACCESS_TOKEN, API_BASE_URL} from '../../constants';
import axios from 'axios';

import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';


class CreateCredentialComponent extends Component {
    constructor(props) {
        super(props);
        console.log(props);
        this.state = { token: localStorage.getItem(ACCESS_TOKEN), inputAlias: '', showInputFields: false};
        
        this.createCredential = this.createCredential.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
    }
    
    createCredential(event) {
        const headers = { 'Authorization': 'Bearer '+this.state.token };

        if(this.state.inputAlias === '')
            return(<div>{
                toast.warning("Please introduce an alias for the Certificate.")}</div>)
        
        const data = new FormData(); data.append('alias', this.state.inputAlias);    

        axios.post(API_BASE_URL+'/credentials', data,
        {
            headers: headers
        }).then(res => {
            toast.success("A certificate was created.");
            this.setState({
                inputAlias: '',
                showInputFields: false,
            });
            window.location.reload();
        })
        .catch(error => {
            toast.error((error && error.message) || 'Oops! Something went wrong. Please try again!');
            this.setState({
                inputAlias: '',
                showInputFields: false,
            });
            console.log(error)
        });
    }
    
    handleInputChange(event){
        this.setState({ inputAlias: event.target.value })
    }


    render() {
        return (
            <div className="input-group mb-3" style={{ paddingTop:"1em" }}>
                <input type="text" className="form-control" name="alias" placeholder="Certificate's alias" 
                    aria-label="Certificate's alias" aria-describedby="basic-addon2"
                    onChange={ this.handleInputChange }/>
                <div className="input-group-append">
                    <button onClick={ this.createCredential } className="btn btn-outline-secondary" type="button">Create</button>
                </div>
            </div>
        );
    }
}

export default CreateCredentialComponent