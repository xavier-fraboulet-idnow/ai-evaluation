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

package eu.europa.ec.eudi.signer.rssp.api.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class LoggerUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/assina?useSSL=false&serverTimezone=UTC&useLegacyDatetimeCode=false";
    public static String desc = "";

    private static final Logger logger = LogManager.getLogger(LoggerUtil.class);

    public static void logs_user(String dbUsername, String dbPassword, int success, String usersID, int eventTypeID,
            String info) {

        try (Connection connection = DriverManager.getConnection(URL, dbUsername, dbPassword)) {
            String sql = "INSERT INTO logs_user (success, usersID, eventTypeID, info) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, success);
                statement.setString(2, usersID);
                statement.setInt(3, eventTypeID);
                statement.setString(4, info);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(e);
        }
    }
}
