package group_manager.service;

import group_manager.entity.Cleaning;
import group_manager.entity.Soldier;
import group_manager.repository.AbsenceRepository;
import group_manager.repository.CleaningRepository;
import group_manager.repository.SoldierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CleaningService {

    private final CleaningRepository cleaningRepository;
    private final SoldierRepository soldierRepository;
    private final AbsenceRepository absenceRepository;

    public List<Cleaning> generateCleaning(LocalDate date) {

        List<Cleaning> existing = cleaningRepository.findByCleaningDate(date);
        if (!existing.isEmpty()) {
            throw new RuntimeException("Прибирання на " + date + " вже існує");
        }

        Set<Long> absentIds = absenceRepository.findByAbsenceDate(date).stream()
                .map(a -> a.getSoldier().getId())
                .collect(Collectors.toSet());

        List<Cleaning> result = new ArrayList<>();

        // По 2 особи з кожного відділення (1, 2, 3)
        for (int platoon = 1; platoon <= 3; platoon++) {
            final int p = platoon;
            List<Soldier> candidates = soldierRepository.findAll().stream()
                    .filter(s -> s.getPlatoon() == p)
                    .filter(s -> !s.getIsCommander())
                    .filter(s -> !Boolean.TRUE.equals(s.getExcludeFromDuty()))
                    .filter(s -> !absentIds.contains(s.getId()))
                    .toList();

            // Вибрати 2 осіб які найменше разів прибирали
            List<Soldier> picked = pickLeast(candidates, 2);
            for (Soldier s : picked) {
                Cleaning c = new Cleaning();
                c.setCleaningDate(date);
                c.setSoldier(s);
                c.setTerritory(p + " відділення");
                c.setIsManual(false);
                result.add(c);
            }
        }

        return cleaningRepository.saveAll(result);
    }

    public List<Soldier> getCandidates(Long cleaningId) {
        Cleaning cleaning = cleaningRepository.findById(cleaningId)
                .orElseThrow(() -> new RuntimeException("Запис не знайдено"));

        LocalDate date = cleaning.getCleaningDate();
        int platoon = cleaning.getSoldier().getPlatoon();

        Set<Long> alreadyCleaning = cleaningRepository.findByCleaningDate(date).stream()
                .filter(c -> !c.getId().equals(cleaningId))
                .map(c -> c.getSoldier().getId())
                .collect(Collectors.toSet());

        Set<Long> absentIds = absenceRepository.findByAbsenceDate(date).stream()
                .map(a -> a.getSoldier().getId())
                .collect(Collectors.toSet());

        return soldierRepository.findAll().stream()
                .filter(s -> s.getPlatoon() == platoon)
                .filter(s -> !s.getIsCommander())
                .filter(s -> !Boolean.TRUE.equals(s.getExcludeFromDuty()))
                .filter(s -> !absentIds.contains(s.getId()))
                .filter(s -> !alreadyCleaning.contains(s.getId()))
                .toList();
    }

    public Cleaning replaceWith(Long cleaningId, Long soldierId) {
        Cleaning cleaning = cleaningRepository.findById(cleaningId)
                .orElseThrow(() -> new RuntimeException("Запис не знайдено"));
        Soldier soldier = soldierRepository.findById(soldierId)
                .orElseThrow(() -> new RuntimeException("Солдата не знайдено"));
        cleaning.setSoldier(soldier);
        cleaning.setIsManual(true);
        return cleaningRepository.save(cleaning);
    }

    private List<Soldier> pickLeast(List<Soldier> candidates, int count) {
        return candidates.stream()
                .sorted(Comparator.comparingLong((Soldier s) ->
                                cleaningRepository.countBySoldier(s))
                        .thenComparing(s -> cleaningRepository
                                .findTopBySoldierOrderByIdDesc(s)
                                .map(c -> c.getCleaningDate())
                                .orElse(LocalDate.of(2000, 1, 1))))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Cleaning> getCleaningByDate(LocalDate date) {
        return cleaningRepository.findByCleaningDate(date);
    }

    public void deleteCleaning(LocalDate date) {
        cleaningRepository.deleteAll(cleaningRepository.findByCleaningDate(date));
    }
}