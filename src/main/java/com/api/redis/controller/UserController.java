package com.api.redis.controller;

import com.api.redis.dao.UserDao;
import com.api.redis.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("users")
public class UserController {


    @Autowired
    private UserDao userDao;

    //create User
    @PostMapping
    public User createUser(@RequestBody User user)
    {
        user.setUserId(UUID.randomUUID().toString());
        return userDao.save(user);
    }

    //get Single user
    @GetMapping("/{userId}")
    public User getUser(@PathVariable("userId") String userId){
        return userDao.get(userId);
    }

    //Find all
    @GetMapping()
    public Map<Object,Object> getAll(){
        return userDao.findAll();
    }

    //delete User
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable String userId){
        userDao.delete(userId);
    }
}
