package com.synopsys.integration.detectable.detectables.nuget.future.range;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class SimpleVersion {
    private final Integer major;
    private final Integer minor;
    private final Integer build;
    private final Integer revision;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SimpleVersion that = (SimpleVersion) o;

        if (!Objects.equals(major, that.major))
            return false;
        if (!Objects.equals(minor, that.minor))
            return false;
        if (!Objects.equals(build, that.build))
            return false;
        return Objects.equals(revision, that.revision);
    }

    @Override
    public int hashCode() {
        int result = major.hashCode();
        result = 31 * result + minor.hashCode();
        result = 31 * result + (build != null ? build.hashCode() : 0);
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        return result;
    }

    public SimpleVersion(int major, int minor, @Nullable Integer build, @Nullable Integer revision) {
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.revision = revision;
    }

    //https://docs.microsoft.com/en-us/dotnet/api/system.version.parse?view=net-6.0
    public static Optional<SimpleVersion> parse(String version) {
        String[] pieces = StringUtils.split(version, ".");
        List<Integer> numbers = Arrays.stream(pieces)
            .map(Integer::parseInt)
            .collect(Collectors.toList());

        if (numbers.size() == 2) {
            return Optional.of(new SimpleVersion(numbers.get(0), numbers.get(0), null, null));
        } else if (numbers.size() == 3) {
            return Optional.of(new SimpleVersion(numbers.get(0), numbers.get(1), numbers.get(2), null));
        } else if (numbers.size() == 4) {
            return Optional.of(new SimpleVersion(numbers.get(0), numbers.get(1), numbers.get(2), numbers.get(3)));
        } else {
            return Optional.empty();
        }
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    @Nullable
    public Integer getBuild() {
        return build;
    }

    @Nullable
    public Integer getRevision() {
        return revision;
    }
}
