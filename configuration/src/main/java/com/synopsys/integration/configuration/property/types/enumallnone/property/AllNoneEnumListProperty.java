package com.synopsys.integration.configuration.property.types.enumallnone.property;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.synopsys.integration.configuration.property.deprecation.DeprecatedValueUsage;
import com.synopsys.integration.configuration.property.types.enumallnone.enumeration.AllNoneEnum;
import com.synopsys.integration.configuration.property.types.enumallnone.list.AllNoneEnumList;
import com.synopsys.integration.configuration.property.types.enumextended.ExtendedEnumListProperty;
import com.synopsys.integration.configuration.property.types.enumextended.ExtendedEnumListPropertyBase;
import com.synopsys.integration.configuration.property.types.enumextended.ExtendedEnumValue;

public class AllNoneEnumListProperty<B extends Enum<B>> extends ExtendedEnumListPropertyBase<AllNoneEnum, B, AllNoneEnumList<B>> {
    public AllNoneEnumListProperty(@NotNull String key, List<ExtendedEnumValue<AllNoneEnum, B>> defaultValue, @NotNull Class<B> eClass) {
        super(key, defaultValue, AllNoneEnum.class, eClass);
    }

    public AllNoneEnumListProperty(@NotNull String key, @NotNull AllNoneEnum allValue, @NotNull Class<B> eClass) {
        super(key, Collections.singletonList(ExtendedEnumValue.ofExtendedValue(allValue)), AllNoneEnum.class, eClass);
    }

    public AllNoneEnumListProperty(@NotNull String key, @NotNull B extendedValue, @NotNull Class<B> eClass) {
        super(key, Collections.singletonList(ExtendedEnumValue.ofBaseValue(extendedValue)), AllNoneEnum.class, eClass);
    }

    public AllNoneEnumList<B> toList(List<ExtendedEnumValue<AllNoneEnum, B>> values) {
        return new AllNoneEnumList<>(values, bClass);
    }

    @Override
    public @NotNull AllNoneEnumList<B> convertValue(List<ExtendedEnumValue<AllNoneEnum, B>> value) {
        return new AllNoneEnumList<>(value, bClass);
    }

    public void deprecateNone(String reason) {
        deprecateExtendedValue(AllNoneEnum.NONE, reason);
    }

}
