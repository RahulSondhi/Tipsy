package com.maroon.mixology.repository;

import java.util.Optional;

import com.maroon.mixology.entity.Role;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {

    Optional<Role> findById(String id);
    
    Role findByName(String name);
}