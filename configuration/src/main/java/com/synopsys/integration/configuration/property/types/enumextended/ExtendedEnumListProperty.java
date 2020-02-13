package com.synopsys.integration.configuration.property.types.enumextended;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.configuration.parse.ListValueParser;
import com.synopsys.integration.configuration.property.base.ValuedListProperty;
import com.synopsys.integration.configuration.util.EnumPropertyUtils;

public class ExtendedEnumListProperty<E extends Enum<E>, B extends Enum<B>> extends ValuedListProperty<ExtendedEnumValue<E, B>> {
    private final List<String> allOptions;
    private final Class<B> bClass;

    public ExtendedEnumListProperty(@NotNull final String key, @NotNull final List<ExtendedEnumValue<E, B>> defaultValue, @NotNull final Class<E> eClass, @NotNull final Class<B> bClass) {
        super(key, new ListValueParser<>(new ExtendedEnumValueParser<>(eClass, bClass)), defaultValue);

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
        return bClass.getSimpleName() + " List";
    }
}
