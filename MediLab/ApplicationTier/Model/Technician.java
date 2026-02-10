package ApplicationTier.Model;
import ApplicationTier.Model.Enums.Role;

public class Technician extends Employee {

    public Technician() {
        super();
        this.setRole(Role.TECHNICIAN);
    }
}