package ApplicationTier.Model;
import ApplicationTier.Model.Enums.Role;

public class FinancialManager extends Employee {

    public FinancialManager() {
        super();
        this.setRole(Role.FINANCIAL_MANAGER);
    }
}