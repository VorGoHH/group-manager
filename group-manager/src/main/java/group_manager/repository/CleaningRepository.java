package group_manager.repository;

import group_manager.entity.Cleaning;
import group_manager.entity.Soldier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CleaningRepository extends JpaRepository<Cleaning, Long> {
    List<Cleaning> findByCleaningDate(LocalDate date);
    long countBySoldier(Soldier soldier);

    // Для видалення останнього запису
    Optional<Cleaning> findTopBySoldierOrderByIdDesc(Soldier soldier);

    List<Cleaning> findBySoldier(Soldier soldier);
}