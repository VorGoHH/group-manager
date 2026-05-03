package group_manager.controller;

import group_manager.service.ReportService;
import group_manager.service.TelegramService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance/report")
public class ReportController {

    private final ReportService reportService;
    private final TelegramService telegramService;

    public ReportController(ReportService reportService, TelegramService telegramService) {
        this.reportService = reportService;
        this.telegramService = telegramService;
    }

    @GetMapping
    public ResponseEntity<String> getReport(@RequestParam String date) {
        String report = reportService.buildReport(LocalDate.parse(date));
        return ResponseEntity.ok(report);
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendReport(@RequestParam String date) {
        try {
            String report = reportService.buildReport(LocalDate.parse(date));
            telegramService.sendMessage(report);
            return ResponseEntity.ok("Надіслано");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Помилка: " + e.getMessage());
        }
    }
}