package group_manager.controller;

import group_manager.entity.Soldier;
import group_manager.repository.SoldierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/soldiers")
@RequiredArgsConstructor
public class SoldierController {

    private final SoldierRepository soldierRepository;

    @GetMapping
    public List<Soldier> getAll() {
        return soldierRepository.findAll();
    }

    @GetMapping("/{id}")
    public Soldier getById(@PathVariable Long id) {
        return soldierRepository.findById(id).orElseThrow();
    }

    @PutMapping("/{id}/status")
    public Soldier updateStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        Soldier soldier = soldierRepository.findById(id).orElseThrow();
        soldier.setIsActive(isActive);
        return soldierRepository.save(soldier);
    }
}