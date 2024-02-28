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

import eu.assina.rssp.api.model.User;
import eu.assina.rssp.api.model.UserBase;
import eu.assina.rssp.api.model.UserOID4VP;
import eu.assina.rssp.api.payload.UserIdentityAvailability;
import eu.assina.rssp.api.payload.UserProfile;
import eu.assina.rssp.api.services.UserService;
import eu.assina.rssp.common.error.ApiException;
import eu.assina.rssp.common.error.AssinaError;
import eu.assina.rssp.security.CurrentUser;
import eu.assina.rssp.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class AssinaUserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('ROLE_USER')")
    public UserBase getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        String userId = userPrincipal.getId();

        // System.out.println(userId);
        System.out.println(userPrincipal.getName());

        UserBase res;
        Optional<User> user = userService.getUserById(userId);
        if(user.isPresent()){
           res = user.get();
        }
        else{
            Optional<UserOID4VP> user2 = userService.getUserOID4VPById(userId);
            if(user2.isPresent()){
                UserOID4VP u = user2.get();
                res = new UserDTO(u.getId(), u.getUsername(), userPrincipal.getName(), u.getRole());
            }
            else{
                throw new ApiException(AssinaError.UserNotFound, "Failed to find user {}", userId);
            }
        }
        return res;
    }

    @PutMapping("/users/me")
    public User updateCurrentUser(@CurrentUser UserPrincipal userPrincipal,
                                  @RequestBody User user) {
        String currentUserId = userPrincipal.getId();
        user.setId(currentUserId);
        final User updated = userService.updateUser(user);
        return updated;
    }

    @GetMapping("/user/checkUsernameAvailability")
    public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
        return new UserIdentityAvailability(userService.isUsernameAvailable(username));
    }

    @GetMapping("/user/checkEmailAvailability")
    public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
        return new UserIdentityAvailability(userService.isEmailAvailable(email));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserProfile getUserProfile(@PathVariable(value = "id") String userId) {
        UserProfile userProfile = userService.getUserProfile(userId).orElseThrow(
                () -> new ApiException(AssinaError.UserNotFound, "Failed to find user {}", userId));
        return userProfile;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<UserProfile> getUsersPaginated(Pageable pageable) {
        List<UserProfile> userProfiles = userService.getUserProfiles(pageable);
        return userProfiles;
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public User updateUser(@RequestBody User user,
                           @PathVariable(value = "userId") String userId) {
        user.setId(userId);
        final User updated = userService.updateUser(user);
        return updated;
    }

    @DeleteMapping("users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable(value = "userId") String userId)
    {
        userService.deleteUser(userId);
    }
}
