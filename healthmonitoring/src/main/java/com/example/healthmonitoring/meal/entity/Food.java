package com.example.healthmonitoring.meal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "foods")
public class Food {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String name;

    @Column(nullable=false)
    private String serving;       // vd: "1 chén (150g)"

    @Column(nullable=false)
    private int kcalPerServing;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getServing() { return serving; }
    public void setServing(String serving) { this.serving = serving; }
    public int getKcalPerServing() { return kcalPerServing; }
    public void setKcalPerServing(int kcalPerServing) { this.kcalPerServing = kcalPerServing; }
}
