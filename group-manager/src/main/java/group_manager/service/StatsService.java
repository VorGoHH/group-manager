package group_manager.service;

import group_manager.entity.Soldier;
import group_manager.repository.CleaningRepository;
import group_manager.repository.DutyRepository;
import group_manager.repository.SoldierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final SoldierRepository soldierRepository;
    private final DutyRepository dutyRepository;
    private final CleaningRepository cleaningRepository;

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

            // Наряди по ролях
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

            result.add(row);
        }

        // Сортування: відділення → прізвище
        result.sort(Comparator
                .comparingInt(r -> (Integer) r.get("platoon"))
        );

        return result;
    }
}