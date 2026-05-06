package group_manager.controller;

import group_manager.entity.Soldier;
import group_manager.entity.WorkAssignment;
import group_manager.service.WorkAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import group_manager.service.TelegramService;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/work")
@RequiredArgsConstructor
public class WorkAssignmentController {

    private final WorkAssignmentService workService;
    private final TelegramService telegramService;

    @GetMapping
    public List<WorkAssignment> getByDate(@RequestParam String date) {
        return workService.getByDate(LocalDate.parse(date));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestParam String date,
                                      @RequestParam String workName,
                                      @RequestParam int count) {
        try {
            return ResponseEntity.ok(workService.generate(LocalDate.parse(date), workName, count));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public void delete(@RequestParam String date, @RequestParam String workName) {
        workService.delete(LocalDate.parse(date), workName);
    }

    @GetMapping("/{id}/candidates")
    public ResponseEntity<?> getCandidates(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(workService.getCandidates(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/replace/{soldierId}")
    public ResponseEntity<?> replace(@PathVariable Long id, @PathVariable Long soldierId) {
        try {
            return ResponseEntity.ok(workService.replaceWith(id, soldierId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/report/send")
    public ResponseEntity<String> sendReport(@RequestParam String date) {
        try {
            List<WorkAssignment> assignments = workService.getByDate(LocalDate.parse(date));
            if (assignments.isEmpty()) return ResponseEntity.badRequest().body("Робіт не знайдено");

            Map<String, List<WorkAssignment>> grouped = assignments.stream()
                    .collect(Collectors.groupingBy(WorkAssignment::getWorkName));

            StringBuilder sb = new StringBuilder();
            grouped.forEach((name, items) -> {
                sb.append(name).append("\n");
                items.forEach(a -> {
                    String fi = a.getSoldier().getFirstName().charAt(0) + "." +
                            a.getSoldier().getMiddleName().charAt(0) + ".";
                    sb.append(a.getSoldier().getLastName()).append(" ").append(fi).append("\n");
                });
                sb.append("\n");
            });

            telegramService.sendMessage(sb.toString().trim());
            return ResponseEntity.ok("Надіслано");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Помилка: " + e.getMessage());
        }
    }
}