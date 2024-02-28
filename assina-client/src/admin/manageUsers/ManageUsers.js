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

import React, { Component, useState } from 'react';
import './ManageUsers.css';
import axios from 'axios';
import {ACCESS_TOKEN, API_BASE_URL, CSC_BASE_URL} from '../../constants';


class ManageUsers extends Component {
    constructor(props) {
        super(props);
        console.log(props);
        this.state = {users: []};

        const token = localStorage.getItem(ACCESS_TOKEN);

        const headers = {
	        'Authorization': 'Bearer '+token
        };

        axios.get(API_BASE_URL+'/users', {
            headers: headers
        }).then(res => {
            this.setState({
                users: res.data
            })
            console.log(this.state.users)
        }).catch(error => console.log(error));

    }

    render() {

        return (
            <div className="manageUsers-container">
                <div className="container">
                    <table id="users">
                        <tr>
                            <th>User ID</th>
                            <th>Full Name</th>
                            <th>Number of Credentials</th>
                            <th>Date joined</th>
                        </tr>
                        {this.state.users.map(user => {
                            return (
                                    <tr>
                                        <td>{user.id}</td>
                                        <td>{user.name}</td>
                                        <td>{user.credentialCount}</td>
                                        <td>{user.joinedAt}</td>
                                    </tr>
                                    )
                        })}
                    </table>
                </div>
            </div>
        );
    }
}

export default ManageUsers
