package group_manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "cleanings")
public class Cleaning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cleaning_date", nullable = false)
    private LocalDate cleaningDate;

    @Column(name = "territory", nullable = false)
    private String territory;

    @ManyToOne
    @JoinColumn(name = "soldier_id", nullable = false)
    private Soldier soldier;

    @Column(name = "is_manual")
    private Boolean isManual = false;
}