package tn.esprit.exam.service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService{
    UserRepository userRepository;

    @Override
    public List<User> retrieveAllUsers() {
        List<User> list = userRepository.findAll();
        log.info("Total users: " + list.size());
        list.forEach(u -> log.info("User: " + u));
        return list;
    }

    @Override
    public User retrieveUser(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User addUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User modifyUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void removeUser(UUID userId) {
        userRepository.deleteById(userId);
    }
}
