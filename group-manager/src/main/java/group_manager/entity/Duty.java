package group_manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(
        name = "duties",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_duty_soldier_date",
                        columnNames = {"duty_date", "soldier_id"}
                )
        }
)
public class Duty {

    @Column(name = "duty_type")
    private String dutyType;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "duty_date", nullable = false)
    private LocalDate dutyDate;

    @ManyToOne
    @JoinColumn(name = "soldier_id", nullable = false)
    private Soldier soldier;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private DutyRole role;

    @Column(name = "is_manual")
    private Boolean isManual = false;
}