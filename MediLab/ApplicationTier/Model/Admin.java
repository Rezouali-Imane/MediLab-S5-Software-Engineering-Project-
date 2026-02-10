package ApplicationTier.Model;
import ApplicationTier.Model.Enums.Role;

public class Admin extends Employee {

    public Admin() {
        super();
        this.setRole(Role.ADMIN);
    }
}
