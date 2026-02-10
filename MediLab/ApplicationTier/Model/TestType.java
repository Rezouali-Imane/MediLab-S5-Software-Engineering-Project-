package ApplicationTier.Model;

public class TestType {
    private int testTypeId;
    private int categoryId;
    private String name;
    private String description;
    private double price;
    private String normalRange;

    public TestType() {}

    public TestType(int categoryId, String name, String description, double price, String normalRange) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.normalRange = normalRange;
    }


    public int getTestTypeId() { return testTypeId; }
    public void setTestTypeId(int testTypeId) { this.testTypeId = testTypeId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getNormalRange() { return normalRange; }
    public void setNormalRange(String normalRange) { this.normalRange = normalRange; }

    @Override
    public String toString() {
        return name + " (" + price + " EUR)";
    }
}