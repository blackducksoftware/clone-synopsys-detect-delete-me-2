package com.synopsys.integration.configuration.property.types.enumextended;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.configuration.property.base.NullableProperty;
import com.synopsys.integration.configuration.util.EnumPropertyUtils;

public class NullableExtendedEnumProperty<E extends Enum<E>, B extends Enum<B>> extends NullableProperty<ExtendedEnumValue<E, B>> {
    private List<String> allOptions;
    private Class<B> bClass;

    public NullableExtendedEnumProperty(@NotNull String key,
        @NotNull Class<E> eClass,
        @NotNull Class<B> bClass) {
        super(key, new ExtendedEnumValueParser<>(eClass, bClass));
        allOptions = new ArrayList<>();
        allOptions.addAll(EnumPropertyUtils.getEnumNames(eClass));
        allOptions.addAll(EnumPropertyUtils.getEnumNames(bClass));
        this.bClass = bClass;
    }

    @Override
    public boolean isCaseSensitive() {
        return false;
    }

    @Nullable
    @Override
    public List<String> listExampleValues() {
        return allOptions;
    }

    @Override
    public boolean isOnlyExampleValues() {
        return true;
    }

    @Nullable
    @Override
    public String describeType() {
        return "Optional " + bClass.getSimpleName();
    }
}
