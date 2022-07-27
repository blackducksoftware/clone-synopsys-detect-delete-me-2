package com.synopsys.integration.detect.docs.model;

public class Detectable {
    private final int index;
    private final String name;
    private final String language;
    private final String forge;
    private final String requirementsMarkdown;
    private final String accuracy;

    public Detectable(
        int index,
        String name,
        String language,
        String forge,
        String requirementsMarkdown,
        String accuracy
    ) {
        this.index = index;
        this.name = name;
        this.language = language;
        this.forge = forge;
        this.requirementsMarkdown = requirementsMarkdown;
        this.accuracy = accuracy;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public String getForge() {
        return forge;
    }

    public String getRequirementsMarkdown() {
        return requirementsMarkdown;
    }

    public String getAccuracy() {
        return accuracy;
    }
}
