package com.daynix.app.reference.repository;

import com.daynix.app.auth.entity.Role;
import com.daynix.app.reference.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByCode(Role code);
}
