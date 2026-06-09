package org.example.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Builder
public class Project {
    private Long id;
    @Setter
    private String name;
    @Setter
    private String description;
    @Setter
    private LocalDateTime lastUpdated;
    @Setter
    private String managerEmail;
    @Setter
    @Builder.Default
    private Set<String> memberEmails = new HashSet<>();
    @Setter
    private List<Task> tasks;

    public List<SalaryEntry> computeSalaries(Map<String, Double> hourlyRates) {
        return memberEmails.stream().map(email -> {
            double totalHours = tasks.stream()
                    .filter(t -> email.equals(t.getAssignedToEmail()))
                    .mapToDouble(t -> t.getHoursWorked() != null ? t.getHoursWorked() : 0.0)
                    .sum();
            double rate = hourlyRates.getOrDefault(email, 0.0);
            return new SalaryEntry(email, totalHours, rate, totalHours * rate);
        }).toList();
    }
}
