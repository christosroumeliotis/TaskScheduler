package com.task.scheduler.core.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "system_variables")
public class SystemVariables {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "variableName", unique = true)
    private String variableName;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Column(name = "string_value")
    private String stringValue;

    @Column(name = "integer_value")
    private Integer integerValue;

    @Column(name = "double_value")
    private Double doubleValue;
}
