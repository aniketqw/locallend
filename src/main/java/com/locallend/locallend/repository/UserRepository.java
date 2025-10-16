package com.locallend.locallend.repository;
import com.locallend.locallend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/** 
repository for the User entity . Provide CRUD operation ;
**/

public interface UserRepository extends MongoRepository<User, String>{}

