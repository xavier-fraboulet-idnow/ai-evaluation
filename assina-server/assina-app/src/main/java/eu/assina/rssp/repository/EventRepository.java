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

package eu.assina.rssp.repository;

import java.sql.*;
import java.util.HashMap;

public class EventRepository {

    private static final String URL = "jdbc:mysql://localhost:3306/assina";
    private static final String USER = "assinaadmin";
    private static final String PASSWORD = "assinaadmin";


    public static HashMap<Integer, String> event(){
        HashMap<Integer,String> result = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM event";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Execute the query
                try (ResultSet resultSet = statement.executeQuery()) {
                    // Process the ResultSet here
                    while (resultSet.next()) {
                        System.out.println(resultSet);
                        int id = resultSet.getInt("eventTypeID");
                        String eventName = resultSet.getString("eventName");
                        System.out.println("eventTypeID: " + id + ", eventName: " + eventName);
                        result.put(id, eventName);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

}
