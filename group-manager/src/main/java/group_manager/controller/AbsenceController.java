package group_manager.controller;

import group_manager.entity.Absence;
import group_manager.entity.AbsenceReason;
import group_manager.entity.Soldier;
import group_manager.repository.AbsenceRepository;
import group_manager.repository.SoldierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceRepository absenceRepository;
    private final SoldierRepository soldierRepository;

    // Отримати розхід на дату
    @GetMapping
    public Map<String, Object> getAttendance(
            @RequestParam(required = false) String date) {

        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        List<Soldier> allSoldiers = soldierRepository.findAll();
        List<Absence> absences = absenceRepository.findByAbsenceDate(targetDate);

        List<Long> absentIds = absences.stream()
                .map(a -> a.getSoldier().getId())
                .toList();

        List<Soldier> present = allSoldiers.stream()
                .filter(s -> !absentIds.contains(s.getId()))
                .toList();

        return Map.of(
                "date", targetDate.toString(),
                "total", allSoldiers.size(),
                "presentCount", present.size(),
                "present", present,
                "absences", absences
        );
    }

    // Відмітити відсутнього
    @PostMapping("/absent")
    public Absence markAbsent(@RequestBody AbsenceRequest request) {
        Soldier soldier = soldierRepository.findById(request.soldierId()).orElseThrow();

        // Якщо вже є запис на цю дату — оновлюємо
        absenceRepository.findByAbsenceDate(request.date()).stream()
                .filter(a -> a.getSoldier().getId().equals(request.soldierId()))
                .findFirst()
                .ifPresent(absenceRepository::delete);

        Absence absence = new Absence();
        absence.setSoldier(soldier);
        absence.setAbsenceDate(request.date());
        absence.setReason(request.reason());
        absence.setNote(request.note());
        return absenceRepository.save(absence);
    }

    // Повернути присутнім
    @DeleteMapping("/absent/{soldierId}")
    public void markPresent(@PathVariable Long soldierId,
                            @RequestParam(required = false) String date) {
        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        absenceRepository.findByAbsenceDate(targetDate).stream()
                .filter(a -> a.getSoldier().getId().equals(soldierId))
                .findFirst()
                .ifPresent(absenceRepository::delete);
    }

    record AbsenceRequest(Long soldierId, LocalDate date,
                          AbsenceReason reason, String note) {}
}