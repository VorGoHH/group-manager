package group_manager.service;

import group_manager.entity.Absence;
import group_manager.entity.AbsenceReason;
import group_manager.repository.AbsenceRepository;
import group_manager.repository.SoldierRepository;
import group_manager.entity.Soldier;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final AbsenceRepository absenceRepository;
    private final SoldierRepository soldierRepository;

    public ReportService(AbsenceRepository absenceRepository, SoldierRepository soldierRepository) {
        this.absenceRepository = absenceRepository;
        this.soldierRepository = soldierRepository;
    }

    public String buildReport(LocalDate date) {
        List<Soldier> allActive = soldierRepository.findAll().stream()
                .filter(s -> s.getIsActive())
                .collect(Collectors.toList());

        List<Absence> absences = absenceRepository.findByAbsenceDate(date);

        int total = allActive.size();
        int absentCount = absences.size();
        int present = total - absentCount;

        String dateStr = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        StringBuilder sb = new StringBuilder();
        sb.append("241 н.г. ").append(dateStr).append("\n");
        sb.append("З/с - ").append(total).append("\n");
        sb.append("В/н - ").append(present).append("\n");

        // Порядок причин
        List<AbsenceReason> order = List.of(
                AbsenceReason.ON_DUTY,
                AbsenceReason.SICK,
                AbsenceReason.EXCUSED,
                AbsenceReason.BUSINESS_TRIP,
                AbsenceReason.INDIVIDUAL,
                AbsenceReason.ILLEGAL
        );

        Map<AbsenceReason, String> labels = Map.of(
                AbsenceReason.ON_DUTY, "Н",
                AbsenceReason.SICK, "Хв",
                AbsenceReason.EXCUSED, "Зв",
                AbsenceReason.BUSINESS_TRIP, "Відр",
                AbsenceReason.INDIVIDUAL, "І/з",
                AbsenceReason.ILLEGAL, "Н/з"
        );

        Map<AbsenceReason, List<Absence>> grouped = absences.stream()
                .collect(Collectors.groupingBy(a -> a.getReason()));

        for (AbsenceReason reason : order) {
            List<Absence> group = grouped.getOrDefault(reason, List.of());
            if (group.isEmpty()) continue;

            String names = group.stream()
                    .map(a -> {
                        Soldier s = a.getSoldier();
                        String last = s.getLastName();
                        String fi = s.getFirstName().substring(0, 1) + "." +
                                s.getMiddleName().substring(0, 1) + ".";
                        return last + " " + fi;
                    })
                    .collect(Collectors.joining(", "));

            sb.append(labels.get(reason)).append(" - ")
                    .append(group.size()).append(" (").append(names).append(")\n");
        }

        return sb.toString().trim();
    }
}