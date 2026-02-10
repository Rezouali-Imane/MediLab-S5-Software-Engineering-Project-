package ApplicationTier.Model;
import ApplicationTier.Model.Enums.Role;


public class Secretary extends Employee {

    public Secretary() {
        super();
        this.setRole(Role.SECRETARY);
    }
}