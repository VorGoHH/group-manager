package group_manager.service;

import group_manager.entity.Cleaning;
import group_manager.entity.Duty;
import group_manager.entity.DutyRole;
import group_manager.entity.Soldier;
import group_manager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final SoldierRepository soldierRepository;
    private final DutyRepository dutyRepository;
    private final CleaningRepository cleaningRepository;
    private final DutyRoleRepository dutyRoleRepository;
    private final WorkAssignmentRepository workAssignmentRepository;

    // Технічна дата для ручних коригувань
    private static final LocalDate MANUAL_DATE = LocalDate.of(1970, 1, 1);

    public List<Map<String, Object>> getStats() {
        List<Soldier> soldiers = soldierRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .toList();

        List<Map<String, Object>> result = new ArrayList<>();

        for (Soldier s : soldiers) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", s.getId());
            row.put("lastName", s.getLastName());
            row.put("firstName", s.getFirstName());
            row.put("rank", s.getRank());
            row.put("platoon", s.getPlatoon());

            Map<String, Long> dutyByRole = new LinkedHashMap<>();
            dutyByRole.put("Черговий ПУ", 0L);
            dutyByRole.put("Помічник ЧПУ", 0L);
            dutyByRole.put("Днювальний", 0L);
            dutyByRole.put("Їдальня", 0L);

            List<Object[]> roleStats = dutyRepository.countByRoleForSoldier(s);
            for (Object[] rs : roleStats) {
                dutyByRole.put((String) rs[0], (Long) rs[1]);
            }

            long totalDuty = dutyByRole.values().stream().mapToLong(Long::longValue).sum();

            row.put("dutyByRole", dutyByRole);
            row.put("totalDuty", totalDuty);
            row.put("totalCleaning", cleaningRepository.countBySoldier(s));
            row.put("totalWork", workAssignmentRepository.countBySoldier(s));

            result.add(row);
        }

        result.sort(Comparator.comparingInt(r -> (Integer) r.get("platoon")));
        return result;
    }

    // Додати наряд вручну
    public void addDuty(Long soldierId, String roleName) {
        Soldier soldier = soldierRepository.findById(soldierId)
                .orElseThrow(() -> new RuntimeException("Солдата не знайдено"));
        DutyRole role = dutyRoleRepository.findByName(roleName);
        if (role == null) throw new RuntimeException("Роль не знайдено: " + roleName);

        Duty duty = new Duty();
        duty.setSoldier(soldier);
        duty.setRole(role);
        duty.setDutyDate(MANUAL_DATE);
        duty.setDutyType("MANUAL");
        duty.setIsManual(true);
        dutyRepository.save(duty);
    }

    // Видалити останній запис по солдату і ролі
    public void removeDuty(Long soldierId, String roleName) {
        Soldier soldier = soldierRepository.findById(soldierId)
                .orElseThrow(() -> new RuntimeException("Солдата не знайдено"));
        DutyRole role = dutyRoleRepository.findByName(roleName);
        if (role == null) throw new RuntimeException("Роль не знайдено: " + roleName);

        dutyRepository.findTopBySoldierAndRoleOrderByIdDesc(soldier, role)
                .ifPresentOrElse(
                        dutyRepository::delete,
                        () -> { throw new RuntimeException("Нарядів для видалення немає"); }
                );
    }

    // Додати прибирання вручну
    public void addCleaning(Long soldierId) {
        Soldier soldier = soldierRepository.findById(soldierId)
                .orElseThrow(() -> new RuntimeException("Солдата не знайдено"));

        Cleaning cleaning = new Cleaning();
        cleaning.setSoldier(soldier);
        cleaning.setCleaningDate(MANUAL_DATE);
        cleaning.setTerritory("ручне");
        cleaning.setIsManual(true);
        cleaningRepository.save(cleaning);
    }

    // Видалити останній запис прибирання по солдату
    public void removeCleaning(Long soldierId) {
        Soldier soldier = soldierRepository.findById(soldierId)
                .orElseThrow(() -> new RuntimeException("Солдата не знайдено"));

        cleaningRepository.findTopBySoldierOrderByIdDesc(soldier)
                .ifPresentOrElse(
                        cleaningRepository::delete,
                        () -> { throw new RuntimeException("Прибирань для видалення немає"); }
                );
    }
    public Map<String, Object> getHistory(Long soldierId) {
        Soldier soldier = soldierRepository.findById(soldierId)
                .orElseThrow(() -> new RuntimeException("Солдата не знайдено"));

        List<Map<String, Object>> duties = dutyRepository.findBySoldier(soldier).stream()
                .sorted(Comparator.comparing(Duty::getDutyDate).reversed())
                .map(d -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("date", d.getDutyDate());
                    row.put("role", d.getRole().getName());
                    row.put("isManual", d.getIsManual());
                    return row;
                }).toList();

        List<Map<String, Object>> cleanings = cleaningRepository.findBySoldier(soldier).stream()
                .sorted(Comparator.comparing(Cleaning::getCleaningDate).reversed())
                .map(c -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("date", c.getCleaningDate());
                    row.put("territory", c.getTerritory());
                    row.put("isManual", c.getIsManual());
                    return row;
                }).toList();

        List<Map<String, Object>> works = workAssignmentRepository.findBySoldier(soldier).stream()
                .sorted(Comparator.comparing(group_manager.entity.WorkAssignment::getWorkDate).reversed())
                .map(w -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("date", w.getWorkDate());
                    row.put("workName", w.getWorkName());
                    row.put("isManual", w.getIsManual());
                    return row;
                }).toList();


        Map<String, Object> result = new LinkedHashMap<>();
        result.put("soldier", soldier.getLastName() + " " + soldier.getFirstName());
        result.put("duties", duties);
        result.put("cleanings", cleanings);
        result.put("works", works);
        return result;

    }
}