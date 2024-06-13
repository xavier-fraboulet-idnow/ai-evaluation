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

import { useContext, createContext, useState } from "react";
import { useNavigate } from "react-router-dom";

import { API_BASE_URL, ACCESS_TOKEN } from "../constants";
import axios from "axios";

export const AuthContext = createContext();

const AuthProvider = ({ children }) => {
    const [currentUserName, setCurrentUserName] = useState(
        sessionStorage.getItem("CurrentUserName") || null
    );
    const [token, setToken] = useState(
        sessionStorage.getItem(ACCESS_TOKEN) || ""
    );
    const [authenticated, setAuthenticated] = useState(
        sessionStorage.getItem("Authenticated") || false
    );

    const navigate = useNavigate();

    const loginAction = async (currentUserName, currentToken) => {
        sessionStorage.setItem(ACCESS_TOKEN, currentToken);
        sessionStorage.setItem("Authenticated", true);
        sessionStorage.setItem("CurrentUserName", currentUserName);
        setCurrentUserName(currentUserName);
        setToken(currentToken);
        setAuthenticated(true);
        navigate("/profile");
        return;
    };

    const logout = () => {
        const token = sessionStorage.getItem(ACCESS_TOKEN);
        if (token !== null) {
            const headers = {
                "Content-Type": "application/json",
                Authorization: "Bearer " + token,
            };
            axios
                .get(API_BASE_URL + "/logs/logout", {
                    headers: headers,
                })
                .then((res) => {})
                .catch((error) => {
                    console.log(error);
                });
            sessionStorage.removeItem(ACCESS_TOKEN);
        }

        sessionStorage.removeItem("Authenticated", false);
        sessionStorage.removeItem("CurrentUserName", null);
        setCurrentUserName(null);
        setToken("");
        setAuthenticated(false);
    };

    return (
        <AuthContext.Provider
            value={{
                token,
                currentUserName,
                authenticated,
                loginAction,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};

export default AuthProvider;

export const useAuth = () => {
    return useContext(AuthContext);
};
