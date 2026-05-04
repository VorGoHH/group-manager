package group_manager.controller;

import group_manager.entity.Cleaning;
import group_manager.service.CleaningService;
import group_manager.service.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cleaning")
@RequiredArgsConstructor
public class CleaningController {

    private final CleaningService cleaningService;
    private final TelegramService telegramService;

    @GetMapping
    public List<Cleaning> getCleaning(@RequestParam String date) {
        return cleaningService.getCleaningByDate(LocalDate.parse(date));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestParam String date) {
        try {
            List<Cleaning> cleaning = cleaningService.generateCleaning(LocalDate.parse(date));
            return ResponseEntity.ok(cleaning);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public void delete(@RequestParam String date) {
        cleaningService.deleteCleaning(LocalDate.parse(date));
    }

    @GetMapping("/{id}/candidates")
    public ResponseEntity<?> getCandidates(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(cleaningService.getCandidates(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/replace/{soldierId}")
    public ResponseEntity<?> replace(@PathVariable Long id, @PathVariable Long soldierId) {
        try {
            return ResponseEntity.ok(cleaningService.replaceWith(id, soldierId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/report/send")
    public ResponseEntity<String> sendReport(@RequestParam String date) {
        try {
            List<Cleaning> cleanings = cleaningService.getCleaningByDate(LocalDate.parse(date));
            if (cleanings.isEmpty()) return ResponseEntity.badRequest().body("Прибирання не знайдено");

            String dateStr = LocalDate.parse(date).format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            StringBuilder sb = new StringBuilder();
            sb.append("🧹 Прибирання ").append(dateStr).append("\n");

            Map<String, List<Cleaning>> byTerritory = cleanings.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Cleaning::getTerritory));

            for (String territory : List.of("1 відділення", "2 відділення", "3 відділення")) {
                List<Cleaning> group = byTerritory.getOrDefault(territory, List.of());
                String names = group.stream()
                        .map(c -> c.getSoldier().getRank() + " " +
                                c.getSoldier().getLastName() + " " +
                                c.getSoldier().getFirstName().charAt(0) + "." +
                                c.getSoldier().getMiddleName().charAt(0) + ".")
                        .collect(java.util.stream.Collectors.joining(", "));
                sb.append(territory).append(": ").append(names.isEmpty() ? "—" : names).append("\n");
            }

            telegramService.sendMessage(sb.toString().trim());
            return ResponseEntity.ok("Надіслано");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Помилка: " + e.getMessage());
        }
    }
}