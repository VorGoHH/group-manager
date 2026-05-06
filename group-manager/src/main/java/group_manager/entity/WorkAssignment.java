package group_manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "work_assignments")
public class WorkAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "soldier_id", nullable = false)
    private Soldier soldier;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "work_name", nullable = false)
    private String workName;

    @Column(name = "is_manual")
    private Boolean isManual = false;
}