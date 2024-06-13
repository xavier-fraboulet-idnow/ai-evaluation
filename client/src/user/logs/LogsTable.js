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

import React, { Component } from "react";
import { ACCESS_TOKEN, API_BASE_URL } from "../../constants";
import axios from "axios";

import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

class LogsTable extends Component {
    constructor(props) {
        super(props);
        this.state = {
            logs: props.logs,
        };
    }

    componentDidUpdate(prevProps) {
        if (this.props.logs !== prevProps.logs) {
            this.setState({
                logs: this.props.logs,
            });
        }
    }

    render() {
        return (
            <div className="row certificateContainer1 card shadow mt-5">
                <div className="col-md-12">
                    <table className="table table-hover text-left">
                        <thead>
                            <tr>
                                <th scope="col">Time</th>
                                <th scope="col">Log Event</th>
                                <th scope="col">Status</th>
                                <th scope="col">Info</th>
                            </tr>
                        </thead>
                        <tbody>
                            {this.state.logs.map((l, index) => {
                                return (
                                    <tr key={index}>
                                        <th scope="row">{l.logTime}</th>
                                        <td>{l.eventType}</td>
                                        <td>{l.success}</td>
                                        <td>{l.info}</td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }
}

export default LogsTable;
