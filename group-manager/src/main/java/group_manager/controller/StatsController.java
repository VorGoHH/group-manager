package group_manager.controller;

import group_manager.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public List<Map<String, Object>> getStats() {
        return statsService.getStats();
    }

    // Наряди
    @PostMapping("/{soldierId}/duty")
    public ResponseEntity<?> addDuty(@PathVariable Long soldierId,
                                     @RequestParam String role) {
        try {
            statsService.addDuty(soldierId, role);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{soldierId}/duty")
    public ResponseEntity<?> removeDuty(@PathVariable Long soldierId,
                                        @RequestParam String role) {
        try {
            statsService.removeDuty(soldierId, role);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Прибирання
    @PostMapping("/{soldierId}/cleaning")
    public ResponseEntity<?> addCleaning(@PathVariable Long soldierId) {
        try {
            statsService.addCleaning(soldierId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{soldierId}/cleaning")
    public ResponseEntity<?> removeCleaning(@PathVariable Long soldierId) {
        try {
            statsService.removeCleaning(soldierId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/{soldierId}/history")
    public ResponseEntity<?> getHistory(@PathVariable Long soldierId) {
        try {
            return ResponseEntity.ok(statsService.getHistory(soldierId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}