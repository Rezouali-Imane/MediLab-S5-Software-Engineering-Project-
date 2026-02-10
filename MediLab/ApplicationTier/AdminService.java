package ApplicationTier;
import ApplicationTier.Model.Enums.Role;
import ApplicationTier.Model.*;
import DAO.*;
import java.util.*;

public class AdminService {

    protected EmployeeDAO employeeDAO;

    public AdminService() {
        this.employeeDAO = new EmployeeDAO();
    }


    // Class diagram methods
    public boolean addEmployee(Employee emp) {
        if (emp.getUsername().length() < 3) return false;
        return employeeDAO.addEmployee(emp);
    }

    public boolean removeEmployee(int employeeId) {
        return employeeDAO.deleteEmployee(employeeId);
    }

    public boolean modifyRoles(Employee emp, Role newRole) {
        if (emp.getRole() == Role.SUPER_ADMIN && newRole != Role.SUPER_ADMIN) {
            List<Employee> allEmployees = employeeDAO.getAllEmployees();
            long superAdminCount = allEmployees.stream()
                    .filter(e -> e.getRole() == Role.SUPER_ADMIN)
                    .count();
            if (superAdminCount <= 1) {
                return false;
            }
        }

        emp.setRole(newRole);
        return employeeDAO.updateEmployee(emp);
    }



    // More Details Methods to make AdminService more useful and presentation better
    public boolean updateEmployeeDetails(Employee emp) {
        return employeeDAO.updateEmployee(emp);
    }


    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }

    public int getPatientCount() {
        return new PatientDAO().getAllPatients().size();
    }

}
