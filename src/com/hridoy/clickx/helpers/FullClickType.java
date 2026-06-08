package com.hridoy.clickx.helpers;

import com.google.appinventor.components.common.OptionList;
import java.util.HashMap;
import java.util.Map;

public enum FullClickType implements OptionList<Integer> {
    None(0),
    FullClick(1),
    FullClickExceptClickableComponents(2);

    private final Integer value;

    FullClickType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer toUnderlyingValue() {
        return this.value;
    }

    private static final Map<Integer, FullClickType> lookup = new HashMap<>();

    static {
        for (FullClickType type : values()) {
            lookup.put(type.toUnderlyingValue(), type);
        }
    }

    public static FullClickType fromUnderlyingValue(Integer value) {
        if (value == null) return None;
        FullClickType type = lookup.get(value);
        return type != null ? type : None;
    }
}