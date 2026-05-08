package com.example.EmployeeManagementSystem.service;
import com.example.EmployeeManagementSystem.entity.Employee;
import com.example.EmployeeManagementSystem.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    // Constructor injection (for testability)
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get all employees
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Get employee by ID
     */
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    /**
     * Create new employee
     * Throws exception if email already exists
     */
    public Employee createEmployee(Employee employee) {
        if (employee.getName() == null || employee.getName().isBlank()) {
            throw new IllegalArgumentException("Employee name cannot be blank");
        }
        if (employee.getEmail() == null || employee.getEmail().isBlank()) {
            throw new IllegalArgumentException("Employee email cannot be blank");
        }
        if (employee.getSalary() <= 0) {
            throw new IllegalArgumentException("Salary must be greater than zero");
        }
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + employee.getEmail());
        }
        return employeeRepository.save(employee);
    }

    /**
     * Update existing employee
     */
    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        existing.setName(updatedEmployee.getName());
        existing.setDepartment(updatedEmployee.getDepartment());
        existing.setSalary(updatedEmployee.getSalary());
        return employeeRepository.save(existing);
    }

    /**
     * Delete employee by ID
     */
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    /**
     * Get employees by department
     */
    public List<Employee> getEmployeesByDepartment(String department) {
        if (department == null || department.isBlank()) {
            throw new IllegalArgumentException("Department cannot be blank");
        }
        return employeeRepository.findByDepartment(department);
    }

    /**
     * Calculate average salary of all employees
     */
    public double calculateAverageSalary() {
        List<Employee> employees = employeeRepository.findAll();
        if (employees.isEmpty()) {
            return 0.0;
        }
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    /**
     * Apply salary hike by percentage
     */
    public Employee applySalaryHike(Long id, double hikePercent) {
        if (hikePercent <= 0 || hikePercent > 100) {
            throw new IllegalArgumentException("Hike percent must be between 0 and 100");
        }
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        double newSalary = employee.getSalary() + (employee.getSalary() * hikePercent / 100);
        employee.setSalary(newSalary);
        return employeeRepository.save(employee);
    }
}
