package com.hridoy.clickx.helpers;

import java.util.HashMap;
import java.util.Map;
import com.google.appinventor.components.common.OptionList;

public enum InteractionType implements OptionList<String> {
    Click("CLICK"),
    DoubleClick("DOUBLE_CLICK"),
    LongPress("LONG_PRESS"),
    SwipeRight("SWIPE_RIGHT"),
    SwipeLeft("SWIPE_LEFT"),
    SwipeUp("SWIPE_UP"),
    SwipeDown("SWIPE_DOWN"),
    TouchUp("TOUCH_UP"),
    TouchDown("TOUCH_DOWN");

    private final String value;

    InteractionType(String value) {
        this.value = value;
    }

    @Override
    public String toUnderlyingValue() {
        return value;
    }

    private static final Map<String, InteractionType> lookup = new HashMap<>();

    static {
        for (InteractionType type : values()) {
            lookup.put(type.toUnderlyingValue(), type);
        }
    }

    public static InteractionType fromUnderlyingValue(String value) {
        return lookup.get(value.toUpperCase());
    }
}
