package group_manager.repository;

import group_manager.entity.Duty;
import group_manager.entity.DutyRole;
import group_manager.entity.Soldier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DutyRepository extends JpaRepository<Duty, Long> {
    List<Duty> findByDutyDate(LocalDate date);
    long countBySoldierAndRole(Soldier soldier, DutyRole role);

    // Загальна кількість нарядів по солдату
    long countBySoldier(Soldier soldier);

    // Кількість нарядів по солдату в кожній ролі
    @Query("SELECT d.role.name, COUNT(d) FROM Duty d WHERE d.soldier = :soldier GROUP BY d.role.name")
    List<Object[]> countByRoleForSoldier(Soldier soldier);
}