package group_manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "absences")
public class Absence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "soldier_id", nullable = false)
    private Soldier soldier;

    @Column(name = "absence_date", nullable = false)
    private LocalDate absenceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private AbsenceReason reason;

    @Column(name = "note")
    private String note;
}