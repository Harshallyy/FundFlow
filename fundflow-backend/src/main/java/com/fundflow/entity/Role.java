package com.fundflow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ROLE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROLE_ID")
    private Long id;

    @Column(name = "ROLE_NAME", nullable = false, unique = true, length = 30)
    private String name; // ROLE_DONOR, ROLE_ORGANIZER, ROLE_ADMIN
}
