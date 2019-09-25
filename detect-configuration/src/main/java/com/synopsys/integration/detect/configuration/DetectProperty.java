/**
 * detect-configuration
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.detect.configuration;

import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.ADVANCED;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.DEFAULT_HELP;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_BAZEL;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_BITBAKE;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_BLACKDUCK_SERVER;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_CLEANUP;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_CONDA;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_CPAN;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_DETECTOR;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_DOCKER;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_GENERAL;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_GO;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_GRADLE;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_HEX;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_HUB_CONFIGURATION;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_LOGGING;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_MAVEN;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_NPM;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_NUGET;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_PACKAGIST;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_PATHS;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_PEAR;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_PIP;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_POLARIS;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_PROJECT;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_PROJECT_INFO;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_PROXY;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_PYTHON;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_REPORT;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_RUBY;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_SBT;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_SIGNATURE_SCANNER;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_SOURCE_PATH;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_SOURCE_SCAN;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.GROUP_YARN;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.SEARCH_GROUP_BLACKDUCK;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.SEARCH_GROUP_GLOBAL;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.SEARCH_GROUP_HUB;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.SEARCH_GROUP_OFFLINE;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.SEARCH_GROUP_POLICY;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.SEARCH_GROUP_PROJECT_SETTING;
import static com.synopsys.integration.detect.configuration.DetectProperty.PropertyConstants.SEARCH_GROUP_REPORT_SETTING;

import com.synopsys.integration.detect.DetectMajorVersion;
import com.synopsys.integration.detect.help.AcceptableValues;
import com.synopsys.integration.detect.help.DetectDeprecation;
import com.synopsys.integration.detect.help.HelpDescription;
import com.synopsys.integration.detect.help.HelpDetailed;
import com.synopsys.integration.detect.help.HelpGroup;
import com.synopsys.integration.detect.property.PropertyType;

public enum DetectProperty {

    @HelpGroup(primary = GROUP_BLACKDUCK_SERVER, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription("The API token used to authenticate with the Black Duck Server.")
    BLACKDUCK_API_TOKEN("blackduck.api.token", "Black Duck API Token", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_BLACKDUCK_SERVER, additional = { SEARCH_GROUP_BLACKDUCK, SEARCH_GROUP_OFFLINE, DEFAULT_HELP })
    @HelpDescription("This can disable any Black Duck communication - if true, Detect will not upload BDIO files, it will not check policies, and it will not download and install the signature scanner.")
    BLACKDUCK_OFFLINE_MODE("blackduck.offline.mode", "Offline Mode", "4.2.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_BLACKDUCK_SERVER, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription("Black Duck password.")
    BLACKDUCK_PASSWORD("blackduck.password", "Black Duck Password", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROXY, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription(category = ADVANCED, value = "Hostname for proxy server.")
    BLACKDUCK_PROXY_HOST("blackduck.proxy.host", "Proxy Host", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROXY, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription(category = ADVANCED, value = "A comma separated list of regular expression host patterns that should not use the proxy.")
    @HelpDetailed("These patterns must adhere to Java regular expressions: https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html")
    BLACKDUCK_PROXY_IGNORED_HOSTS("blackduck.proxy.ignored.hosts", "Bypass Proxy Hosts", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROXY, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription(category = ADVANCED, value = "NTLM Proxy domain.")
    BLACKDUCK_PROXY_NTLM_DOMAIN("blackduck.proxy.ntlm.domain", "NTLM Proxy Domain", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROXY, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription(category = ADVANCED, value = "NTLM Proxy workstation.")
    BLACKDUCK_PROXY_NTLM_WORKSTATION("blackduck.proxy.ntlm.workstation", "NTLM Proxy Workstation", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROXY, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription(category = ADVANCED, value = "Proxy password.")
    BLACKDUCK_PROXY_PASSWORD("blackduck.proxy.password", "Proxy Password", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROXY, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription(category = ADVANCED, value = "Proxy port.")
    BLACKDUCK_PROXY_PORT("blackduck.proxy.port", "Proxy Port", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROXY, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription(category = ADVANCED, value = "Proxy username.")
    BLACKDUCK_PROXY_USERNAME("blackduck.proxy.username", "Proxy Username", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_BLACKDUCK_SERVER, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription(category = ADVANCED, value = "The time to wait for network connections to complete (in seconds).")
    BLACKDUCK_TIMEOUT("blackduck.timeout", "Black Duck Timeout", "4.2.0", PropertyType.INTEGER, PropertyAuthority.None, "120"),

    @HelpGroup(primary = GROUP_BLACKDUCK_SERVER, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription(category = ADVANCED, value = "If true, automatically trust the certificate for the current run of Detect only.")
    BLACKDUCK_TRUST_CERT("blackduck.trust.cert", "Trust All SSL Certificates", "4.2.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_BLACKDUCK_SERVER, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription("URL of the Black Duck server.")
    BLACKDUCK_URL("blackduck.url", "Black Duck URL", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_BLACKDUCK_SERVER, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription("Black Duck username.")
    BLACKDUCK_USERNAME("blackduck.username", "Black Duck Username", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_GENERAL, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "The number of threads to run processes in parallel, defaults to 1, but if you specify less than or equal to 0, the number of processors on the machine will be used.")
    DETECT_PARALLEL_PROCESSORS("detect.parallel.processors", "Detect Parallel Processors", "6.0.0", PropertyType.INTEGER, PropertyAuthority.None, "1"),

    @HelpGroup(primary = GROUP_PATHS, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("Path to the Bash executable.")
    @HelpDetailed("If set, Detect will use the given Bash executable instead of searching for one.")
    DETECT_BASH_PATH("detect.bash.path", "Bash Executable", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_BAZEL, additional = SEARCH_GROUP_GLOBAL)
    @HelpDescription("The path to the Bazel executable.")
    DETECT_BAZEL_PATH("detect.bazel.path", "Bazel Executable", "5.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_BAZEL, additional = GROUP_SOURCE_SCAN)
    @HelpDescription("The Bazel target (for example, //foo:foolib) for which dependencies are collected. For Detect to run Bazel, this property must be set.")
    DETECT_BAZEL_TARGET("detect.bazel.target", "Bazel Target", "5.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_BAZEL, additional = GROUP_SOURCE_SCAN)
    @HelpDescription("The path to a file containing a list of BazelExternalIdExtractionXPathRule objects in json for overriding the default behavior).")
    @HelpDetailed("This property is normally not set, but could potentially be used to customize the Bazel detector.")
    DETECT_BAZEL_ADVANCED_RULES_PATH("detect.bazel.advanced.rules.path", "Bazel Advanced Rules File", "5.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PATHS, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The path to the output directory for all BDIO files.")
    @HelpDetailed("If not set, the BDIO files are placed in a 'BDIO' subdirectory of the output directory.")
    DETECT_BDIO_OUTPUT_PATH("detect.bdio.output.path", "BDIO Output Directory", "3.0.0", PropertyType.STRING, PropertyAuthority.DirectoryManager),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { GROUP_SOURCE_PATH })
    @HelpDescription("If specified, this file and this file only will be uploaded for binary scan analysis. This property takes precedence over detect.binary.scan.file.name.patterns.")
    DETECT_BINARY_SCAN_FILE("detect.binary.scan.file.path", "Binary Scan Target", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { GROUP_SOURCE_PATH })
    @HelpDescription("If specified, all files in the source directory whose names match these file name patterns will be zipped and uploaded for binary scan analysis. This property will not be used if detect.binary.scan.file.path is specified.")
    DETECT_BINARY_SCAN_FILE_NAME_PATTERNS("detect.binary.scan.file.name.patterns", "Binary Scan Target", "6.0.0", PropertyType.STRING_ARRAY, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_BITBAKE, additional = GROUP_SOURCE_SCAN)
    @HelpDescription("The name of the build environment init script.")
    DETECT_BITBAKE_BUILD_ENV_NAME("detect.bitbake.build.env.name", "BitBake Init Script Name", "4.4.0", PropertyType.STRING, PropertyAuthority.None, "oe-init-build-env"),

    @HelpGroup(primary = GROUP_BITBAKE, additional = GROUP_SOURCE_SCAN)
    @HelpDescription("A comma-separated list of package names from which dependencies are extracted.")
    DETECT_BITBAKE_PACKAGE_NAMES("detect.bitbake.package.names", "BitBake Package Names", "4.4.0", PropertyType.STRING_ARRAY, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_BITBAKE, additional = GROUP_SOURCE_SCAN)
    @HelpDescription("A comma-separated list of arguments to supply when sourcing the build environment init script.")
    DETECT_BITBAKE_SOURCE_ARGUMENTS("detect.bitbake.source.arguments", "BitBake Source Arguments", "6.0.0", PropertyType.STRING_ARRAY, PropertyAuthority.None, ""),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("Additional arguments to use when running the Black Duck signature scanner.")
    @HelpDetailed("For example: Suppose you are running in bash on Linux and want to use the signature scanner's ability to read a list of directories to exclude from a file (using the signature scanner --exclude-from option). " +
                      "You tell the signature scanner read excluded directories from a file named excludes.txt in your home directory with: " +
                      "--detect.blackduck.signature.scanner.arguments='--exclude-from ${HOME}/excludes.txt'")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_ARGUMENTS("detect.blackduck.signature.scanner.arguments", "Signature Scanner Arguments", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("If set to true, the signature scanner results are not uploaded to Black Duck, and the scanner results are written to disk.")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_DRY_RUN("detect.blackduck.signature.scanner.dry.run", "Signature Scanner Dry Run", "4.2.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("A comma-separated list of directory name patterns for which Detect searches and adds to the signature scanner --exclude flag values.")
    @HelpDetailed("These patterns are file system glob patterns ('?' is a wildcard for a single character, '*' is a wildcard for zero or more characters). " +
                      "Detect will recursively search within the scan targets for files/directories that match these patterns and will create the corresponding exclusion patterns (paths relative to the scan target directory) for the signature scanner (Black Duck scan CLI). "
                      +
                      "Please note that the signature scanner will only exclude directories; matched filenames will be passed to the signature scanner but will have no effect. These patterns will be added to the patterns provided by detect.blackduck.signature.scanner.exclusion.patterns "
                      +
                      "and passed as --exclude values. " +
                      "For example: suppose you are running in bash on Linux, and have a subdirectory named blackduck-common that you want to exclude. " +
                      "Any of the following would exclude it: --detect.blackduck.signature.scanner.exclusion.name.patterns=blackduck-common, --detect.blackduck.signature.scanner.exclusion.name.patterns='blackduck-common', --detect.blackduck.signature.scanner.exclusion.name.patterns='blackduck-*'. "
                      +
                      "Use this property when you want Detect to convert the given patterns to actual paths. Use detect.blackduck.signature.scanner.exclusion.patterns to pass patterns directly to the signature scanner as-is.")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_EXCLUSION_NAME_PATTERNS("detect.blackduck.signature.scanner.exclusion.name.patterns", "Directory Name Exclusion Patterns", "4.2.0", PropertyType.STRING_ARRAY, PropertyAuthority.None, "node_modules"),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("Enables you to adjust the depth to which Detect will search when creating signature scanner exclusion patterns.")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_EXCLUSION_PATTERN_SEARCH_DEPTH("detect.blackduck.signature.scanner.exclusion.pattern.search.depth", "Exclusion Patterns Search Depth", "5.0.0", PropertyType.INTEGER, PropertyAuthority.None, "4"),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("A comma-separated list of values to be used with the Signature Scanner --exclude flag.")
    @HelpDetailed(
        "Each pattern provided is passed to the signature scanner (Black Duck scan CLI) as a value for an --exclude option. The signature scanner requires that these exclusion patterns start and end with a forward slash (/) and may not contain double asterisks (**). "
            +
            "These patterns will be added to the paths created from detect.blackduck.signature.scanner.exclusion.name.patterns and passed as --exclude values. Use this property to pass patterns directly to the signature scanner as-is. " +
            "For example: suppose you are running in bash on Linux, and have a subdirectory named blackduck-common that you want to exclude from signature scanning. " +
            "Any of the following would exclude it: --detect.blackduck.signature.scanner.exclusion.patterns=/blackduck-common/, --detect.blackduck.signature.scanner.exclusion.patterns='/blackduck-common/', --detect.blackduck.signature.scanner.exclusion.patterns='/blackduck-*/'. "
            +
            "Use detect.blackduck.signature.scanner.exclusion.name.patterns when you want Detect to convert the given patterns to actual paths.")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_EXCLUSION_PATTERNS("detect.blackduck.signature.scanner.exclusion.patterns", "Exclusion Patterns", "4.2.0", PropertyType.STRING_ARRAY, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "If this url is set, an attempt will be made to use it to download the signature scanner. The server url provided must respect the Black Duck's urls for different operating systems.")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_HOST_URL("detect.blackduck.signature.scanner.host.url", "Signature Scanner Host URL", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("To use a local signature scanner, specify the path where the signature scanner was unzipped. This will likely look similar to 'scan.cli-x.y.z' and includes the 'bin, icon, jre, and lib' directories of the expanded scan.cli.")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_LOCAL_PATH("detect.blackduck.signature.scanner.local.path", "Signature Scanner Local Path", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "The memory for the scanner to use.")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_MEMORY("detect.blackduck.signature.scanner.memory", "Signature Scanner Memory", "4.2.0", PropertyType.INTEGER, PropertyAuthority.None, "4096"),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "To use a local signature scanner and force offline, specify the path where the signature scanner was unzipped. This will likely look similar to 'scan.cli-x.y.z' and includes the 'bin, icon, jre, and lib' directories of the expanded scan.cli.")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_OFFLINE_LOCAL_PATH("detect.blackduck.signature.scanner.offline.local.path", "Signature Scanner Local Path (Offline)", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("These paths and only these paths will be scanned.")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_PATHS("detect.blackduck.signature.scanner.paths", "Signature Scanner Target Paths", "4.2.0", PropertyType.STRING_ARRAY, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription("Use this value to enable the various snippet scanning modes. For a full explanation, please refer to the Black Duck Signature Scanner documentation.")
    @AcceptableValues(value = { "SNIPPET_MATCHING", "SNIPPET_MATCHING_ONLY", "FULL_SNIPPET_MATCHING", "FULL_SNIPPET_MATCHING_ONLY", "NONE" }, strict = true)
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_SNIPPET_MATCHING("detect.blackduck.signature.scanner.snippet.matching", "Snippet Matching", "5.5.0", PropertyType.STRING, PropertyAuthority.None, "NONE"),

    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription("If set to true, the signature scanner will, if supported by your Black Duck version, upload source code to Black Duck.")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_UPLOAD_SOURCE_MODE("detect.blackduck.signature.scanner.upload.source.mode", "Upload source mode", "5.4.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "If set, this will aggregate all the BOMs to create a single BDIO file with the name provided.")
    DETECT_BOM_AGGREGATE_NAME("detect.bom.aggregate.name", "Aggregate BDIO File Name", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_GENERAL, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("If set to true, only Detector's capable of running without a build will be run.")
    DETECT_BUILDLESS("detect.detector.buildless", "Buildless Mode", "5.4.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_CLEANUP, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("If true, the files created by Detect will be cleaned up.")
    DETECT_CLEANUP("detect.cleanup", "Cleanup Output", "3.2.0", PropertyType.BOOLEAN, PropertyAuthority.None, "true"),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_GLOBAL, SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "The name of the project version to clone this project version from. Respects the given Clone Categories in detect.project.clone.categories or as set on the Black Duck server.")
    DETECT_CLONE_PROJECT_VERSION_NAME("detect.clone.project.version.name", "Clone Project Version Name", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_GLOBAL, SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "If set to true, detect will attempt to use the latest project version as the clone for this project. The project must exist and have at least one version.")
    DETECT_CLONE_PROJECT_VERSION_LATEST("detect.clone.project.version.latest", "Clone Latest Project Version", "5.6.0", PropertyType.BOOLEAN, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "An override for the name Detect will use for the scan file it creates. If supplied and multiple scans are found, Detect will append an index to each scan name.")
    DETECT_CODE_LOCATION_NAME("detect.code.location.name", "Scan Name", "4.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_CONDA, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("The name of the anaconda environment used by your project.")
    DETECT_CONDA_ENVIRONMENT_NAME("detect.conda.environment.name", "Anaconda Environment Name", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_CONDA, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The path to the conda executable.")
    DETECT_CONDA_PATH("detect.conda.path", "Conda Executable", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_CPAN, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The path to the cpan executable.")
    DETECT_CPAN_PATH("detect.cpan.path", "cpan Executable", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_CPAN, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The path to the cpanm executable.")
    DETECT_CPANM_PATH("detect.cpanm.path", "cpanm Executable", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "The scheme to use when the package managers can not determine a version, either 'text' or 'timestamp'.")
    DETECT_DEFAULT_PROJECT_VERSION_SCHEME("detect.default.project.version.scheme", "Default Project Version Name Scheme", "3.0.0", PropertyType.STRING, PropertyAuthority.None, "text"),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "The text to use as the default project version.")
    DETECT_DEFAULT_PROJECT_VERSION_TEXT("detect.default.project.version.text", "Default Project Version Name Text", "3.0.0", PropertyType.STRING, PropertyAuthority.None, "Default Detect Version"),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "The timestamp format to use as the default project version.")
    DETECT_DEFAULT_PROJECT_VERSION_TIMEFORMAT("detect.default.project.version.timeformat", "Default Project Version Name Timestamp Format", "3.0.0", PropertyType.STRING, PropertyAuthority.None, "yyyy-MM-dd\\'T\\'HH:mm:ss.SSS"),

    @HelpGroup(primary = GROUP_PATHS, additional = { GROUP_DETECTOR, SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription("Depth of subdirectories within the source directory to which Detect will search for files that indicate whether a detector applies.")
    @HelpDetailed("A value of 0 (the default) tells Detect not to search any subdirectories, a value of 1 tells Detect to search first-level subdirectories, etc.")
    DETECT_DETECTOR_SEARCH_DEPTH("detect.detector.search.depth", "Detector Search Depth", "3.2.0", PropertyType.INTEGER, PropertyAuthority.None, "0"),

    @HelpGroup(primary = GROUP_PATHS, additional = { GROUP_DETECTOR, SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "If true, the bom tool search will continue to look for nested bom tools of the same type to the maximum search depth, see the detailed help for more information.")
    @HelpDetailed("If true, Detect will find Maven projects that are in subdirectories of a Maven project and Gradle projects that are in subdirectories of Gradle projects, etc.\r\nIf false, Detect will only find bom tools in subdirectories of a project if they are of a different type such as an Npm project in a subdirectory of a Gradle project.")
    DETECT_DETECTOR_SEARCH_CONTINUE("detect.detector.search.continue", "Detector Search Continue", "3.2.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_PATHS, additional = { GROUP_DETECTOR, SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "A comma-separated list of directory names to exclude from detector search.")
    @HelpDetailed("While searching the source directory to determine which detectors to run, subdirectories whose name appear in this list will not be searched.")
    DETECT_DETECTOR_SEARCH_EXCLUSION("detect.detector.search.exclusion", "Detector Directory Exclusions", "3.2.0", PropertyType.STRING_ARRAY, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PATHS, additional = { GROUP_DETECTOR, SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "A comma-separated list of directory name patterns to exclude from detector search.")
    @HelpDetailed(
        "While searching the source directory to determine which detectors to run, subdirectories whose name match a pattern in this list will not be searched.\n\rThese patterns are file system glob patterns ('?' is a wildcard for a single character, '*' is a wildcard for zero or more characters). "
            +
            "For example, suppose you're running in bash on Linux, you've set --detect.detector.search.depth=1, and have a subdirectory named blackduck-common (a gradle project) that you want to exclude from the detector search. Any of the following would exclude it: "
            +
            "--detect.detector.search.exclusion.patterns=blackduck-common, --detect.detector.search.exclusion.patterns='blackduck-common', --detect.detector.search.exclusion.patterns='blackduck-*'")
    DETECT_DETECTOR_SEARCH_EXCLUSION_PATTERNS("detect.detector.search.exclusion.patterns", " Detector Directory Patterns Exclusions", "3.2.0", PropertyType.STRING_ARRAY, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PATHS, additional = { GROUP_DETECTOR, SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "A comma-separated list of directory paths to exclude from detector search. (E.g. 'foo/bar/biz' will only exclude the 'biz' directory if the parent directory structure is 'foo/bar/'.)")
    @HelpDetailed("This property performs the same basic function as detect.detector.search.exclusion, but lets you be more specific.")
    DETECT_DETECTOR_SEARCH_EXCLUSION_PATHS("detect.detector.search.exclusion.paths", " Detector Directory Path Exclusions", "5.5.0", PropertyType.STRING_ARRAY, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PATHS, additional = { GROUP_DETECTOR, SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "A comma-separated list of file names to exclude from detector search.")
    DETECT_DETECTOR_SEARCH_EXCLUSION_FILES("detect.detector.search.exclusion.files", " Detector File Exclusions", "6.0.0", PropertyType.STRING_ARRAY, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PATHS, additional = { GROUP_DETECTOR, SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "If true, the bom tool search will exclude the default directory names. See the detailed help for more information.")
    @HelpDetailed("If true, these directories will be excluded from the detector search: " + DetectorSearchExcludedDirectories.DIRECTORY_NAMES + ".")
    DETECT_DETECTOR_SEARCH_EXCLUSION_DEFAULTS("detect.detector.search.exclusion.defaults", "Detector Exclude Default Directories", "3.2.0", PropertyType.BOOLEAN, PropertyAuthority.None, "true"),

    @HelpGroup(primary = GROUP_GENERAL, additional = { GROUP_BLACKDUCK_SERVER, GROUP_POLARIS })
    @HelpDescription(category = ADVANCED, value = "If true, Detect will ignore any products that it cannot connect to.")
    @HelpDetailed("If true, when Detect attempts to boot a product it will also check if it can communicate with it - if it cannot, it will not run the product.")
    DETECT_IGNORE_CONNECTION_FAILURES("detect.ignore.connection.failures", "Detect Ignore Connection Failures", "5.3.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_DOCKER, additional = { GROUP_SOURCE_PATH })
    @HelpDescription("The Docker image name to inspect. For Detect to run Docker Inspector, either this property or detect.docker.tar must be set. Docker Inspector finds packages installed by the Linux package manager in Linux-based images.")
    DETECT_DOCKER_IMAGE("detect.docker.image", "Docker Image Name", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_DOCKER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "The path to the directory containing the Docker Inspector jar and images.")
    DETECT_DOCKER_INSPECTOR_AIR_GAP_PATH("detect.docker.inspector.air.gap.path", "Docker Inspector AirGap Path", "3.0.0", PropertyType.STRING, PropertyAuthority.AirGapManager),

    @HelpGroup(primary = GROUP_DOCKER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "This is used to override using the hosted Docker Inspector .jar file by binary repository url. You can use a local Docker Inspector .jar file at this path.")
    DETECT_DOCKER_INSPECTOR_PATH("detect.docker.inspector.path", "Docker Inspector .jar File Path", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_DOCKER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "Version of the Docker Inspector to use. By default Detect will attempt to automatically determine the version to use.")
    DETECT_DOCKER_INSPECTOR_VERSION("detect.docker.inspector.version", "Docker Inspector Version", "3.0.0", PropertyType.STRING, PropertyAuthority.None, ""),

    @HelpGroup(primary = GROUP_DOCKER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("Path to the docker executable.")
    DETECT_DOCKER_PATH("detect.docker.path", "Docker Executable", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_DOCKER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "If set to true, Detect will attempt to run the Docker Inspector only if it finds a docker client executable.")
    DETECT_DOCKER_PATH_REQUIRED("detect.docker.path.required", "Run Without Docker in Path", "4.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_DOCKER, additional = { GROUP_SOURCE_PATH })
    @HelpDescription("A saved Docker image - must be a .tar file. For Detect to run Docker Inspector, either this property or detect.docker.tar must be set. Docker Inspector finds packages installed by the Linux package manager in Linux-based images.")
    DETECT_DOCKER_TAR("detect.docker.tar", "Docker Image Archive File", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_NUGET, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The path to the dotnet executable.")
    DETECT_DOTNET_PATH("detect.dotnet.path", "dotnet Executable", "4.4.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_DETECTOR, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "By default, all detectors will be included. If you want to exclude specific detectors, specify the ones to exclude here. If you want to exclude all detectors, specify \"ALL\". Exclusion rules always win.")
    @HelpDetailed("If Detect runs one or more detector on your project that you would like to exclude, you can use this property to prevent Detect from running them.")
    DETECT_EXCLUDED_DETECTOR_TYPES("detect.excluded.detector.types", "Detector Types Excluded", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_GENERAL, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "If true, Detect will always exit with code 0.")
    DETECT_FORCE_SUCCESS("detect.force.success", "Force Success", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_PATHS, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("Path of the git executable")
    DETECT_GIT_PATH("detect.git.path", "Git Executable", "5.5.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_GO, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("Path to the Godep executable.")
    DETECT_GO_PATH("detect.go.path", "Godep Executable", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_GRADLE, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("Gradle command line arguments to add to the mvn/mvnw command line.")
    @HelpDetailed("By default, Detect runs the gradle (or gradlew) command with one task: dependencies. You can use this property to insert one or more additional gradle command line arguments (options or tasks) before the dependencies argument.")
    DETECT_GRADLE_BUILD_COMMAND("detect.gradle.build.command", "Gradle Build Command", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_GRADLE, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "A comma-separated list of Gradle configurations to exclude.")
    @HelpDetailed("As Detect examines the Gradle project for dependencies, Detect will skip any Gradle configurations specified via this property.")
    DETECT_GRADLE_EXCLUDED_CONFIGURATIONS("detect.gradle.excluded.configurations", "Gradle Exclude Configurations", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_GRADLE, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "A comma-separated list of Gradle sub-projects to exclude.")
    @HelpDetailed("As Detect examines the Gradle project for dependencies, Detect will skip any Gradle sub-projects specified via this property.")
    DETECT_GRADLE_EXCLUDED_PROJECTS("detect.gradle.excluded.projects", "Gradle Exclude Projects", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_GRADLE, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "A comma-separated list of Gradle configurations to include.")
    @HelpDetailed("As Detect examines the Gradle project for dependencies, if this property is set, Detect will include only those Gradle configurations specified via this property that are not excluded. Leaving this unset implies 'include all'. Exclusion rules always win.")
    DETECT_GRADLE_INCLUDED_CONFIGURATIONS("detect.gradle.included.configurations", "Gradle Include Configurations", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_GRADLE, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "A comma-separated list of Gradle sub-projects to include.")
    @HelpDetailed("As Detect examines the Gradle project for dependencies, if this property is set, Detect will include only those sub-projects specified via this property that are not excluded. Leaving this unset implies 'include all'. Exclusion rules always win.")
    DETECT_GRADLE_INCLUDED_PROJECTS("detect.gradle.included.projects", "Gradle Include Projects", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_GRADLE, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "The path to the directory containing the air gap dependencies for the gradle inspector.")
    @HelpDetailed("Use this property when running Detect on a Gradle project in 'air gap' mode (offline). Download and unzip the Detect air gap zip file, and point this property to the packaged-inspectors/gradle directory.")
    DETECT_GRADLE_INSPECTOR_AIR_GAP_PATH("detect.gradle.inspector.air.gap.path", "Gradle Inspector AirGap Path", "3.0.0", PropertyType.STRING, PropertyAuthority.AirGapManager),

    @HelpGroup(primary = GROUP_GRADLE, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "The version of the Gradle Inspector that Detect should use. By default, Detect will try to automatically determine the correct Gradle Inspector version.")
    @HelpDetailed("The Detect Gradle detector uses a separate program, the Gradle Inspector, to discover dependencies from Gradle projects. Detect automatically downloads the Gradle Inspector as needed. Use the property to use a specific version of the Gradle Inspector.")
    DETECT_GRADLE_INSPECTOR_VERSION("detect.gradle.inspector.version", "Gradle Inspector Version", "3.0.0", PropertyType.STRING, PropertyAuthority.None, ""),

    @HelpGroup(primary = GROUP_GRADLE)
    @HelpDescription("The path to the Gradle executable (gradle or gradlew).")
    @HelpDetailed("If set, Detect will use the given Gradle executable instead of searching for one.")
    DETECT_GRADLE_PATH("detect.gradle.path", "Gradle Executable", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_HEX, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The path to the rebar3 executable.")
    DETECT_HEX_REBAR3_PATH("detect.hex.rebar3.path", "Rebar3 Executable", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_DETECTOR, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "By default, all tools will be included. If you want to include only specific tools, specify the ones to include here. Exclusion rules always win.")
    @HelpDetailed("If you want to limit Detect to a subset of its detectors, use this property to specify that subset.")
    DETECT_INCLUDED_DETECTOR_TYPES("detect.included.detector.types", "Detector Types Included", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PATHS, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("Path to the java executable.")
    @HelpDetailed("If set, Detect will use the given java executable instead of searching for one.")
    DETECT_JAVA_PATH("detect.java.path", "Java Executable", "5.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_MAVEN, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("Maven command line arguments to add to the mvn/mvnw command line.")
    @HelpDetailed(
        "By default, Detect runs the mvn (or mvnw) command with one argument: dependency:tree. You can use this property to insert one or more additional mvn command line arguments (goals, etc.) before the dependency:tree argument. " +
            "For example: suppose you are running in bash on Linux, and want to point maven to your settings file (maven_dev_settings.xml in your home directory) and assign the value 'other' to property 'reason'. " +
            "You could do this with: --detect.maven.build.command='--settings ${HOME}/maven_dev_settings.xml --define reason=other'")
    DETECT_MAVEN_BUILD_COMMAND("detect.maven.build.command", "Maven Build Command", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_MAVEN, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "A comma-separated list of Maven modules (sub-projects) to exclude.")
    @HelpDetailed("As Detect parses the mvn dependency:tree output for dependencies, Detect will skip any Maven modules specified via this property.")
    DETECT_MAVEN_EXCLUDED_MODULES("detect.maven.excluded.modules", "Maven Modules Excluded", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_MAVEN, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "A comma-separated list of Maven modules (sub-projects) to include.")
    @HelpDetailed("As Detect parses the mvn dependency:tree output for dependencies, if this property is set, Detect will include only those Maven modules specified via this property that are not excluded. Leaving this unset implies 'include all'. Exclusion rules always win.")
    DETECT_MAVEN_INCLUDED_MODULES("detect.maven.included.modules", "Maven Modules Included", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_MAVEN, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The path to the Maven executable (mvn or mvnw).")
    @HelpDetailed("If set, Detect will use the given Maven executable instead of searching for one.")
    DETECT_MAVEN_PATH("detect.maven.path", "Maven Executable", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_MAVEN, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("A comma separated list of Maven scopes. Output will be limited to dependencies within these scopes (overridden by exclude).")
    @HelpDetailed("If set, Detect will include only dependencies of the given Maven scope.")
    DETECT_MAVEN_INCLUDED_SCOPES("detect.maven.included.scopes", "Dependency Scope Included", "6.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_MAVEN, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("A comma separated list of Maven scopes. Output will be limited to dependencies outside these scopes (overrides include).")
    @HelpDetailed("If set, Detect will include only dependencies outside of the given Maven scope.")
    DETECT_MAVEN_EXCLUDED_SCOPES("detect.maven.excluded.scopes", "Dependency Scope Excluded", "6.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_MAVEN, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "Whether or not detect will include the plugins section when parsing a pom.xml.")
    DETECT_MAVEN_INCLUDE_PLUGINS("detect.maven.include.plugins", "Maven Include Plugins", "5.6.0", PropertyType.BOOLEAN, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_REPORT, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("When set to true, a Black Duck notices report in text form will be created in your source directory.")
    DETECT_NOTICES_REPORT("detect.notices.report", "Generate Notices Report", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_REPORT, additional = { SEARCH_GROUP_GLOBAL, SEARCH_GROUP_REPORT_SETTING })
    @HelpDescription("The output directory for notices report. Default is the source directory.")
    DETECT_NOTICES_REPORT_PATH("detect.notices.report.path", "Notices Report Path", "3.0.0", PropertyType.STRING, PropertyAuthority.None, "."),

    @HelpGroup(primary = GROUP_NPM, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("A space-separated list of additional arguments to use when running Detect against an NPM project.")
    DETECT_NPM_ARGUMENTS("detect.npm.arguments", "NPM Arguments", "4.3.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_NPM, additional = { SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription("Set this value to false if you would like to exclude your dev dependencies when ran.")
    DETECT_NPM_INCLUDE_DEV_DEPENDENCIES("detect.npm.include.dev.dependencies", "Include NPM Development Dependencies", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "true"),

    @HelpGroup(primary = GROUP_NPM, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The path to the Npm executable.")
    DETECT_NPM_PATH("detect.npm.path", "NPM Executable", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_NUGET, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("The path to the Nuget.Config file to supply to the nuget exe.")
    DETECT_NUGET_CONFIG_PATH("detect.nuget.config.path", "Nuget Config File", "4.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_NUGET, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "The names of the projects in a solution to exclude.")
    DETECT_NUGET_EXCLUDED_MODULES("detect.nuget.excluded.modules", "Nuget Projects Excluded", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_NUGET, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "If true errors will be logged and then ignored.")
    DETECT_NUGET_IGNORE_FAILURE("detect.nuget.ignore.failure", "Ignore Nuget Failures", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_NUGET, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "The names of the projects in a solution to include (overrides exclude).")
    DETECT_NUGET_INCLUDED_MODULES("detect.nuget.included.modules", "Nuget Modules Included", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_NUGET, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "The path to the directory containing the nuget inspector nupkg.")
    DETECT_NUGET_INSPECTOR_AIR_GAP_PATH("detect.nuget.inspector.air.gap.path", "Nuget Inspector AirGap Path", "3.0.0", PropertyType.STRING, PropertyAuthority.AirGapManager),

    @HelpGroup(primary = GROUP_NUGET, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "Version of the Nuget Inspector. By default Detect will communicate with Artifactory.")
    DETECT_NUGET_INSPECTOR_VERSION("detect.nuget.inspector.version", "Nuget Inspector Version", "3.0.0", PropertyType.STRING, PropertyAuthority.None, ""),

    @HelpGroup(primary = GROUP_NUGET, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The source for nuget packages")
    @HelpDetailed("Set this to \"https://www.nuget.org/api/v2/\" if your are still using a nuget client expecting the v2 api.")
    DETECT_NUGET_PACKAGES_REPO_URL("detect.nuget.packages.repo.url", "Nuget Packages Repository URL", "3.0.0", PropertyType.STRING_ARRAY, PropertyAuthority.None, "https://api.nuget.org/v3/index.json"),

    @HelpGroup(primary = GROUP_PATHS, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The path to the output directory.")
    @HelpDetailed("If set, Detect will use the given directory to store files that it downloads and creates, instead of using the default location (~/blackduck).")
    DETECT_OUTPUT_PATH("detect.output.path", "Detect Output Path", "3.0.0", PropertyType.STRING, PropertyAuthority.DirectoryManager),

    @HelpGroup(primary = GROUP_PATHS, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "The path to the tools directory where detect should download and/or access things like the Signature Scanner that it shares over multiple runs.")
    @HelpDetailed("If set, Detect will use the given directory instead of using the default location of output path plus tools.")
    DETECT_TOOLS_OUTPUT_PATH("detect.tools.output.path", "Detect Tools Output Path", "5.6.0", PropertyType.STRING, PropertyAuthority.DirectoryManager),

    @HelpGroup(primary = GROUP_PACKAGIST, additional = { SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription("Set this value to false if you would like to exclude your dev requires dependencies when ran.")
    DETECT_PACKAGIST_INCLUDE_DEV_DEPENDENCIES("detect.packagist.include.dev.dependencies", "Include Packagist Development Dependencies", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "true"),

    @HelpGroup(primary = GROUP_PEAR, additional = { SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription("Set to true if you would like to include only required packages.")
    DETECT_PEAR_ONLY_REQUIRED_DEPS("detect.pear.only.required.deps", "Include Only Required Pear Dependencies", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_PEAR, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The path to the pear executable.")
    DETECT_PEAR_PATH("detect.pear.path", "Pear Executable", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PIP, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("The name of your PIP project, to be used if your project's name cannot be correctly inferred from its setup.py file.")
    DETECT_PIP_PROJECT_NAME("detect.pip.project.name", "PIP Project Name", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PIP, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("The version of your PIP project, to be used if your project's version name cannot be correctly inferred from its setup.py file.")
    DETECT_PIP_PROJECT_VERSION_NAME("detect.pip.project.version.name", "PIP Project Version Name", "4.1.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PIP, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("A comma-separated list of paths to requirements.txt files.")
    DETECT_PIP_REQUIREMENTS_PATH("detect.pip.requirements.path", "PIP Requirements Path", "3.0.0", PropertyType.STRING_ARRAY, PropertyAuthority.None, null),

    @HelpGroup(primary = GROUP_PIP, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The path to the Pipenv executable.")
    DETECT_PIPENV_PATH("detect.pipenv.path", "Pipenv Executable", "4.1.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PATHS, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("Path of the swift executable.")
    DETECT_SWIFT_PATH("detect.swift.path", "Swift Executable", "6.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_POLARIS, additional = { DEFAULT_HELP, SEARCH_GROUP_GLOBAL })
    @HelpDescription("The url of your polaris instance.")
    POLARIS_URL("polaris.url", "Polaris Url", "4.1.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_POLARIS, additional = { DEFAULT_HELP, SEARCH_GROUP_GLOBAL })
    @HelpDescription("The access token for your polaris instance.")
    POLARIS_ACCESS_TOKEN("polaris.access.token", "Polaris Access Token", "5.3.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_POLARIS, additional = { DEFAULT_HELP, GROUP_SOURCE_SCAN })
    @HelpDescription("Additional arguments to pass to polaris.")
    POLARIS_ARGUMENTS("polaris.arguments", "Polaris Arguments", "5.3.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_GLOBAL, SEARCH_GROUP_PROJECT_SETTING, SEARCH_GROUP_POLICY })
    @HelpDescription("A comma-separated list of policy violation severities that will fail Detect. If this is not set, Detect will not fail due to policy violations. A value of ALL is equivalent to all of the other possible values except UNSPECIFIED.")
    @AcceptableValues(value = { "ALL", "BLOCKER", "CRITICAL", "MAJOR", "MINOR", "TRIVIAL", "UNSPECIFIED" }, caseSensitive = false, strict = false, isCommaSeparatedList = true)
    DETECT_POLICY_CHECK_FAIL_ON_SEVERITIES("detect.policy.check.fail.on.severities", "Fail on Policy Violation Severities", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "Sets the 'Application ID' project setting.")
    DETECT_PROJECT_APPLICATION_ID("detect.project.application.id", "Application ID", "5.2.0", PropertyType.STRING, PropertyAuthority.None, null),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "A  list of custom fields with a label and comma-separated value starting from index 0. For example detect.custom.fields.project[0].label='example' and detect.custom.fields.project[0].value='one,two'. Note that these will not show up in the detect configuration log.")
    DETECT_CUSTOM_FIELDS_PROJECT("detect.custom.fields.project", "Custom Fields", "5.6.0", PropertyType.STRING, PropertyAuthority.None, ""),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "A  list of custom fields with a label and comma-separated value starting from index 0. For example detect.custom.fields.version[0].label='example' and detect.custom.fields.version[0].value='one,two'. Note that these will not show up in the detect configuration log.")
    DETECT_CUSTOM_FIELDS_VERSION("detect.custom.fields.version", "Custom Fields", "5.6.0", PropertyType.STRING, PropertyAuthority.None, ""),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "An override for the Project Clone Categories that are used when cloning a version. If the project already exists, make sure to use --detect.project.version.update to make sure these are set.")
    @AcceptableValues(value = { "COMPONENT_DATA", "VULN_DATA" }, caseSensitive = false, strict = false, isCommaSeparatedList = true)
    DETECT_PROJECT_CLONE_CATEGORIES("detect.project.clone.categories", "Clone Project Categories", "4.2.0", PropertyType.STRING_ARRAY, PropertyAuthority.None, "COMPONENT_DATA,VULN_DATA"),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING, SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "A prefix to the name of the scans created by Detect. Useful for running against the same projects on multiple machines.")
    DETECT_PROJECT_CODELOCATION_PREFIX("detect.project.codelocation.prefix", "Scan Name Prefix", "3.0.0", PropertyType.STRING, PropertyAuthority.None, ""),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING, SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "A suffix to the name of the scans created by Detect.")
    DETECT_PROJECT_CODELOCATION_SUFFIX("detect.project.codelocation.suffix", "Scan Name Suffix", "3.0.0", PropertyType.STRING, PropertyAuthority.None, ""),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "If set to true, unmaps all other scans mapped to the project version produced by the current run of Detect.")
    DETECT_PROJECT_CODELOCATION_UNMAP("detect.project.codelocation.unmap", "Unmap All Other Scans for Project", "4.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription("If project description is specified, your project version will be created with this description.")
    DETECT_PROJECT_DESCRIPTION("detect.project.description", "Project Description", "4.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "A comma-separated list of names of user groups to add to the project.")
    DETECT_PROJECT_USER_GROUPS("detect.project.user.groups", "Project User Groups", "5.4.0", PropertyType.STRING_ARRAY, PropertyAuthority.None, ""),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "A comma-separated list of tags to add to the project.")
    DETECT_PROJECT_TAGS("detect.project.tags", "Project Tags", "5.6.0", PropertyType.STRING_ARRAY, PropertyAuthority.None, ""),

    @HelpGroup(primary = GROUP_PATHS, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "The detector that will be used to determine the project name and version when multiple detector types. This property should be used with the detect.project.tool.")
    @HelpDetailed("If Detect finds that multiple detectors apply, this property can be used to select the detector that will provide the project name and version. When using this property, you should also set detect.project.tool=DETECTOR")
    DETECT_PROJECT_DETECTOR("detect.project.detector", "Project Name/Version Detector", "4.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING, SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "An override for the Project level matches.")
    DETECT_PROJECT_LEVEL_ADJUSTMENTS("detect.project.level.adjustments", "Allow Project Level Adjustments", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "true"),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription("An override for the name to use for the Black Duck project. If not supplied, Detect will attempt to use the tools to figure out a reasonable project name. If that fails, the final part of the directory path where the inspection is taking place will be used.")
    DETECT_PROJECT_NAME("detect.project.name", "Project Name", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "When a parent project and version name are specified, the created detect project will be added as a component to the specified parent project version.")
    DETECT_PARENT_PROJECT_NAME("detect.parent.project.name", "Parent Project Name", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "When a parent project and version name are specified, the created detect project will be added as a component to the specified parent project version.")
    DETECT_PARENT_PROJECT_VERSION_NAME("detect.parent.project.version.name", "Parent Project Version Name", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription("If a Black Duck project tier is specified, your project will be created with this tier.")
    @AcceptableValues(value = { "1", "2", "3", "4", "5" }, caseSensitive = false, strict = false)
    DETECT_PROJECT_TIER("detect.project.tier", "Project Tier", "3.1.0", PropertyType.INTEGER, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PATHS, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "The tool priority for project name and version. The project name and version will be determined by the first tool in this list that provides them.")
    @HelpDetailed("This allows you to control which tool provides the project name and version when more than one tool are capable of providing it.")
    @AcceptableValues(value = { "DETECTOR", "DOCKER", "BAZEL" }, caseSensitive = true, strict = true, isCommaSeparatedList = true)
    DETECT_PROJECT_TOOL("detect.project.tool", "Detector Tool Priority", "5.0.0", PropertyType.STRING, PropertyAuthority.None, "DOCKER,DETECTOR,BAZEL"),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription(category = ADVANCED, value = "An override for the Project Version distribution")
    @AcceptableValues(value = { "EXTERNAL", "SAAS", "INTERNAL", "OPENSOURCE" }, caseSensitive = false, strict = false)
    DETECT_PROJECT_VERSION_DISTRIBUTION("detect.project.version.distribution", "Version Distribution", "3.0.0", PropertyType.STRING, PropertyAuthority.None, "External"),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription("An override for the version to use for the Black Duck project. If not supplied, Detect will attempt to use the tools to figure out a reasonable version name. If that fails, the current date will be used.")
    DETECT_PROJECT_VERSION_NAME("detect.project.version.name", "Version Name", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription("If a project version nickname is specified, your project version will be created with this nickname.")
    DETECT_PROJECT_VERSION_NICKNAME("detect.project.version.nickname", "Version Nickname", "5.2.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription("If project version notes are specified, your project version will be created with these notes.")
    DETECT_PROJECT_VERSION_NOTES("detect.project.version.notes", "Version Notes", "3.1.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription("An override for the Project Version phase.")
    @AcceptableValues(value = { "ARCHIVED", "DEPRECATED", "DEVELOPMENT", "PLANNING", "PRERELEASE", "RELEASED" }, caseSensitive = false, strict = false)
    DETECT_PROJECT_VERSION_PHASE("detect.project.version.phase", "Version Phase", "3.0.0", PropertyType.STRING, PropertyAuthority.None, "Development"),

    @HelpGroup(primary = GROUP_PROJECT, additional = { SEARCH_GROUP_PROJECT_SETTING })
    @HelpDescription("If set to true, will update the Project Version with the configured properties. See detailed help for more information.")
    @HelpDetailed("When set to true, the following properties will be updated on the Project. Project tier (detect.project.tier) and Project Level Adjustments (detect.project.level.adjustments).\r\n The following properties will also be updated on the Version. Version notes (detect.project.version.notes), phase (detect.project.version.phase), distribution (detect.project.version.distribution).")
    DETECT_PROJECT_VERSION_UPDATE("detect.project.version.update", "Update Project Version", "4.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_PYTHON, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The path to the Python executable.")
    DETECT_PYTHON_PATH("detect.python.path", "Python Executable", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PYTHON, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("If true will use Python 3 if available on class path.")
    DETECT_PYTHON_PYTHON3("detect.python.python3", "Use Python3", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_BLACKDUCK_SERVER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The amount of time in seconds Detect will wait for scans to finish and to generate reports (i.e. risk and policy check). When changing this value, keep in mind the checking of policies might have to wait for scans to process which can take some time.")
    DETECT_REPORT_TIMEOUT("detect.report.timeout", "Report Generation Timeout", "5.2.0", PropertyType.LONG, PropertyAuthority.None, "300"),

    @HelpGroup(primary = GROUP_DETECTOR, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The set of required detectors.")
    @HelpDetailed("If you want one or more detectors to be required (must be found to apply), use this property to specify the set of required detectors. If this property is set, and one (or more) of the given detectors is not found to apply, Detect will fail.")
    DETECT_REQUIRED_DETECTOR_TYPES("detect.required.detector.types", "Required Detect Types", "4.3.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PATHS, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("If set to false Detect will not automatically resolve the '~/' prefix in a mac or linux path to the user's home directory.")
    DETECT_RESOLVE_TILDE_IN_PATHS("detect.resolve.tilde.in.paths", "Resolve Tilde in Paths", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "true"),

    @HelpGroup(primary = GROUP_REPORT, additional = { SEARCH_GROUP_GLOBAL, SEARCH_GROUP_REPORT_SETTING })
    @HelpDescription("When set to true, a Black Duck risk report in PDF form will be created.")
    DETECT_RISK_REPORT_PDF("detect.risk.report.pdf", "Generate Risk Report (PDF)", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_REPORT, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The output directory for risk report in PDF. Default is the source directory.")
    DETECT_RISK_REPORT_PDF_PATH("detect.risk.report.pdf.path", "Risk Report Output Path", "3.0.0", PropertyType.STRING, PropertyAuthority.None, "."),

    @HelpGroup(primary = GROUP_REPORT, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "The names of the sbt configurations to exclude.")
    DETECT_SBT_EXCLUDED_CONFIGURATIONS("detect.sbt.excluded.configurations", "SBT Configurations Excluded", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_RUBY, additional = { SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription("If set to false, runtime dependencies will not be included when parsing *.gemspec files.")
    DETECT_RUBY_INCLUDE_RUNTIME_DEPENDENCIES("detect.ruby.include.runtime.dependencies", "Ruby Runtime Dependencies", "5.4.0", PropertyType.BOOLEAN, PropertyAuthority.None, "true"),

    @HelpGroup(primary = GROUP_RUBY, additional = { SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription("If set to true, development dependencies will be included when parsing *.gemspec files.")
    DETECT_RUBY_INCLUDE_DEV_DEPENDENCIES("detect.ruby.include.dev.dependencies", "Ruby Development Dependencies", "5.4.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_SBT, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription(category = ADVANCED, value = "The names of the sbt configurations to include.")
    DETECT_SBT_INCLUDED_CONFIGURATIONS("detect.sbt.included.configurations", "SBT Configurations Included", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_SBT, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("Depth the sbt detector will use to search for report files.")
    DETECT_SBT_REPORT_DEPTH("detect.sbt.report.search.depth", "SBT Report Search Depth", "4.3.0", PropertyType.INTEGER, PropertyAuthority.None, "3"),

    @HelpGroup(primary = GROUP_PATHS, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The output directory for all signature scanner output files. If not set, the signature scanner output files will be in a 'scan' subdirectory of the output directory.")
    DETECT_SCAN_OUTPUT_PATH("detect.scan.output.path", "Scan Output Path", "3.0.0", PropertyType.STRING, PropertyAuthority.DirectoryManager),

    @HelpGroup(primary = GROUP_PATHS, additional = { GROUP_SOURCE_PATH })
    @HelpDescription("The path to the project directory to inspect.")
    @HelpDetailed("Detect will search the given directory for hints that indicate which package manager(s) the project uses, and will attempt to run the corresponding detector(s).")
    DETECT_SOURCE_PATH("detect.source.path", "Source Path", "3.0.0", PropertyType.STRING, PropertyAuthority.DirectoryManager),

    @HelpGroup(primary = GROUP_BLACKDUCK_SERVER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("Test the connection to Black Duck with the current configuration.")
    DETECT_TEST_CONNECTION("detect.test.connection", "Test Connection to Black Duck", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_PATHS, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The tools Detect should allow in a comma-separated list. Tools in this list (as long as they are not also in the excluded list) will be allowed to run if all criteria of the tool are met. Exclusion rules always win.")
    @HelpDetailed("This property and detect.tools.excluded provide control over which tools Detect runs.")
    @AcceptableValues(value = { "BAZEL", "DETECTOR", "DOCKER", "SIGNATURE_SCAN", "BINARY_SCAN", "POLARIS", "NONE", "ALL" }, caseSensitive = true, strict = false, isCommaSeparatedList = true)
    DETECT_TOOLS("detect.tools", "Detect Tools Included", "5.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpGroup(primary = GROUP_PATHS, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The tools Detect should not allow, in a comma-separated list. Excluded tools will not be run even if all criteria for the tool is met. Exclusion rules always win.")
    @HelpDetailed("This property and detect.tools provide control over which tools Detect runs.")
    @AcceptableValues(value = { "BAZEL", "DETECTOR", "DOCKER", "SIGNATURE_SCAN", "BINARY_SCAN", "POLARIS", "NONE", "ALL" }, caseSensitive = true, strict = false, isCommaSeparatedList = true)
    DETECT_TOOLS_EXCLUDED("detect.tools.excluded", "Detect Tools Excluded", "5.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpDescription("The path to the Yarn executable.")
    @HelpGroup(primary = GROUP_YARN, additional = { SEARCH_GROUP_GLOBAL })
    DETECT_YARN_PATH("detect.yarn.path", "Yarn Executable", "4.0.0", PropertyType.STRING, PropertyAuthority.None),

    @HelpDescription("Set this to true to only scan production dependencies.")
    @HelpGroup(primary = GROUP_YARN, additional = { SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    DETECT_YARN_PROD_ONLY("detect.yarn.prod.only", "Include Yarn Production Dependencies Only", "4.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @HelpGroup(primary = GROUP_LOGGING, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The logging level of Detect.")
    @AcceptableValues(value = { "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF" }, caseSensitive = false, strict = true)
    LOGGING_LEVEL_COM_SYNOPSYS_INTEGRATION("logging.level.com.synopsys.integration", "Logging Level", "5.3.0", PropertyType.STRING, PropertyAuthority.None, "INFO"),

    @HelpGroup(primary = GROUP_GENERAL, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("If set to true, Detect will wait for Synopsys products until results are available or the blackduck.timeout is exceeded.")
    DETECT_WAIT_FOR_RESULTS("detect.wait.for.results", "Wait For Results", "5.5.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),
    /**********************************************************************************************
     * DEPRECATED START
     *********************************************************************************************/

    @Deprecated
    @DetectDeprecation(description = "This property is no longer required and will not be used in the Bitbake Detector.", failInVersion = DetectMajorVersion.SEVEN, removeInVersion = DetectMajorVersion.EIGHT)
    @HelpGroup(primary = GROUP_BITBAKE, additional = GROUP_SOURCE_SCAN)
    @HelpDescription("The reference implementation of the Yocto project. These characters are stripped from the discovered target architecture.")
    DETECT_BITBAKE_REFERENCE_IMPL("detect.bitbake.reference.impl", "Reference implementation", "4.4.0", PropertyType.STRING, PropertyAuthority.None, "-poky-linux"),

    @Deprecated
    @DetectDeprecation(description = "This property is now deprecated. Please use --detect.report.timeout in the future. NOTE the new property is in SECONDS not MILLISECONDS.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_PROJECT_INFO, additional = { GROUP_PROJECT })
    @HelpDescription("Timeout for response from Black Duck regarding your project (i.e. risk reports and policy check). When changing this value, keep in mind the checking of policies might have to wait for a new scan to process which can take some time.")
    DETECT_API_TIMEOUT("detect.api.timeout", "", "3.0.0", PropertyType.LONG, PropertyAuthority.None, "300000"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.url in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("URL of the Hub server.")
    BLACKDUCK_HUB_URL("blackduck.hub.url", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.timeout in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("The time to wait for rest connections to complete in seconds.")
    BLACKDUCK_HUB_TIMEOUT("blackduck.hub.timeout", "", "3.0.0", PropertyType.INTEGER, PropertyAuthority.None, "120"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.username in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("Hub username.")
    BLACKDUCK_HUB_USERNAME("blackduck.hub.username", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.password in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("Hub password.")
    BLACKDUCK_HUB_PASSWORD("blackduck.hub.password", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.api.token in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("Hub API Token.")
    BLACKDUCK_HUB_API_TOKEN("blackduck.hub.api.token", "", "3.1.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.proxy.host in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB, GROUP_PROXY })
    @HelpDescription("Proxy host.")
    BLACKDUCK_HUB_PROXY_HOST("blackduck.hub.proxy.host", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.proxy.port in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB, GROUP_PROXY })
    @HelpDescription("Proxy port.")
    BLACKDUCK_HUB_PROXY_PORT("blackduck.hub.proxy.port", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.proxy.username in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB, GROUP_PROXY })
    @HelpDescription("Proxy username.")
    BLACKDUCK_HUB_PROXY_USERNAME("blackduck.hub.proxy.username", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.proxy.password in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB, GROUP_PROXY })
    @HelpDescription("Proxy password.")
    BLACKDUCK_HUB_PROXY_PASSWORD("blackduck.hub.proxy.password", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.proxy.ntlm.domain in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB, GROUP_PROXY })
    @HelpDescription("NTLM Proxy domain.")
    BLACKDUCK_HUB_PROXY_NTLM_DOMAIN("blackduck.hub.proxy.ntlm.domain", "", "3.1.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.proxy.ignored.hosts in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB, GROUP_PROXY })
    @HelpDescription("A comma-separated list of host patterns that should not use the proxy.")
    BLACKDUCK_HUB_PROXY_IGNORED_HOSTS("blackduck.hub.proxy.ignored.hosts", "", "3.2.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.proxy.ntlm.workstation in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB, GROUP_PROXY })
    @HelpDescription("NTLM Proxy workstation.")
    BLACKDUCK_HUB_PROXY_NTLM_WORKSTATION("blackduck.hub.proxy.ntlm.workstation", "", "3.1.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.trust.cert in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("If true, automatically trusts the certificate for the current run of Detect only.")
    BLACKDUCK_HUB_TRUST_CERT("blackduck.hub.trust.cert", "", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --blackduck.offline.mode in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB, SEARCH_GROUP_OFFLINE })
    @HelpDescription("This disables any Hub communication. If true, Detect does not upload BDIO files, does not check policies, and does not download and install the signature scanner.")
    BLACKDUCK_HUB_OFFLINE_MODE("blackduck.hub.offline.mode", "", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.ignore.connection.failures in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_HUB_CONFIGURATION, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("If true, during initialization Detect will check for Hub connectivity and exit with status code 0 if it cannot connect.")
    DETECT_DISABLE_WITHOUT_HUB("detect.disable.without.hub", "", "4.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.ignore.connection.failures in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_BLACKDUCK_SERVER, additional = { SEARCH_GROUP_BLACKDUCK, DEFAULT_HELP })
    @HelpDescription("If true, during initialization Detect will check for Black Duck connectivity and exit with status code 0 if it cannot connect.")
    DETECT_DISABLE_WITHOUT_BLACKDUCK("detect.disable.without.blackduck", "Check For Valid Black Duck Connection", "4.2.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @Deprecated
    @DetectDeprecation(description = "This property is being removed. Configuration can no longer be suppressed individually. Log level can be used.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_LOGGING)
    @HelpDescription("If true, the default behavior of printing your configuration properties at startup will be suppressed.")
    DETECT_SUPPRESS_CONFIGURATION_OUTPUT("detect.suppress.configuration.output", "", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @Deprecated
    @DetectDeprecation(description = "This property is being removed. Results can no longer be suppressed individually. Log level can be used.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_LOGGING)
    @HelpDescription("If true, the default behavior of printing the Detect Results will be suppressed.")
    DETECT_SUPPRESS_RESULTS_OUTPUT("detect.suppress.results.output", "", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.excluded.detector.types in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_DETECTOR, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("By default, all tools will be included. If you want to exclude specific detectors, specify the ones to exclude here. If you want to exclude all tools, specify \"ALL\". Exclusion rules always win.")
    DETECT_EXCLUDED_BOM_TOOL_TYPES("detect.excluded.bom.tool.types", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.detector.search.exclusion.defaults in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_PATHS, additional = { GROUP_DETECTOR })
    @HelpDescription("If true, the bom tool search will exclude the default directory names. See the detailed help for more information.")
    @HelpDetailed("If true, these directories will be excluded from the bom tool search: " + DetectorSearchExcludedDirectories.DIRECTORY_NAMES)
    DETECT_BOM_TOOL_SEARCH_EXCLUSION_DEFAULTS("detect.bom.tool.search.exclusion.defaults", "", "3.2.0", PropertyType.BOOLEAN, PropertyAuthority.None, "true"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.detector.search.exclusion in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_PATHS, additional = { GROUP_DETECTOR })
    @HelpDescription("A comma-separated list of directory names to exclude from the bom tool search.")
    DETECT_BOM_TOOL_SEARCH_EXCLUSION("detect.bom.tool.search.exclusion", "", "3.2.0", PropertyType.STRING_ARRAY, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.included.detector.types in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_DETECTOR, additional = { GROUP_DETECTOR })
    @HelpDescription("By default, all tools will be included. If you want to include only specific tools, specify the ones to include here. Exclusion rules always win.")
    DETECT_INCLUDED_BOM_TOOL_TYPES("detect.included.bom.tool.types", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.project.detector in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_PATHS, additional = { GROUP_DETECTOR })
    @HelpDescription("The detector to choose when multiple detector types are found and one needs to be chosen for project name and version. This property should be used with the detect.project.tool.")
    DETECT_PROJECT_BOM_TOOL("detect.project.bom.tool", "", "4.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.detector.search.depth in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_PATHS, additional = { GROUP_DETECTOR })
    @HelpDescription("Depth of subdirectories within the source directory to search for files that indicate whether a detector applies.")
    @HelpDetailed("A value of 0 (the default) tells Detect not to search any subdirectories, a value of 1 tells Detect to search first-level subdirectories, etc.")
    DETECT_BOM_TOOL_SEARCH_DEPTH("detect.bom.tool.search.depth", "", "3.2.0", PropertyType.INTEGER, PropertyAuthority.None, "0"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.required.detector.types in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_DETECTOR, additional = { GROUP_DETECTOR })
    @HelpDescription("If set, Detect will fail if it does not find the bom tool types supplied here.")
    DETECT_REQUIRED_BOM_TOOL_TYPES("detect.required.bom.tool.types", "", "4.3.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.detector.search.continue in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_PATHS, additional = { GROUP_DETECTOR })
    @HelpDescription("If true, the bom tool search will continue to look for nested bom tools of the same type to the maximum search depth, see the detailed help for more information.")
    @HelpDetailed("If true, Detect will find Maven projects that are in subdirectories of a Maven project and Gradle projects that are in subdirectories of Gradle projects, etc.\r\nIf false, Detect will only find bom tools in subdirectories of a project if they are of a different type such as an Npm project in a subdirectory of a Gradle project.")
    DETECT_BOM_TOOL_SEARCH_CONTINUE("detect.bom.tool.search.continue", "", "3.2.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @Deprecated
    @DetectDeprecation(description = "In the future, the gradle inspector will no longer be downloaded from a custom repository, please use Detect Air Gap instead.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_GRADLE)
    @HelpDescription("The respository gradle should use to look for the gradle inspector dependencies.")
    DETECT_GRADLE_INSPECTOR_REPOSITORY_URL("detect.gradle.inspector.repository.url", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "In the future, Detect will not look for a custom named inspector.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_NUGET)
    @HelpDescription("Name of the Nuget Inspector package and the Nuget Inspector exe. (Do not include '.exe'.)")
    @HelpDetailed("The nuget inspector (previously) could be hosted on a custom nuget feed. In this case, Detect needed to know the name of the package to pull and the name of the exe file (which has to match). In the future, Detect will only retreive it from Artifactory or from Air Gap so a custom name is no longer supported.")
    DETECT_NUGET_INSPECTOR_NAME("detect.nuget.inspector.name", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None, "IntegrationNugetInspector"),

    @Deprecated
    @DetectDeprecation(description = "In the future, Detect will no longer need a nuget executable as it will download the inspector from Artifactory exclusively.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_NUGET)
    @HelpDescription("The path to the Nuget executable. Nuget is used to download the classic inspectors nuget package.")
    DETECT_NUGET_PATH("detect.nuget.path", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.blackduck.signature.scanner.dry.run in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("If set to true, the signature scanner results will not be uploaded to the Hub and the scanner results will be written to disk.")
    DETECT_HUB_SIGNATURE_SCANNER_DRY_RUN("detect.hub.signature.scanner.dry.run", "", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.blackduck.signature.scanner.snippet.mode in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("If set to true, the signature scanner will, if supported by your Hub version, run in snippet scanning mode.")
    DETECT_HUB_SIGNATURE_SCANNER_SNIPPET_MODE("detect.hub.signature.scanner.snippet.mode", "", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.blackduck.signature.scanner.exclusion.patterns in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("A comma-separated list of values to be used with the Signature Scanner --exclude flag.")
    DETECT_HUB_SIGNATURE_SCANNER_EXCLUSION_PATTERNS("detect.hub.signature.scanner.exclusion.patterns", "", "3.0.0", PropertyType.STRING_ARRAY, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.blackduck.signature.scanner.paths in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("These paths and only these paths will be scanned.")
    DETECT_HUB_SIGNATURE_SCANNER_PATHS("detect.hub.signature.scanner.paths", "", "3.0.0", PropertyType.STRING_ARRAY, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.blackduck.signature.scanner.exclusion.name.patterns in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("A comma-separated list of directory name patterns Detect will search for and add to the Signature Scanner --exclude flag values.")
    @HelpDetailed("Detect will recursively search within the scan targets for files/directories that match these file name patterns and will create the corresponding exclusion patterns for the signature scanner.\r\nThese patterns will be added to the patterns provided by detect.blackduck.signature.scanner.exclusion.patterns.")
    DETECT_HUB_SIGNATURE_SCANNER_EXCLUSION_NAME_PATTERNS("detect.hub.signature.scanner.exclusion.name.patterns", "", "4.0.0", PropertyType.STRING_ARRAY, PropertyAuthority.None, "node_modules"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.blackduck.signature.scanner.memory in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("The memory for the scanner to use.")
    DETECT_HUB_SIGNATURE_SCANNER_MEMORY("detect.hub.signature.scanner.memory", "", "3.0.0", PropertyType.INTEGER, PropertyAuthority.None, "4096"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.tools in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("Set to true to disable the Hub Signature Scanner.")
    DETECT_HUB_SIGNATURE_SCANNER_DISABLED("detect.hub.signature.scanner.disabled", "", "3.0.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.tools in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_BLACKDUCK })
    @HelpDescription("Set to true to disable the Black Duck Signature Scanner.")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_DISABLED("detect.blackduck.signature.scanner.disabled", "", "4.2.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.blackduck.signature.scanner.offline.local.path in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_OFFLINE, SEARCH_GROUP_HUB })
    @HelpDescription("To use a local signature scanner and force offline, specify the path where the signature scanner was unzipped. This will likely look similar to 'scan.cli-x.y.z' and includes the 'bin, icon, jre, and lib' directories of the expanded scan.cli.")
    DETECT_HUB_SIGNATURE_SCANNER_OFFLINE_LOCAL_PATH("detect.hub.signature.scanner.offline.local.path", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.blackduck.signature.scanner.local.path in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_OFFLINE, SEARCH_GROUP_HUB })
    @HelpDescription("To use a local signature scanner, specify the path where the signature scanner was unzipped. This will likely look similar to 'scan.cli-x.y.z' and includes the 'bin, icon, jre, and lib' directories of the expanded scan.cli.")
    DETECT_HUB_SIGNATURE_SCANNER_LOCAL_PATH("detect.hub.signature.scanner.local.path", "", "4.2.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.blackduck.signature.scanner.host.url in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("If this url is set, an attempt will be made to use it to download the signature scanner. The server url provided must respect the Hub's urls for different operating systems.")
    DETECT_HUB_SIGNATURE_SCANNER_HOST_URL("detect.hub.signature.scanner.host.url", "", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.parallel.processors in the future. The --detect.parallel.processors property will take precedence over this property.", failInVersion = DetectMajorVersion.SEVEN, removeInVersion = DetectMajorVersion.EIGHT)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription(category = ADVANCED, value = "The number of scans to run in parallel, defaults to 1, but if you specify -1, the number of processors on the machine will be used.")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_PARALLEL_PROCESSORS("detect.blackduck.signature.scanner.parallel.processors", "Signature Scanner Parallel Processors", "4.2.0", PropertyType.INTEGER, PropertyAuthority.None, "1"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.parallel.processors in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("The number of scans to run in parallel, defaults to 1, but if you specify -1, the number of processors on the machine will be used.")
    DETECT_HUB_SIGNATURE_SCANNER_PARALLEL_PROCESSORS("detect.hub.signature.scanner.parallel.processors", "", "3.0.0", PropertyType.INTEGER, PropertyAuthority.None, "1"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.blackduck.signature.scanner.arguments in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_HUB })
    @HelpDescription("Additional arguments to use when running the Hub signature scanner.")
    DETECT_HUB_SIGNATURE_SCANNER_ARGUMENTS("detect.hub.signature.scanner.arguments", "", "4.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.tools and POLARIS in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_POLARIS)
    @HelpDescription("Set to false to disable the Synopsys Polaris Tool.")
    DETECT_SWIP_ENABLED("detect.polaris.enabled", "", "4.4.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --logging.level.com.synopsys.integration in the future.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_LOGGING, additional = { SEARCH_GROUP_GLOBAL })
    @HelpDescription("The logging level of Detect.")
    @AcceptableValues(value = { "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF" }, caseSensitive = false, strict = true)
    LOGGING_LEVEL_COM_BLACKDUCKSOFTWARE_INTEGRATION("logging.level.com.blackducksoftware.integration", "Logging Level", "3.0.0", PropertyType.STRING, PropertyAuthority.None, "INFO"),

    @Deprecated
    @DetectDeprecation(description = "This property is changing. Please use --detect.maven.included.scope in the future.", failInVersion = DetectMajorVersion.SEVEN, removeInVersion = DetectMajorVersion.EIGHT)
    @HelpGroup(primary = GROUP_MAVEN, additional = { GROUP_SOURCE_SCAN })
    @HelpDescription("The name of a Maven scope. Output will be limited to dependencies with this scope.")
    @HelpDetailed("If set, Detect will include only dependencies of the given Maven scope.")
    DETECT_MAVEN_SCOPE("detect.maven.scope", "Dependency Scope Included", "3.0.0", PropertyType.STRING, PropertyAuthority.None),

    @Deprecated
    @DetectDeprecation(description = "This property is now deprecated. Please use --detect.blackduck.signature.scanner.snippet.matching in the future. NOTE the new property is one of a particular set of values. You will need to consult the documentation for the Signature Scanner in Black Duck for details.", failInVersion = DetectMajorVersion.SIX, removeInVersion = DetectMajorVersion.SEVEN)
    @HelpGroup(primary = GROUP_SIGNATURE_SCANNER, additional = { SEARCH_GROUP_GLOBAL, GROUP_SOURCE_SCAN })
    @HelpDescription("If set to true, the signature scanner will, if supported by your Black Duck version, run in snippet scanning mode.")
    DETECT_BLACKDUCK_SIGNATURE_SCANNER_SNIPPET_MODE("detect.blackduck.signature.scanner.snippet.mode", "Snippet Scanning", "4.2.0", PropertyType.BOOLEAN, PropertyAuthority.None, "false");

    /**********************************************************************************************
     * DEPRECATED END
     *********************************************************************************************/

    private final String propertyKey;
    private final String propertyName;
    private final PropertyType propertyType;
    private final String defaultValue;
    private final String asOf;
    private final PropertyAuthority propertyAuthority;

    DetectProperty(final String propertyKey, final String propertyName, final String asOf, final PropertyType propertyType, final PropertyAuthority propertyAuthority) {
        this(propertyKey, propertyName, asOf, propertyType, propertyAuthority, null);
    }

    DetectProperty(final String propertyKey, final String propertyName, final String asOf, final PropertyType propertyType, final PropertyAuthority propertyAuthority, final String defaultValue) {
        this.propertyKey = propertyKey;
        this.propertyName = propertyName;
        this.asOf = asOf;
        this.propertyType = propertyType;
        this.defaultValue = defaultValue;
        this.propertyAuthority = propertyAuthority;
    }

    public PropertyAuthority getPropertyAuthority() {
        return propertyAuthority;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getAddedInVersion() {
        return asOf;
    }

    public Boolean isEqualToDefault(final String value) {
        String defaultValue = "";
        if (null != getDefaultValue()) {
            defaultValue = getDefaultValue();
        }
        return value.equals(defaultValue);
    }

    public final class PropertyConstants {
        //Category
        public static final String SIMPLE = "simple";
        public static final String ADVANCED = "advanced";

        //Group
        public static final String GROUP_ARTIFACTORY = "artifactory";
        public static final String GROUP_BLACKDUCK_SERVER = "blackduck server";
        public static final String GROUP_CLEANUP = "cleanup";
        public static final String GROUP_CODELOCATION = "codelocation";
        public static final String GROUP_GENERAL = "general";
        public static final String GROUP_LOGGING = "logging";
        public static final String GROUP_PATHS = "paths";
        public static final String GROUP_POLICY_CHECK = "policy check";
        public static final String GROUP_PROJECT = "project";
        public static final String GROUP_PROJECT_INFO = "project info";
        public static final String GROUP_PROXY = "proxy";
        public static final String GROUP_REPORT = "report";
        public static final String GROUP_SOURCE_SCAN = "source scan";
        public static final String GROUP_SOURCE_PATH = "source path";

        //Tool Groups
        public static final String GROUP_DETECTOR = "detector";
        public static final String GROUP_POLARIS = "polaris";
        public static final String GROUP_SIGNATURE_SCANNER = "signature scanner";

        //Detector Groups
        public static final String GROUP_BAZEL = "bazel";
        public static final String GROUP_BITBAKE = "bitbake";
        public static final String GROUP_CONDA = "conda";
        public static final String GROUP_CPAN = "cpan";
        public static final String GROUP_DOCKER = "docker";
        public static final String GROUP_GO = "go";
        public static final String GROUP_GRADLE = "gradle";
        public static final String GROUP_HEX = "hex";
        public static final String GROUP_MAVEN = "maven";
        public static final String GROUP_NPM = "npm";
        public static final String GROUP_NUGET = "nuget";
        public static final String GROUP_PACKAGIST = "packagist";
        public static final String GROUP_PEAR = "pear";
        public static final String GROUP_PIP = "pip";
        public static final String GROUP_PYTHON = "python";
        public static final String GROUP_RUBY = "ruby";
        public static final String GROUP_SBT = "sbt";
        public static final String GROUP_YARN = "yarn";

        //Additional groups (should not be used as a primary group
        public static final String SEARCH_GROUP_BLACKDUCK = "blackduck";
        public static final String SEARCH_GROUP_DEBUG = "debug";
        public static final String SEARCH_GROUP_GLOBAL = "global";
        public static final String SEARCH_GROUP_OFFLINE = "offline";
        public static final String SEARCH_GROUP_POLICY = "policy";
        public static final String SEARCH_GROUP_PROJECT_SETTING = "project setting";
        public static final String SEARCH_GROUP_REPORT_SETTING = "report setting";
        public static final String SEARCH_GROUP_SEARCH = "search";
        public static final String DEFAULT_HELP = "default";

        //Print Config
        public static final String PRINT_GROUP_DEFAULT = DEFAULT_HELP;

        //All Deprecated Groups
        @Deprecated
        public static final String GROUP_BOMTOOL = "detector";
        @Deprecated
        public static final String GROUP_HUB_CONFIGURATION = "hub configuration";
        @Deprecated
        public static final String SEARCH_GROUP_HUB = "hub";
    }
}
