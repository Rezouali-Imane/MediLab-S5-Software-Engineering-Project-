package ApplicationTier.Model;

public class Enums {

    public enum Role {
        SUPER_ADMIN, ADMIN, SECRETARY, TECHNICIAN, FINANCIAL_MANAGER
    }

    public enum AppointmentStatus {
        SCHEDULED, COMPLETED, CANCELLED
    }

    public enum TestOrderStatus {
        CREATED, SAMPLE_COLLECTED, IN_PROGRESS, VALIDATED, COMPLETED
    }

    public enum PaymentStatus {
        PAID, UNPAID, PARTIAL;

        public boolean equalsIgnoreCase(String paid) {
            return this.name().equalsIgnoreCase(paid);
        }
    }

    public enum PaymentMethod {
        CCP, BaridiMob, BankCard, Cash, Check
    }
}