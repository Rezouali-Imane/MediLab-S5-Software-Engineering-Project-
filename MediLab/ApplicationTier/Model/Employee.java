package ApplicationTier.Model;

import java.util.Date;
import ApplicationTier.Model.Enums.Role;

public class Employee {
    private int employeeId;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Role role;
    private Date hireDate;
    private String adress;
    private String phone;
    private String email;

    public Employee(String firstName, String lastName, String username, String password, Role role, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.role = role;
        this.hireDate = new Date();
        this.phone = phone;
        this.email = email;
    }

    public Employee() {

    }


    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public void setRole(String roleStr) {
        if(roleStr != null) this.role = Role.valueOf(roleStr.toUpperCase());
    }

    public Date getHireDate() { return hireDate; }
    public void setHireDate(Date hireDate) { this.hireDate = hireDate; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return adress; }
    public void setAddress(String adress) { this.adress = adress; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return firstName + " " + lastName; }
}