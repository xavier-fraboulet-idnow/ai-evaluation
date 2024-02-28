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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import eu.assina.rssp.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.assina.rssp.api.model.LogsUser;
import eu.assina.rssp.repository.LogsUserRepository;
import eu.assina.rssp.security.CurrentUser;
import eu.assina.rssp.security.UserPrincipal;

@RestController
@RequestMapping(value = "/logs")
public class AssinaLogsController
{
    private final LogsUserRepository repository;

    private HashMap<Integer, String> events;

    private SimpleDateFormat formatter;

	public AssinaLogsController(@Autowired final LogsUserRepository logsUserRepository)
	{
		this.repository = logsUserRepository;
        this.events = EventRepository.event();
        this.formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	}

    @GetMapping
    public ResponseEntity<List<LogsUserDTO>> getLogsOfUser(@CurrentUser UserPrincipal userPrincipal){
        String id = userPrincipal.getId();
        Optional<List<LogsUser>> logsOptional = this.repository.findByUsersID(id);

        if(logsOptional.isEmpty()){
            List<LogsUserDTO> empty = new ArrayList<>();
            return ResponseEntity.ok(empty);
        }

        List<LogsUser> logs = logsOptional.get();


        List<LogsUserDTO> returnList = new ArrayList<>();
        for (LogsUser l : logs) {
            LogsUserDTO lDTO = new LogsUserDTO();

            lDTO.setLogTime(this.formatter.format(l.getLogTime()));

            String success = l.getSuccess() == 0 ? "Failed" : "Success";
            lDTO.setSuccess(success);

            String eventType = this.events.get(l.getEventTypeID());
            lDTO.setEventType(eventType);
            returnList.add(lDTO);
        }

        return ResponseEntity.ok(returnList);
    }
}
