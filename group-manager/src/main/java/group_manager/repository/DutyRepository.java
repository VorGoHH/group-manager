package group_manager.repository;

import group_manager.entity.Duty;
import group_manager.entity.DutyRole;
import group_manager.entity.Soldier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DutyRepository extends JpaRepository<Duty, Long> {
    List<Duty> findByDutyDate(LocalDate date);
    long countBySoldierAndRole(Soldier soldier, DutyRole role);
    long countBySoldier(Soldier soldier);

    // Для статистики — розбивка по ролях
    @Query("SELECT d.role.name, COUNT(d) FROM Duty d WHERE d.soldier = :soldier GROUP BY d.role.name")
    List<Object[]> countByRoleForSoldier(Soldier soldier);

    // Для видалення останнього запису
    Optional<Duty> findTopBySoldierAndRoleOrderByIdDesc(Soldier soldier, DutyRole role);

    List<Duty> findBySoldier(Soldier soldier);
}