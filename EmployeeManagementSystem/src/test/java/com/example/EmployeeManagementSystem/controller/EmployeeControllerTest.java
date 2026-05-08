
package com.example.EmployeeManagementSystem.controller;
import com.example.EmployeeManagementSystem.entity.Employee;
import com.example.EmployeeManagementSystem.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@DisplayName("EmployeeController Integration Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleEmployee = new Employee("Tenzin Khedup", "tenzin@example.com", "Engineering", 75000.0);
        sampleEmployee.setId(1L);
    }

    // ===================== GET ALL =====================

    @Test
    @DisplayName("✅ GET /api/employees - returns 200 with list")
    void testGetAllEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(sampleEmployee));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tenzin Khedup"))
                .andExpect(jsonPath("$[0].department").value("Engineering"));
    }

    @Test
    @DisplayName("✅ GET /api/employees - returns empty list")
    void testGetAllEmployees_Empty() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ===================== GET BY ID =====================

    @Test
    @DisplayName("✅ GET /api/employees/{id} - returns employee")
    void testGetEmployeeById_Found() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(sampleEmployee));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tenzin Khedup"))
                .andExpect(jsonPath("$.salary").value(75000.0));
    }

    @Test
    @DisplayName("❌ GET /api/employees/{id} - returns 404 when not found")
    void testGetEmployeeById_NotFound() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    // ===================== CREATE =====================

    @Test
    @DisplayName("✅ POST /api/employees - creates employee and returns 201")
    void testCreateEmployee_Success() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(sampleEmployee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEmployee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Tenzin Khedup"));
    }

    @Test
    @DisplayName("❌ POST /api/employees - returns 400 on duplicate email")
    void testCreateEmployee_DuplicateEmail() throws Exception {
        when(employeeService.createEmployee(any())).thenThrow(
                new IllegalArgumentException("Email already exists: tenzin@example.com")
        );

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEmployee)))
                .andExpect(status().isBadRequest());
    }

    // ===================== UPDATE =====================

    @Test
    @DisplayName("✅ PUT /api/employees/{id} - updates successfully")
    void testUpdateEmployee_Success() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(sampleEmployee);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEmployee)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("❌ PUT /api/employees/{id} - returns 404 when not found")
    void testUpdateEmployee_NotFound() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any())).thenThrow(
                new RuntimeException("Employee not found with id: 99")
        );

        mockMvc.perform(put("/api/employees/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEmployee)))
                .andExpect(status().isNotFound());
    }

    // ===================== DELETE =====================

    @Test
    @DisplayName("✅ DELETE /api/employees/{id} - deletes successfully")
    void testDeleteEmployee_Success() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Employee deleted successfully"));
    }

    @Test
    @DisplayName("❌ DELETE /api/employees/{id} - returns 404 when not found")
    void testDeleteEmployee_NotFound() throws Exception {
        doThrow(new RuntimeException("Employee not found with id: 99"))
                .when(employeeService).deleteEmployee(99L);

        mockMvc.perform(delete("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    // ===================== AVERAGE SALARY =====================

    @Test
    @DisplayName("✅ GET /api/employees/average-salary - returns correct value")
    void testGetAverageSalary() throws Exception {
        when(employeeService.calculateAverageSalary()).thenReturn(80000.0);

        mockMvc.perform(get("/api/employees/average-salary"))
                .andExpect(status().isOk())
                .andExpect(content().string("80000.0"));
    }

    // ===================== SALARY HIKE =====================

    @Test
    @DisplayName("✅ PUT /api/employees/{id}/hike - applies hike successfully")
    void testApplySalaryHike_Success() throws Exception {
        Employee hiked = new Employee("Tenzin Khedup", "tenzin@example.com", "Engineering", 82500.0);
        hiked.setId(1L);
        when(employeeService.applySalaryHike(1L, 10.0)).thenReturn(hiked);

        mockMvc.perform(put("/api/employees/1/hike?percent=10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salary").value(82500.0));
    }

    @Test
    @DisplayName("❌ PUT /api/employees/{id}/hike - returns 400 for invalid percent")
    void testApplySalaryHike_InvalidPercent() throws Exception {
        when(employeeService.applySalaryHike(1L, 150.0)).thenThrow(
                new IllegalArgumentException("Hike percent must be between 0 and 100")
        );

        mockMvc.perform(put("/api/employees/1/hike?percent=150.0"))
                .andExpect(status().isBadRequest());
    }
}
