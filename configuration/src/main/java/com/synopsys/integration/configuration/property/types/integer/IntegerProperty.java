package com.synopsys.integration.configuration.property.types.integer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.configuration.property.base.ValuedAlikeProperty;
import com.synopsys.integration.configuration.property.base.ValuedProperty;

public class IntegerProperty extends ValuedAlikeProperty<Integer> {
    public IntegerProperty(@NotNull String key, @NotNull Integer defaultValue) {
        super(key, new IntegerValueParser(), defaultValue);
    }

    @Nullable
    @Override
    public String describeDefault() {
        return getDefaultValue().toString();
    }

    @Nullable
    @Override
    public String describeType() {
        return "Integer";
    }
}
