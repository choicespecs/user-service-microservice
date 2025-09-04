
package com.choicespecs.e_commerce_proj_user_service.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;

/**
 * Repository for {@link UserEntity}
 */
public interface UserRepository extends CrudRepository<UserEntity, String> {

    List<UserEntity> findByDeletedFalse();

    UserEntity findByEmail(String email);
    UserEntity findByUsernameIgnoreCase(String username);
}
