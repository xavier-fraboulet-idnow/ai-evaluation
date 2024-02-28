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

import {
    ASSINA_RSSP_BASE_URL,
    API_BASE_URL,
    ACCESS_TOKEN,
} from '../constants';

const request = (options) => {
    const headers = new Headers({
        'Content-Type': 'application/json',
    })

    if(localStorage.getItem(ACCESS_TOKEN)) {
        headers.append('Authorization', 'Bearer ' + localStorage.getItem(ACCESS_TOKEN))
    }

    const defaults = {headers: headers};
    options = Object.assign({}, defaults, options);

    return fetch(options.url, options)
    .then(response =>
        response.json().then(json => {
            if(!response.ok) {
                return Promise.reject(json);
            }
            return json;
        })
    );
};

export function getCurrentUser() {
    if(!localStorage.getItem(ACCESS_TOKEN)) {
        return Promise.reject("No access token set.");
    }

    return request({
        url: API_BASE_URL + "/user/me",
        method: 'GET'
    });
}

export function login(loginRequest) {
    return request({
        url: ASSINA_RSSP_BASE_URL + "/auth/login",
        method: 'POST',
        body: JSON.stringify(loginRequest)
    });
}

export function logout() {
    return request({
        url: ASSINA_RSSP_BASE_URL + "/auth/logout",
        method: 'GET'
    });
}

export function log_sign_file(aux) {
    if(aux === 1) {
        return request({
            url: ASSINA_RSSP_BASE_URL + "/auth/sign_log",
            method: 'GET'
        });
    }
    if(aux === 0) {
        return request({
            url: ASSINA_RSSP_BASE_URL + "/auth/sign_log_err",
            method: 'GET'
        });
    }
    
}

export function log_download_file() {
    return request({
        url: ASSINA_RSSP_BASE_URL + "/auth/download_log",
        method: 'GET'
    });
}

export function signup(signupRequest) {
    return request({
        url: ASSINA_RSSP_BASE_URL + "/auth/signup",
        method: 'POST',
        body: JSON.stringify(signupRequest)
    });
}

/*
export function createCredential(token) {
    const headers = {
        'Authorization': 'Bearer '+token
    };

    return axios.post(API_BASE_URL+'/credentials',
            {
                alias: 'temp'
            },
            {
                headers: headers
            }).then(res => {
                console.log(res);
            }).catch(error => {
                console.log(error);
            });
}*/
