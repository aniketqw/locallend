package com.locallend.locallend.controller;

import com.locallend.locallend.model.User;
import com.locallend.locallend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 
* REST controller to manage user in Locallend
**/

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	/**
	* Get all users.
	* @return list of users
	**/

	@GetMapping
	public List<User> getAllUsers(){
		return userRepository.findAll()
	}

	/**
	* Add a new user
	* @param user - user info in JSON
	* @return saved user
	**/
	@postMapping
	public User addUser (@RequestBody User user){
		return userRepository.save(user);
	}
}


