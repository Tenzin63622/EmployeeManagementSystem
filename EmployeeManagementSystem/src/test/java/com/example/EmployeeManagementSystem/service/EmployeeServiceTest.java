package com.example.EmployeeManagementSystem.service;
import com.example.EmployeeManagementSystem.entity.Employee;
import com.example.EmployeeManagementSystem.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Unit Tests")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleEmployee = new Employee("Tenzin Khedup", "tenzin@example.com", "Engineering", 75000.0);
        sampleEmployee.setId(1L);
    }

    // ===================== GET ALL EMPLOYEES =====================

    @Test
    @DisplayName("✅ Should return all employees successfully")
    void testGetAllEmployees_Positive() {
        List<Employee> employees = Arrays.asList(
                sampleEmployee,
                new Employee("Jane Smith", "jane@example.com", "HR", 60000.0)
        );
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("✅ Should return empty list when no employees exist")
    void testGetAllEmployees_EmptyList() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Employee> result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ===================== GET EMPLOYEE BY ID =====================

    @Test
    @DisplayName("✅ Should return employee when valid ID provided")
    void testGetEmployeeById_Positive() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        Optional<Employee> result = employeeService.getEmployeeById(1L);

        assertTrue(result.isPresent());
        assertEquals("Tenzin Khedup", result.get().getName());
    }

    @Test
    @DisplayName("Should return empty Optional when employee not found")
    void testGetEmployeeById_NotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Employee> result = employeeService.getEmployeeById(99L);

        assertFalse(result.isPresent());
    }

    //  CREATE EMPLOYEE 

    @Test
    @DisplayName("✅ Should create employee successfully")
    void testCreateEmployee_Positive() {
        when(employeeRepository.existsByEmail("tenzin@example.com")).thenReturn(false);
        when(employeeRepository.save(sampleEmployee)).thenReturn(sampleEmployee);

        Employee result = employeeService.createEmployee(sampleEmployee);

        assertNotNull(result);
        assertEquals("Tenzin Khedup", result.getName());
        assertEquals("Engineering", result.getDepartment());
        verify(employeeRepository, times(1)).save(sampleEmployee);
    }

    @Test
    @DisplayName("❌ Should throw exception when name is blank")
    void testCreateEmployee_BlankName() {
        Employee emp = new Employee("", "test@example.com", "IT", 50000.0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(emp)
        );
        assertEquals("Employee name cannot be blank", ex.getMessage());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("❌ Should throw exception when email is blank")
    void testCreateEmployee_BlankEmail() {
        Employee emp = new Employee("John", "", "IT", 50000.0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(emp)
        );
        assertEquals("Employee email cannot be blank", ex.getMessage());
    }

    @Test
    @DisplayName("❌ Should throw exception when salary is zero or negative")
    void testCreateEmployee_InvalidSalary() {
        Employee emp = new Employee("John", "john@example.com", "IT", -5000.0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(emp)
        );
        assertEquals("Salary must be greater than zero", ex.getMessage());
    }

    @Test
    @DisplayName("❌ Should throw exception when email already exists")
    void testCreateEmployee_DuplicateEmail() {
        when(employeeRepository.existsByEmail("tenzin@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(sampleEmployee)
        );
        assertTrue(ex.getMessage().contains("Email already exists"));
        verify(employeeRepository, never()).save(any());
    }

    // ===================== UPDATE EMPLOYEE =====================

    @Test
    @DisplayName("✅ Should update employee successfully")
    void testUpdateEmployee_Positive() {
        Employee updated = new Employee("Tenzin Updated", "tenzin@example.com", "Management", 90000.0);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

        Employee result = employeeService.updateEmployee(1L, updated);

        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("❌ Should throw exception when updating non-existent employee")
    void testUpdateEmployee_NotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        Employee updated = new Employee("Ghost", "ghost@example.com", "Nowhere", 0);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> employeeService.updateEmployee(99L, updated)
        );
        assertTrue(ex.getMessage().contains("Employee not found with id: 99"));
    }

    // ===================== DELETE EMPLOYEE =====================

    @Test
    @DisplayName("✅ Should delete employee successfully")
    void testDeleteEmployee_Positive() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);

        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("❌ Should throw exception when deleting non-existent employee")
    void testDeleteEmployee_NotFound() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> employeeService.deleteEmployee(99L)
        );
        assertTrue(ex.getMessage().contains("Employee not found with id: 99"));
        verify(employeeRepository, never()).deleteById(any());
    }

    // ===================== GET BY DEPARTMENT =====================

    @Test
    @DisplayName("✅ Should return employees by department")
    void testGetEmployeesByDepartment_Positive() {
        when(employeeRepository.findByDepartment("Engineering"))
                .thenReturn(List.of(sampleEmployee));

        List<Employee> result = employeeService.getEmployeesByDepartment("Engineering");

        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0).getDepartment());
    }

    @Test
    @DisplayName("❌ Should throw exception when department is blank")
    void testGetEmployeesByDepartment_BlankDept() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployeesByDepartment("")
        );
        assertEquals("Department cannot be blank", ex.getMessage());
    }

    // ===================== AVERAGE SALARY =====================

    @Test
    @DisplayName("✅ Should calculate correct average salary")
    void testCalculateAverageSalary_Positive() {
        List<Employee> employees = Arrays.asList(
                new Employee("A", "a@x.com", "IT", 60000),
                new Employee("B", "b@x.com", "IT", 80000),
                new Employee("C", "c@x.com", "IT", 100000)
        );
        when(employeeRepository.findAll()).thenReturn(employees);

        double avg = employeeService.calculateAverageSalary();

        assertEquals(80000.0, avg, 0.01);
    }

    @Test
    @DisplayName("✅ Should return 0.0 when no employees exist")
    void testCalculateAverageSalary_NoEmployees() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        double avg = employeeService.calculateAverageSalary();

        assertEquals(0.0, avg);
    }

    // ===================== SALARY HIKE =====================

    @Test
    @DisplayName("✅ Should apply salary hike correctly")
    void testApplySalaryHike_Positive() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(employeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Employee result = employeeService.applySalaryHike(1L, 10.0);

        // 75000 + 10% = 82500
        assertEquals(82500.0, result.getSalary(), 0.01);
    }

    @Test
    @DisplayName("❌ Should throw exception when hike percent is zero")
    void testApplySalaryHike_ZeroPercent() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.applySalaryHike(1L, 0.0)
        );
        assertTrue(ex.getMessage().contains("Hike percent must be between 0 and 100"));
    }

    @Test
    @DisplayName("❌ Should throw exception when hike percent exceeds 100")
    void testApplySalaryHike_ExceedsLimit() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.applySalaryHike(1L, 150.0)
        );
        assertTrue(ex.getMessage().contains("Hike percent must be between 0 and 100"));
    }

    @Test
    @DisplayName("❌ Should throw exception when employee not found for hike")
    void testApplySalaryHike_EmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> employeeService.applySalaryHike(99L, 10.0)
        );
        assertTrue(ex.getMessage().contains("Employee not found with id: 99"));
    }
}
