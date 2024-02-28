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
import './Edit.css';
import { Link, Redirect } from 'react-router-dom'
import axios from 'axios';
import { ACCESS_TOKEN, API_BASE_URL } from '../../constants';

class Edit extends Component {
    render() {

        return (
            <main>
            <div className="container" style={{marginTop:"3em"}}>
                <div className="row mt-1">
                    <div className="col-md-12 text-center">
                        <h3>View Profile</h3>
                    </div>
                </div>
                <div className="row mt-3">
                    <div className="col-md-12 card shadow p-3">
                        <div className="row">
                            <div className="col-md-12">
                                <span>Account Details</span>
                                
                            </div>
                        </div>
                        <EditForm {...this.props} />
                    </div>
                </div>
            </div>
        </main>
        );
    }
}

class EditForm extends Component {
    constructor(props) {
        super(props);
        this.state = {
            name: this.props.currentUser.name,
            email: this.props.currentUser.email,
            password: '',
            pin: ''
        }
        this.handleSubmit = this.handleSubmit.bind(this);
    }


    handleSubmit(event) {
        event.preventDefault();
        window.location = '/profile';
    }

    render() {

        const provider = this.props.currentUser.provider;

        return (
            <form onSubmit={this.handleSubmit}>
                {
                    (this.props.currentUser.provider == "local") ? (
                        <div className="row mt-3">
                            <div className="col-md-12">
                                <div className="form-floating">
                                    <input type="text" name="name"
                                        className="form-control" placeholder={this.props.currentUser.name}
                                        value={this.state.name} disabled/>
                                </div>
                            </div>
                        </div>
                    ) : null
                }
                {
                    (this.props.currentUser.provider == "local") ? (
                        <div className="row mt-3">
                            <div className="col-md-12">
                                <div className="form-floating">
                                <input type="email" name="email"
                                    className="form-control" placeholder={this.props.currentUser.email}
                                    value={this.state.email} disabled/>
                                </div>
                            </div>
                        </div>
                    ) : null
                }
                {
                    (this.props.currentUser.provider == "local") ? (
                        <div class="row mt-3">
                            <div class="col-md-12">
                                <div class="form-floating">
                                    <input type="password" name="password"
                                        className="form-control" placeholder="Enter a password only IF you want to change it"
                                        value={this.state.password} disabled/>
                                </div>
                            </div>
                        </div>
                    ) : null
                }
                    <div class="row mt-3">
                        <div class="col-md-12 text-center">
                            <button type="submit" class="btn btn-danger">Return <i class="bt bi-house-door"></i></button>  
                            
                        </div>
                    </div>
            </form>

        );
    }
}

export default Edit
