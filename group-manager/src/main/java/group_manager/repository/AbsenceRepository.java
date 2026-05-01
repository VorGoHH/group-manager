package group_manager.repository;

import group_manager.entity.Absence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long> {
    List<Absence> findByAbsenceDate(LocalDate date);
    boolean existsBySoldierIdAndAbsenceDate(Long soldierId, LocalDate date);
}