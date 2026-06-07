package com.hridoy.clickx.helpers;

import java.util.HashMap;
import java.util.Map;
import com.google.appinventor.components.common.OptionList;

public enum Animation implements OptionList<String> {
    None("NONE"),
    Shrink("SHRINK"),
    Ripple("RIPPLE");

    private final String value;

    Animation(String value) {
        this.value = value;
    }

    @Override
    public String toUnderlyingValue() {
        return value;
    }

    private static final Map<String, Animation> lookup = new HashMap<>();

    static {
        for (Animation type : values()) {
            lookup.put(type.toUnderlyingValue(), type);
        }
    }

    public static Animation fromUnderlyingValue(String value) {
        return lookup.get(value.toUpperCase());
    }
}