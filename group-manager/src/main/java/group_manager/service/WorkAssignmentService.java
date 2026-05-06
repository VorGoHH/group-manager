package group_manager.service;

import group_manager.entity.*;
import group_manager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkAssignmentService {

    private final WorkAssignmentRepository workRepository;
    private final SoldierRepository soldierRepository;
    private final AbsenceRepository absenceRepository;
    private final DutyRepository dutyRepository;

    public List<WorkAssignment> generate(LocalDate date, String workName, int count) {

        Set<Long> absentIds = absenceRepository.findByAbsenceDate(date).stream()
                .map(a -> a.getSoldier().getId())
                .collect(Collectors.toSet());

        Set<Long> onDutyIds = dutyRepository.findByDutyDate(date).stream()
                .map(d -> d.getSoldier().getId())
                .collect(Collectors.toSet());

        List<Soldier> candidates = soldierRepository.findAll().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getExcludeFromDuty()))
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .filter(s -> !Boolean.TRUE.equals(s.getIsCommander()))
                .filter(s -> !absentIds.contains(s.getId()))
                .filter(s -> !onDutyIds.contains(s.getId()))
                .collect(Collectors.toList());

        List<Soldier> picked = pickLeast(candidates, count);

        List<WorkAssignment> result = picked.stream().map(s -> {
            WorkAssignment w = new WorkAssignment();
            w.setSoldier(s);
            w.setWorkDate(date);
            w.setWorkName(workName);
            w.setIsManual(false);
            return w;
        }).collect(Collectors.toList());

        return workRepository.saveAll(result);
    }

    private List<Soldier> pickLeast(List<Soldier> candidates, int count) {
        long minCount = candidates.stream()
                .mapToLong(workRepository::countBySoldier)
                .min().orElse(0);

        List<Soldier> leastUsed = candidates.stream()
                .filter(s -> workRepository.countBySoldier(s) == minCount)
                .collect(Collectors.toList());

        if (leastUsed.size() >= count) {
            return leastUsed.stream()
                    .sorted(Comparator.comparing(s -> workRepository.findTopBySoldierOrderByIdDesc(s)
                            .map(WorkAssignment::getWorkDate)
                            .orElse(LocalDate.of(2000, 1, 1))))
                    .limit(count)
                    .collect(Collectors.toList());
        }

        List<Soldier> result = new ArrayList<>(leastUsed);
        List<Soldier> rest = candidates.stream()
                .filter(s -> !leastUsed.contains(s))
                .sorted(Comparator.comparingLong(workRepository::countBySoldier))
                .collect(Collectors.toList());
        result.addAll(rest.subList(0, Math.min(count - result.size(), rest.size())));
        return result;
    }

    public List<WorkAssignment> getByDate(LocalDate date) {
        return workRepository.findByWorkDate(date);
    }

    public void delete(LocalDate date, String workName) {
        workRepository.deleteByWorkDateAndWorkName(date, workName);
    }

    public WorkAssignment replaceWith(Long id, Long soldierId) {
        WorkAssignment w = workRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Запис не знайдено"));
        Soldier soldier = soldierRepository.findById(soldierId)
                .orElseThrow(() -> new RuntimeException("Солдата не знайдено"));
        w.setSoldier(soldier);
        w.setIsManual(true);
        return workRepository.save(w);
    }

    public List<Soldier> getCandidates(Long id) {
        WorkAssignment w = workRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Запис не знайдено"));
        LocalDate date = w.getWorkDate();

        Set<Long> alreadyAssigned = workRepository.findByWorkDate(date).stream()
                .filter(a -> !a.getId().equals(id))
                .map(a -> a.getSoldier().getId())
                .collect(Collectors.toSet());

        Set<Long> absentIds = absenceRepository.findByAbsenceDate(date).stream()
                .map(a -> a.getSoldier().getId())
                .collect(Collectors.toSet());

        Set<Long> onDutyIds = dutyRepository.findByDutyDate(date).stream()
                .map(d -> d.getSoldier().getId())
                .collect(Collectors.toSet());

        return soldierRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .filter(s -> !Boolean.TRUE.equals(s.getIsCommander()))
                .filter(s -> !absentIds.contains(s.getId()))
                .filter(s -> !onDutyIds.contains(s.getId()))
                .filter(s -> !alreadyAssigned.contains(s.getId()))
                .collect(Collectors.toList());
    }

    public List<WorkAssignment> getBySoldier(Soldier soldier) {
        return workRepository.findBySoldier(soldier);
    }
}