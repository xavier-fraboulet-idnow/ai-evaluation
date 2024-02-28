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

package eu.assina.rssp.api.controller;

import eu.assina.rssp.api.model.UserBase;

public class UserDTO implements UserBase{
    private String username;
    private String name;
    private String role;
    private String id;
    private String password;

    public UserDTO(String id, String username, String name, String role){
        this.id = id;
        this.username = username;
        this.name = name;
        this.role = role;
        this.password = null;
    }

    public String getId(){
        return this.id;
    }

    public String getUsername(){
        return this.username;
    }

    public String getRole(){
        return this.role;
    }

    public String getPassword(){
        return this.password;
    }

    public String getName(){
        return this.name;
    }
}