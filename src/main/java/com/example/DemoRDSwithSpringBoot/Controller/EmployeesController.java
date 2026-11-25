package com.example.DemoRDSwithSpringBoot.Controller;
import com.example.DemoRDSwithSpringBoot.Entity.employees;
import com.example.DemoRDSwithSpringBoot.Repository.EmployeesRepository;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeesController {

    private final EmployeesRepository repository;

    public EmployeesController(EmployeesRepository repository) {
        this.repository = repository;
    }

    // Insert employee
    @PostMapping
    public employees createEmployee(@RequestBody employees employee) {
        return repository.save(employee);
    }

    // Retrieve all employees
    @GetMapping
    public List<employees> getAllEmployees() {
        return repository.findAll();
    }

    // Retrieve employee by ID
    @GetMapping("/{id}")
    public employees getEmployeeById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found"));
    }
}