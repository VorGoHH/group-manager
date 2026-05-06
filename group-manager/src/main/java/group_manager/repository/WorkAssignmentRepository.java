package group_manager.repository;

import group_manager.entity.Soldier;
import group_manager.entity.WorkAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface WorkAssignmentRepository extends JpaRepository<WorkAssignment, Long> {
    List<WorkAssignment> findByWorkDate(LocalDate date);
    List<WorkAssignment> findBySoldier(Soldier soldier);
    long countBySoldier(Soldier soldier);
    Optional<WorkAssignment> findTopBySoldierOrderByIdDesc(Soldier soldier);
    List<WorkAssignment> findByWorkDateAndWorkName(LocalDate date, String workName);

    @Transactional
    void deleteByWorkDateAndWorkName(LocalDate date, String workName);
}