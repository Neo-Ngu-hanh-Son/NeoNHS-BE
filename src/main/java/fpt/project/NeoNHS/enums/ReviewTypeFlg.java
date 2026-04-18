package fpt.project.NeoNHS.enums;

import lombok.Getter;

@Getter
public enum ReviewTypeFlg {
    WORKSHOP(1),
    EVENT(2),
    POINT(3);

    private final int value;

    ReviewTypeFlg(int value) {
        this.value = value;
    }

    public static ReviewTypeFlg fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ReviewTypeFlg type : ReviewTypeFlg.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ReviewTypeFlg value: " + value);
    }
}
