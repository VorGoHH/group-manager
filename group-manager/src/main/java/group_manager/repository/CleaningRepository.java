package group_manager.repository;

import group_manager.entity.Cleaning;
import group_manager.entity.Soldier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CleaningRepository extends JpaRepository<Cleaning, Long> {
    List<Cleaning> findByCleaningDate(LocalDate date);
    long countBySoldier(Soldier soldier);
}