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
package com.synopsys.integration.detect.help.print;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.detect.configuration.HelpConstants;
import com.synopsys.integration.detect.help.DetectArgumentState;
import com.synopsys.integration.detect.help.DetectOption;
import com.synopsys.integration.detect.help.DetectOptionHelp;

public class HelpPrinter {
    private static final String DIAGNOSTIC_HELP_TEXT = "\nDiagnostics mode:\n\n" +
                                                           "In diagnostics mode, Detect will produce a diagnostics zip file that contains a collection of intermediate and output files\n" +
                                                           "that can be very useful for troubleshooting. Extended diagnostics mode writes additional files to the diagnostics zip file.\n" +
                                                           "Invoke diagnostics mode by adding -d (diagnostics mode) or -de (extended diagnostics mode) to the command line.\n" +
                                                           "The path to the generated diagnostics file can be found in the log (look for: \"Diagnostics file created at: ...\").\n" +
                                                           "The diagnostics file can be large, so you may want to generate it only when you will actually use it.\n";

    private static final Comparator<DetectOption> SORT_BY_GROUP_THEN_KEY = (o1, o2) -> {
        if (o1.getDetectOptionHelp().primaryGroup.equals(o2.getDetectOptionHelp().primaryGroup)) {
            return o1.getDetectProperty().getPropertyKey().compareTo(o2.getDetectProperty().getPropertyKey());
        } else {
            return o1.getDetectOptionHelp().primaryGroup.compareTo(o2.getDetectOptionHelp().primaryGroup);
        }
    };

    public void printAppropriateHelpMessage(final PrintStream printStream, final List<DetectOption> allOptions, final DetectArgumentState state) {
        final HelpTextWriter writer = new HelpTextWriter();

        final List<DetectOption> currentOptions = allOptions.stream()
                                                      .filter(it -> !it.getDetectOptionHelp().isDeprecated)
                                                      .collect(Collectors.toList());
        final List<DetectOption> deprecatedOptions = allOptions.stream()
                                                         .filter(it -> it.getDetectOptionHelp().isDeprecated)
                                                         .collect(Collectors.toList());
        final List<String> allPrintGroups = getPrintGroups(currentOptions);

        if (state.isVerboseHelp()) {
            printOptions(writer, currentOptions, null);
        } else if (state.isDeprecatedHelp()) {
            printOptions(writer, deprecatedOptions, "Showing only deprecated properties.");
        } else {
            if (state.getParsedValue() != null) {
                if (isProperty(currentOptions, state.getParsedValue())) {
                    printDetailedHelp(writer, allOptions, state.getParsedValue());
                } else if (isPrintGroup(allPrintGroups, state.getParsedValue())) {
                    printHelpFilteredByPrintGroup(writer, currentOptions, state.getParsedValue());
                } else {
                    printHelpFilteredBySearchTerm(writer, currentOptions, state.getParsedValue());
                }
            } else if (state.isDiagnostic() || state.isDiagnosticExtended()) {
                printStream.println(DIAGNOSTIC_HELP_TEXT);
            } else {
                printDefaultHelp(writer, currentOptions);
            }
        }

        printStandardFooter(writer, getPrintGroupText(allPrintGroups));

        writer.write(printStream);
    }

    private void printDetailedHelp(final HelpTextWriter writer, final List<DetectOption> options, final String optionName) {
        final DetectOption option = options.stream()
                                        .filter(it -> it.getDetectProperty().getPropertyKey().equals(optionName))
                                        .findFirst().orElse(null);

        if (option == null) {
            writer.println("Could not find option named: " + optionName);
        } else {
            printDetailedOption(writer, option);
        }
    }

    private void printDefaultHelp(final HelpTextWriter writer, final List<DetectOption> options) {
        printHelpFilteredByPrintGroup(writer, options, HelpConstants.PRINT_GROUP_DEFAULT);
    }

    private void printHelpFilteredByPrintGroup(final HelpTextWriter writer, final List<DetectOption> options, final String filterGroup) {
        final String notes = "Showing help only for: " + filterGroup;

        final List<DetectOption> filteredOptions = options.stream()
                                                       .filter(detectOption -> optionMatchesFilterGroup(detectOption.getDetectOptionHelp(), filterGroup))
                                                       .collect(Collectors.toList());

        printOptions(writer, filteredOptions, notes);
    }

    private boolean optionMatchesFilterGroup(final DetectOptionHelp detectOptionHelp, final String filterGroup) {
        final boolean primaryMatches = detectOptionHelp.primaryGroup.equalsIgnoreCase(filterGroup);
        final boolean additionalMatches = detectOptionHelp.additionalGroups.stream()
                                              .anyMatch(printGroup -> printGroup.equalsIgnoreCase(filterGroup));
        return primaryMatches || additionalMatches;
    }

    private void printHelpFilteredBySearchTerm(final HelpTextWriter writer, final List<DetectOption> options, final String searchTerm) {
        final String notes = "Showing help only for fields that contain: " + searchTerm;

        final List<DetectOption> filteredOptions = options.stream()
                                                       .filter(it -> it.getDetectProperty().getPropertyKey().contains(searchTerm))
                                                       .collect(Collectors.toList());

        printOptions(writer, filteredOptions, notes);
    }

    private boolean isPrintGroup(final List<String> allPrintGroups, final String filterGroup) {
        return allPrintGroups.contains(filterGroup);
    }

    private boolean isProperty(final List<DetectOption> allOptions, final String filterTerm) {
        return allOptions.stream()
                   .map(it -> it.getDetectProperty().getPropertyKey())
                   .anyMatch(it -> it.equals(filterTerm));
    }

    private List<String> getPrintGroups(final List<DetectOption> options) {
        return options.stream()
                   .flatMap(it -> it.getDetectOptionHelp().additionalGroups.stream())
                   .distinct()
                   .sorted()
                   .collect(Collectors.toList());
    }

    private String getPrintGroupText(final List<String> printGroups) {
        return printGroups.stream().collect(Collectors.joining(","));
    }

    public void printDetailedOption(final HelpTextWriter writer, final DetectOption detectOption) {
        detectOption.printDetailedOption(writer);
    }

    public void printOptions(final HelpTextWriter writer, final List<DetectOption> options, final String notes) {
        writer.printColumns("Property Name", "Default", "Description");
        writer.printSeperator();

        final List<DetectOption> sorted = options.stream()
                                              .sorted(SORT_BY_GROUP_THEN_KEY)
                                              .collect(Collectors.toList());

        if (notes != null) {
            writer.println(notes);
            writer.println();
        }

        String group = null;
        for (final DetectOption detectOption : sorted) {
            final String currentGroup = detectOption.getDetectOptionHelp().primaryGroup;
            if (group == null) {
                group = currentGroup;
                writer.println("[" + group + "]");
            } else if (!group.equals(currentGroup)) {
                group = currentGroup;
                writer.println();
                writer.println("[" + group + "]");
            }
            detectOption.printOption(writer);
        }
    }

    public void printStandardFooter(final HelpTextWriter writer, final String groupText) {
        writer.println();
        writer.println("To set a Detect property on the command line: ");
        writer.println("\t--<property name>=<value>");
        writer.println();
        writer.println("To see all properties, you may request verbose help log with '-hv'");
        writer.println("To see the hidden deprecated properties, you may request them with '-hd'");
        writer.println();
        writer.println("To get detailed help for a specific property, you may specify the property name with '-h [property]'");
        writer.println();
        writer.println("To print only a subset of options, you may specify one of the following printable groups with '-h [group]': ");
        writer.println("\t" + groupText);
        writer.println();
        writer.println("To search options, you may specify a search term with '-h [term]'");
        writer.println();
        writer.println("To run in interactive mode (which prompts you for the values needed for common use cases), run Detect with the argument '--interactive'");
        writer.println();
    }
}
