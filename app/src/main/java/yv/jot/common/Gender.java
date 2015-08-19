package yv.jot.common;

/**
 * Created by yaron on 31/07/15.
 */
public enum Gender {
    Male(1), Female(2);

    private final int code;

    Gender(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Gender fromString(String gender) {
        if(gender.equalsIgnoreCase(Male.name())) {
            return Male;
        }
        if(gender.equalsIgnoreCase(Female.name())) {
            return Female;
        }
        return null;
    }
}
