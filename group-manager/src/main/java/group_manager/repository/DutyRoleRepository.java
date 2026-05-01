package group_manager.repository;

import group_manager.entity.DutyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DutyRoleRepository extends JpaRepository<DutyRole, Long> {
    DutyRole findByName(String name);
}