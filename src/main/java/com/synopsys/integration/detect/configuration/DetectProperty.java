package com.synopsys.integration.detect.configuration;

import org.antlr.v4.runtime.misc.NotNull;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.configuration.property.Property;
import com.synopsys.integration.configuration.property.PropertyDeprecationInfo;
import com.synopsys.integration.configuration.property.PropertyGroupInfo;
import com.synopsys.integration.configuration.property.PropertyHelpInfo;
import com.synopsys.integration.configuration.util.Category;
import com.synopsys.integration.configuration.util.Group;
import com.synopsys.integration.configuration.util.ProductMajorVersion;

public class DetectProperty<T extends Property> {
    private final T property;

    @Nullable
    private String name = null;
    @Nullable
    private String fromVersion = null;
    @Nullable
    private PropertyHelpInfo propertyHelpInfo = null;
    @Nullable
    private PropertyGroupInfo propertyGroupInfo = null;
    @Nullable
    private Category category = null;
    @Nullable
    private PropertyDeprecationInfo propertyDeprecationInfo = null;

    public DetectProperty(T property) {
        this.property = property;
    }

    public DetectProperty<T> setInfo(String name, String fromVersion) {
        this.name = name;
        this.fromVersion = fromVersion;
        return this;
    }

    public DetectProperty<T> setHelp(@NotNull String shortText) {
        this.propertyHelpInfo = new PropertyHelpInfo(shortText, null);
        return this;
    }

    public DetectProperty<T> setHelp(@NotNull String shortText, @Nullable String longText) {
        this.propertyHelpInfo = new PropertyHelpInfo(shortText, longText);
        return this;
    }

    public DetectProperty<T> setGroups(Group primaryGroup, Group... additionalGroups) {
        this.propertyGroupInfo = new PropertyGroupInfo(primaryGroup, additionalGroups);
        return this;
    }

    public DetectProperty<T> setCategory(Category category) {
        this.category = category;
        return this;
    }

    public DetectProperty<T> setDeprecated(String description, ProductMajorVersion failInVersion, ProductMajorVersion removeInVersion) {
        this.propertyDeprecationInfo = new PropertyDeprecationInfo(description, failInVersion, removeInVersion);
        return this;
    }

    public T getProperty() {
        return property;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getFromVersion() {
        return fromVersion;
    }

    @Nullable
    public PropertyHelpInfo getPropertyHelpInfo() {
        return propertyHelpInfo;
    }

    @Nullable
    public PropertyGroupInfo getPropertyGroupInfo() {
        return propertyGroupInfo;
    }

    @Nullable
    public Category getCategory() {
        return category;
    }

    @Nullable
    public PropertyDeprecationInfo getPropertyDeprecationInfo() {
        return propertyDeprecationInfo;
    }
}
