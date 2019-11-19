<#ftl output_format="Markdown">
# Detectors

Detect uses "Detectors" to find and extract dependencies from all supported package managers.

Each package manager ecosystem is given a Detector Type. Each Detector Type may have multiple ways it extracts dependencies.

All supported Detector Types are listed here.

## Build Detectors
|Type|Name|Language|Forge|Requirements
|---|---|---|---|---|
<#list build as detector>
|${detector.detectorType} | ${detector.detectorName} |${detector.detectableLanguage!""}|${detector.detectableForge!""} | <#if detector.detectableRequirementsMarkdown?has_content ><#noautoesc>${detector.detectableRequirementsMarkdown!""}</#noautoesc></#if>|
</#list>

## Buildless Detectors
|Type|Name|Language|Forge|Requirements
|---|---|---|---|---|
<#list buildless as detector>
|${detector.detectorType} | ${detector.detectorName} |${detector.detectableLanguage!""}|${detector.detectableForge!""} | <#if detector.detectableRequirementsMarkdown?has_content ><#noautoesc>${detector.detectableRequirementsMarkdown!""}</#noautoesc></#if>|
</#list>

## Detector Search

Detectors first check if they "apply" to your project by looking for specific triggers such as present files or specified configuration properties.

Detectors then check that your environment is "extractable", meaning you have all the relevant executables (such as npm or a gradle wrapper) and all the relevant downloads are present or available (such as the docker or nuget inspector).

Inspectors are used by detectors when the package manager requires an integration or embedded plugin to work. For example, Gradle uses an inspector as a plugin that executes a custom task. Most detectors do not require an inspector.

Finally, Detectors perform their "extraction" to find your dependencies. This may require but is not limited to: running executables, performing builds, parsing files and talking to web services.
