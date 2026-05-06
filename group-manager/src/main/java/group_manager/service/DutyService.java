package group_manager.service;

import group_manager.entity.*;
import group_manager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import group_manager.entity.AbsenceReason;
import group_manager.entity.Absence;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DutyService {

    private final SoldierRepository soldierRepository;
    private final DutyRepository dutyRepository;
    private final DutyRoleRepository dutyRoleRepository;
    private final AbsenceRepository absenceRepository;

    // Генерація наряду на дату
    public List<Duty> generateDuty(LocalDate date, String type) {

        List<Duty> existing = dutyRepository.findByDutyDate(date);
        if (!existing.isEmpty()) {
            throw new RuntimeException("Наряд на " + date + " вже існує");
        }

        Set<Long> absentIds = absenceRepository.findByAbsenceDate(date)
                .stream().map(a -> a.getSoldier().getId())
                .collect(Collectors.toSet());

        List<Soldier> allPresent = soldierRepository.findAll().stream()
                .filter(s -> !absentIds.contains(s.getId()))
                .toList();

        DutyRole roleCommander = dutyRoleRepository.findByName("Черговий ПУ");
        DutyRole roleAssistant = dutyRoleRepository.findByName("Помічник ЧПУ");
        DutyRole roleDuty = dutyRoleRepository.findByName("Днювальний");
        DutyRole roleMess = dutyRoleRepository.findByName("Їдальня");

        List<Duty> result = new ArrayList<>();
        Set<Long> assigned = new HashSet<>();

        boolean withPu = "WITH_PU".equals(type);
        List<String> commanderLastNames = List.of("Вашуленко", "Бондаренко", "Кіндратяк", "Рязанов");
        List<String> assistantLastNames = List.of("Богаченко", "Лобач", "Радківський", "Ходоровський");
        List<String> dutyOnlyLastNames = List.of("Базелюк", "Вітвіцький", "Демченко", "Зубаков", "Кінах",
                "Корчинський", "Кравченко", "Кульбако", "Марченко", "Милетич",
                "Мясоєдов", "Снігир", "Сехін", "Тимашов", "Харченко");
        List<String> messLastNames = List.of("Базелюк", "Богаченко", "Вітвіцький", "Демченко", "Зубаков",
                "Кінах", "Корчинський", "Кравченко", "Кульбако", "Лобач", "Марченко", "Милетич",
                "Мясоєдов", "Радківський", "Снігир", "Сехін", "Тимашов", "Харченко", "Ходоровський");


        if (withPu) {
            // Черговий ПУ
            List<Soldier> commanders = allPresent.stream()
                    .filter(s -> commanderLastNames.contains(s.getLastName()))
                    .toList();
            Soldier commander = pickLeast(commanders, roleCommander);
            if (commander != null) {
                result.add(createDuty(date, commander, roleCommander, type));
                assigned.add(commander.getId());
            }

            // Помічник ЧПУ
            List<Soldier> assistants = allPresent.stream()
                    .filter(s -> assistantLastNames.contains(s.getLastName()))
                    .filter(s -> !assigned.contains(s.getId()))
                    .toList();
            Soldier assistant = pickLeast(assistants, roleAssistant);
            if (assistant != null) {
                result.add(createDuty(date, assistant, roleAssistant, type));
                assigned.add(assistant.getId());
            }
        }

        // Днювальні
        List<Soldier> dutyPool = allPresent.stream()
                .filter(s -> dutyOnlyLastNames.contains(s.getLastName()))
                .filter(s -> !assigned.contains(s.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < 2; i++) {
            Soldier picked = pickLeast(dutyPool, roleDuty);
            if (picked != null) {
                result.add(createDuty(date, picked, roleDuty, type));
                assigned.add(picked.getId());
                dutyPool.remove(picked);
            }
        }

        // Їдальня
        int messCount = withPu ? 2 : 1;
        List<Soldier> messPool = allPresent.stream()
                .filter(s -> messLastNames.contains(s.getLastName()))
                .filter(s -> !assigned.contains(s.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < messCount; i++) {
            Soldier mess = pickLeast(messPool, roleMess);
            if (mess != null) {
                result.add(createDuty(date, mess, roleMess, type));
                assigned.add(mess.getId());
                messPool.remove(mess);
            }
        }

        // Рядові для днювальних та їдальні
        List<Soldier> regular = allPresent.stream()
                .filter(s -> !s.getIsCommander())
                .filter(s -> !Boolean.TRUE.equals(s.getExcludeFromDuty()))
                .filter(s -> !assigned.contains(s.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        List<Duty> saved = dutyRepository.saveAll(result);
        saved.forEach(d -> createOnDutyAbsence(d.getSoldier(), date));
        return saved;
    }

    public Duty replaceSoldier(Long dutyId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new RuntimeException("Наряд не знайдено"));

        DutyRole role = duty.getRole();
        LocalDate date = duty.getDutyDate();

        Set<Long> alreadyInDuty = dutyRepository.findByDutyDate(date).stream()
                .filter(d -> !d.getId().equals(dutyId))
                .map(d -> d.getSoldier().getId())
                .collect(Collectors.toSet());

        Set<Long> absentIds = absenceRepository.findByAbsenceDate(date).stream()
                .map(a -> a.getSoldier().getId())
                .collect(Collectors.toSet());

        List<Soldier> candidates;

        if (role.getName().equals("Черговий ПУ")) {
            candidates = soldierRepository.findAll().stream()
                    .filter(Soldier::getIsCommander)
                    .filter(s -> !absentIds.contains(s.getId()))
                    .filter(s -> !alreadyInDuty.contains(s.getId()))
                    .toList();
        } else if (role.getName().equals("Помічник ЧПУ")) {
            List<String> assistantLastNames = List.of("Радківський", "Ходоровський", "Богаченко", "Лобач");
            candidates = soldierRepository.findAll().stream()
                    .filter(s -> assistantLastNames.contains(s.getLastName()))
                    .filter(s -> !absentIds.contains(s.getId()))
                    .filter(s -> !alreadyInDuty.contains(s.getId()))
                    .toList();
        } else {
            candidates = soldierRepository.findAll().stream()
                    .filter(s -> !s.getIsCommander())
                    .filter(s -> !Boolean.TRUE.equals(s.getExcludeFromDuty()))
                    .filter(s -> !absentIds.contains(s.getId()))
                    .filter(s -> !alreadyInDuty.contains(s.getId()))
                    .toList();
        }

        if (candidates.isEmpty()) {
            throw new RuntimeException("Немає доступних кандидатів для заміни");
        }

        Soldier oldSoldier = duty.getSoldier();
        Soldier newSoldier = pickLeast(candidates, role);
        duty.setSoldier(newSoldier);
        duty.setIsManual(true);
        Duty saved = dutyRepository.save(duty);
        removeOnDutyAbsence(oldSoldier, date);
        createOnDutyAbsence(newSoldier, date);
        return saved;
    }

    // Вибрати того хто найменше разів чергував у цій ролі
    private Soldier pickLeast(List<Soldier> candidates, DutyRole role) {
        if (candidates.isEmpty()) return null;

        // 1. Мінімальна загальна кількість нарядів
        long minCount = candidates.stream()
                .mapToLong(s -> dutyRepository.countBySoldier(s))
                .min()
                .orElse(0);

        // 2. Ті хто має мінімум
        List<Soldier> leastUsed = candidates.stream()
                .filter(s -> dutyRepository.countBySoldier(s) == minCount)
                .collect(Collectors.toList());

        // 3. З них — хто найдавніше взагалі чергував
        return leastUsed.stream()
                .min(Comparator.comparing(s -> dutyRepository.findBySoldier(s).stream()
                        .filter(d -> !d.getDutyDate().equals(LocalDate.of(1970, 1, 1)))
                        .map(Duty::getDutyDate)
                        .max(Comparator.naturalOrder())
                        .orElse(LocalDate.of(2000, 1, 1))))
                .orElse(null);
    }



    private Duty createDuty(LocalDate date, Soldier soldier, DutyRole role, String type) {
        Duty duty = new Duty();
        duty.setDutyDate(date);
        duty.setSoldier(soldier);
        duty.setRole(role);
        duty.setIsManual(false);
        duty.setDutyType(type);
        return duty;
    }

    public List<Duty> getDutyByDate(LocalDate date) {
        return dutyRepository.findByDutyDate(date).stream()
                .sorted(Comparator.comparingLong(d -> d.getRole().getId()))
                .toList();
    }

    public List<Soldier> getCandidates(Long dutyId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new RuntimeException("Наряд не знайдено"));


        DutyRole role = duty.getRole();
        LocalDate date = duty.getDutyDate();

        Set<Long> alreadyInDuty = dutyRepository.findByDutyDate(date).stream()
                .filter(d -> !d.getId().equals(dutyId))
                .map(d -> d.getSoldier().getId())
                .collect(Collectors.toSet());

        Set<Long> absentIds = absenceRepository.findByAbsenceDate(date).stream()
                .map(a -> a.getSoldier().getId())
                .collect(Collectors.toSet());

        if (role.getName().equals("Черговий ПУ")) {
            return soldierRepository.findAll().stream()
                    .filter(s -> s.getIsCommander() || s.getLastName().equals("Каніболоцький"))
                    .filter(s -> !absentIds.contains(s.getId()))
                    .filter(s -> !alreadyInDuty.contains(s.getId()))
                    .toList();
        } else if (role.getName().equals("Помічник ЧПУ")) {
            List<String> assistantLastNames = List.of("Радківський", "Ходоровський", "Богаченко", "Лобач");
            return soldierRepository.findAll().stream()
                    .filter(s -> assistantLastNames.contains(s.getLastName()))
                    .filter(s -> !absentIds.contains(s.getId()))
                    .filter(s -> !alreadyInDuty.contains(s.getId()))
                    .toList();
        } else {
            return soldierRepository.findAll().stream()
                    .filter(s -> !s.getIsCommander())
                    .filter(s -> !Boolean.TRUE.equals(s.getExcludeFromDuty()))
                    .filter(s -> !absentIds.contains(s.getId()))
                    .filter(s -> !alreadyInDuty.contains(s.getId()))
                    .toList();
        }
    }

    public Duty replaceSoldierWith(Long dutyId, Long soldierId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new RuntimeException("Наряд не знайдено"));
        Soldier newSoldier = soldierRepository.findById(soldierId)
                .orElseThrow(() -> new RuntimeException("Солдата не знайдено"));
        Soldier oldSoldier = duty.getSoldier();
        duty.setSoldier(newSoldier);
        duty.setIsManual(true);
        Duty saved = dutyRepository.save(duty);
        removeOnDutyAbsence(oldSoldier, duty.getDutyDate());
        createOnDutyAbsence(newSoldier, duty.getDutyDate());
        return saved;
    }

    public void deleteDuty(LocalDate date) {
        List<Duty> duties = dutyRepository.findByDutyDate(date);
        duties.forEach(d -> removeOnDutyAbsence(d.getSoldier(), date));
        dutyRepository.deleteAll(duties);
    }

    private void createOnDutyAbsence(Soldier soldier, LocalDate date) {
        if (!absenceRepository.existsBySoldierIdAndAbsenceDate(soldier.getId(), date)) {
            Absence absence = new Absence();
            absence.setSoldier(soldier);
            absence.setAbsenceDate(date);
            absence.setReason(AbsenceReason.ON_DUTY);
            absence.setNote("Наряд");
            absenceRepository.save(absence);
        }
    }

    private void removeOnDutyAbsence(Soldier soldier, LocalDate date) {
        absenceRepository.findByAbsenceDate(date).stream()
                .filter(a -> a.getSoldier().getId().equals(soldier.getId()))
                .filter(a -> AbsenceReason.ON_DUTY.equals(a.getReason()))
                .forEach(absenceRepository::delete);
    }
}