package com.synopsys.integration.configuration.property.types.enums;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.configuration.parse.ListValueParser;
import com.synopsys.integration.configuration.property.base.ValuedListProperty;
import com.synopsys.integration.configuration.util.EnumPropertyUtils;
import com.synopsys.integration.configuration.util.PropertyUtils;

public class EnumListProperty<E extends Enum<E>> extends ValuedListProperty<E> {
    @NotNull
    private final Class<E> enumClass;

    public EnumListProperty(@NotNull String key, @NotNull List<E> defaultValue, @NotNull Class<E> enumClass) {
        super(key, new ListValueParser<>(new EnumValueParser<>(enumClass)), defaultValue);
        this.enumClass = enumClass;
    }

    @Nullable
    @Override
    public String describeDefault() {
        return PropertyUtils.describeObjectList(getDefaultValue());
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }

    @Nullable
    @Override
    public List<String> listExampleValues() {
        return EnumPropertyUtils.getEnumNames(enumClass);
    }

    @Override
    public boolean isOnlyExampleValues() {
        return true;
    }

    @Nullable
    @Override
    public String describeType() {
        return enumClass.getSimpleName() + " List";
    }
}