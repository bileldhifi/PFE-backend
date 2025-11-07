package tn.esprit.exam.service;

import tn.esprit.exam.dto.UserRequest;
import tn.esprit.exam.dto.UserResponse;
import tn.esprit.exam.entity.User;

import java.util.List;
import java.util.UUID;

public interface IUserService {

    List<UserResponse> retrieveAllUsers();
    UserResponse retrieveUser(UUID userId);
    UserResponse addUser(UserRequest user);
    UserResponse modifyUser(UUID userId, UserRequest user);
    void removeUser(UUID userId);
    UUID getUserIdByEmail(String email);
}
