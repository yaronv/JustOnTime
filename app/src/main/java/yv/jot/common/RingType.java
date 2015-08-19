package yv.jot.common;

public enum RingType {
    GET_READY(0), GO_OUT(1);

    private final int code;

    RingType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static RingType fromString(String gender) {
        if(gender.equalsIgnoreCase(GET_READY.name())) {
            return GET_READY;
        }
        if(gender.equalsIgnoreCase(GO_OUT.name())) {
            return GO_OUT;
        }
        return null;
    }

    public  static RingType formCode(int code) {
        for(RingType r : values()) {
            if(r.getCode() == code) {
                return r;
            }
        }
        return null;
    }
}
