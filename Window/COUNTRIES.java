package Window;

public enum COUNTRIES {
    AUSTRIA_HUNGARY(0, "Austria-Hungary"),
    ENGLAND(1, "England"),
    FRANCE(2, "France"),
    GERMANY(3, "Germany"),
    ITALY(4, "Italy"),
    RUSSIA(5, "Russia"),
    TURKEY(6, "Turkey");

    private final int value;
    private final String name;

    COUNTRIES (final int newValue, final String newString) {
        value = newValue;
        name = newString;
    }

     public int getValue() { return value; }

     public String toString() { return name; }
}
