package com.collabpulse.userservice.service;

import com.collabpulse.userservice.model.User;
import com.collabpulse.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        boolean exist = userRepository.findByUsername(user.getUsername()).isPresent();
        if (exist) {
            throw new IllegalArgumentException("Bu kullanıcı adı zaten sistemde kayıtlı!");
        }
        return userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUserById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı! ID: " + id));
    }
}
