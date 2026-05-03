package group_manager.service;

import group_manager.entity.Cleaning;
import group_manager.repository.CleaningRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SchedulerService {

    private final CleaningService cleaningService;
    private final TelegramService telegramService;

    public SchedulerService(CleaningService cleaningService,
                            CleaningRepository cleaningRepository,
                            TelegramService telegramService) {
        this.cleaningService = cleaningService;
        this.telegramService = telegramService;
    }

    @Scheduled(cron = "0 00 20 * * *")
    public void scheduleCleaningAndNotify() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Cleaning> cleanings;
        try {
            cleanings = cleaningService.generateCleaning(tomorrow);
        } catch (RuntimeException e) {
            cleanings = cleaningService.getCleaningByDate(tomorrow);
            if (cleanings.isEmpty()) return;
        }

        // Формат повідомлення
        String dateStr = tomorrow.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        StringBuilder sb = new StringBuilder();
        sb.append("🧹 Прибирання ").append(dateStr).append("\n");

        Map<String, List<Cleaning>> byTerritory = cleanings.stream()
                .collect(Collectors.groupingBy(Cleaning::getTerritory));

        List<String> order = List.of("1 відділення", "2 відділення", "3 відділення");
        for (String territory : order) {
            List<Cleaning> group = byTerritory.getOrDefault(territory, List.of());
            String names = group.stream()
                    .map(c -> c.getSoldier().getLastName() + " " +
                            c.getSoldier().getFirstName().charAt(0) + "." +
                            c.getSoldier().getMiddleName().charAt(0) + ".")
                    .collect(Collectors.joining(", "));
            sb.append(territory).append(": ").append(names.isEmpty() ? "—" : names).append("\n");
        }

        telegramService.sendMessage(sb.toString().trim());
    }
}