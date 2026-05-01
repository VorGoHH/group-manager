package group_manager.controller;

import group_manager.entity.Duty;
import group_manager.service.DutyService;
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
}