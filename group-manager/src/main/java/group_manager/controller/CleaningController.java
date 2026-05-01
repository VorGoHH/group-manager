package group_manager.controller;

import group_manager.entity.Cleaning;
import group_manager.service.CleaningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cleaning")
@RequiredArgsConstructor
public class CleaningController {

    private final CleaningService cleaningService;

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
}