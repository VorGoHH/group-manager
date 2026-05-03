package group_manager.controller;

import group_manager.entity.Duty;
import group_manager.service.DutyService;
import group_manager.service.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import group_manager.entity.Soldier;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/duties")
@RequiredArgsConstructor
public class DutyController {

    private final DutyService dutyService;
    private final TelegramService telegramService;

    @GetMapping
    public List<Duty> getDuty(@RequestParam String date) {
        return dutyService.getDutyByDate(LocalDate.parse(date));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestParam String date,
                                      @RequestParam(defaultValue = "NORMAL") String type) {
        try {
            List<Duty> duties = dutyService.generateDuty(LocalDate.parse(date), type);
            return ResponseEntity.ok(duties);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/candidates")
    public ResponseEntity<?> getCandidates(@PathVariable Long id) {
        try {
            List<Soldier> candidates = dutyService.getCandidates(id);
            return ResponseEntity.ok(candidates);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/replace/{soldierId}")
    public ResponseEntity<?> replaceWithSoldier(@PathVariable Long id,
                                                @PathVariable Long soldierId) {
        try {
            Duty duty = dutyService.replaceSoldierWith(id, soldierId);
            return ResponseEntity.ok(duty);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/{id}/replace")
    public ResponseEntity<?> replace(@PathVariable Long id) {
        try {
            Duty duty = dutyService.replaceSoldier(id);
            return ResponseEntity.ok(duty);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public void delete(@RequestParam String date) {
        dutyService.deleteDuty(LocalDate.parse(date));
    }
    @PostMapping("/report/send")
    public ResponseEntity<String> sendDutyReport(@RequestParam String date) {
        try {
            List<Duty> duties = dutyService.getDutyByDate(LocalDate.parse(date));
            if (duties.isEmpty()) return ResponseEntity.badRequest().body("Наряд не знайдено");

            String report = buildDutyReport(LocalDate.parse(date), duties);
            telegramService.sendMessage(report);
            return ResponseEntity.ok("Надіслано");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Помилка: " + e.getMessage());
        }
    }

    private String buildDutyReport(LocalDate date, List<Duty> duties) {
        String dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM"));
        StringBuilder sb = new StringBuilder();
        sb.append("📌Наряд на ").append(dateStr).append(" (241 н.г.)\n");

        // Черговий ПУ (role id=1)
        duties.stream().filter(d -> d.getRole().getId() == 1).forEach(d -> {
            sb.append("\n1. Черговий ПУ - ").append(d.getSoldier().getRank()).append(" ")
                    .append(d.getSoldier().getLastName()).append(" ")
                    .append(d.getSoldier().getFirstName()).append(" ")
                    .append(d.getSoldier().getMiddleName()).append("\n");
        });

        // Помічник ЧПУ (role id=2)
        duties.stream().filter(d -> d.getRole().getId() == 2).forEach(d -> {
            sb.append("2. Помічник ЧПУ - ").append(d.getSoldier().getRank()).append(" ")
                    .append(d.getSoldier().getLastName()).append(" ")
                    .append(d.getSoldier().getFirstName()).append(" ")
                    .append(d.getSoldier().getMiddleName()).append("\n");
        });

        // Днювальні (role id=3)
        List<Duty> dniuvalni = duties.stream().filter(d -> d.getRole().getId() == 3).toList();
        if (!dniuvalni.isEmpty()) {
            sb.append("\nДнювальні:\n");
            for (int i = 0; i < dniuvalni.size(); i++) {
                Duty d = dniuvalni.get(i);
                sb.append(i + 1).append(". ").append(d.getSoldier().getRank()).append(" ")
                        .append(d.getSoldier().getLastName()).append(" ")
                        .append(d.getSoldier().getFirstName()).append(" ")
                        .append(d.getSoldier().getMiddleName()).append("\n");
            }
        }

        // Їдальня (role id=4)
        List<Duty> yidalnia = duties.stream().filter(d -> d.getRole().getId() == 4).toList();
        if (!yidalnia.isEmpty()) {
            sb.append("\nСтолова:\n");
            for (int i = 0; i < yidalnia.size(); i++) {
                Duty d = yidalnia.get(i);
                sb.append(i + 1).append(". ").append(d.getSoldier().getRank()).append(" ")
                        .append(d.getSoldier().getLastName()).append(" ")
                        .append(d.getSoldier().getFirstName()).append(" ")
                        .append(d.getSoldier().getMiddleName()).append("\n");
            }
        }

        return sb.toString().trim();
    }
}