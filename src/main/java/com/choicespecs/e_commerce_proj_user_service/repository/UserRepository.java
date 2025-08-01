/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;

/**
 *
 * @author christopherlee
 */
public interface UserRepository extends CrudRepository<UserEntity, String> {

    List<UserEntity> findByDeletedFalse();

    UserEntity findByEmail(String email);
}
