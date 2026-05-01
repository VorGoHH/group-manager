package group_manager.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "soldiers")
public class Soldier {

    @Column(name = "exclude_from_duty")
    private Boolean excludeFromDuty = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "rank")
    private String rank;

    @Column(name = "platoon")
    private Integer platoon;

    @Column(name = "is_commander")
    private Boolean isCommander = false;

    @Column(name = "is_active")
    private Boolean isActive = true;
}