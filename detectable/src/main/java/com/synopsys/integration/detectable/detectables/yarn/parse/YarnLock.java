package com.synopsys.integration.detectable.detectables.yarn.parse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class YarnLock {
    private final Map<String, Entry> fuzzyIdToResolvedVersionMap;

    public YarnLock(final Map<String, Entry> fuzzyIdToResolvedVersionMap) {
        this.fuzzyIdToResolvedVersionMap = fuzzyIdToResolvedVersionMap;
    }

    public Optional<Entry> entryForFuzzyId(final String fuzzyId) {
        if (fuzzyIdToResolvedVersionMap.containsKey(fuzzyId)) {
            return Optional.of(fuzzyIdToResolvedVersionMap.get(fuzzyId));
        } else {
            return Optional.empty();
        }
    }

    public static class Entry {
        private final String name;
        private final String resolvedVersion;
        private final List<Dependency> yarnDependencies;

        public Entry(final String name, final String resolvedVersion, final List<Dependency> yarnDependencies) {
            this.name = name;
            this.resolvedVersion = resolvedVersion;
            this.yarnDependencies = yarnDependencies;
        }

        public String getName() {
            return name;
        }

        public String getResolvedVersion() {
            return resolvedVersion;
        }

        public List<Dependency> getYarnDependencies() {
            return yarnDependencies;
        }

        public static class Dependency {
            private final String name;
            private final String fuzzyVersion;

            public Dependency(final String name, final String fuzzyVersion) {
                this.name = name;
                this.fuzzyVersion = fuzzyVersion;
            }

            public String getName() {
                return name;
            }

            public String getFuzzyId() {
                return name + "@" + fuzzyVersion;
            }
        }
    }
}
