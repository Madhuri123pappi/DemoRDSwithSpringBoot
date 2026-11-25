package com.example.DemoRDSwithSpringBoot.Repository;
import com.example.DemoRDSwithSpringBoot.Entity.employees;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeesRepository
        extends JpaRepository<employees, Long> {
}
