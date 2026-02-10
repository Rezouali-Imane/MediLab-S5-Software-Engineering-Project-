package ApplicationTier.Model;
import ApplicationTier.Model.Enums.Role;

public class SuperAdmin extends Admin {

    public SuperAdmin() {
        super();
        this.setRole(Role.SUPER_ADMIN);
    }
}