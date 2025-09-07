package tn.esprit.exam.service;

import tn.esprit.exam.entity.User;

import java.util.List;
import java.util.UUID;

public interface IUserService {

    List<User> retrieveAllUsers();
    User retrieveUser(UUID userId);
    User addUser(User user);
    User modifyUser(User user);
    void removeUser(UUID userId);
}
