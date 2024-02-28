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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.stereotype.Service;

import eu.assina.rssp.api.model.LogsUser;
import eu.assina.rssp.repository.LogsUserRepository;

@Service
public class LoggerUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/assina";
    private static final String USER = "assinaadmin";
    private static final String PASSWORD = "assinaadmin";

    // LogsUserRepository repository;

    // public LoggerUtil(LogsUserRepository repository){
    //    this.repository = repository;
    // }

    public static void logs_user(int success, String usersID, int eventTypeID) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "INSERT INTO logs_user (success, usersID, eventTypeID) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, success);
                statement.setString(2, usersID);
                statement.setInt(3, eventTypeID);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        /*
        LogsUser log = new LogsUser(success, usersID, eventTypeID);
        this.repository.save(log);
        System.out.println(log.getUsersID());
        */
    }
}

