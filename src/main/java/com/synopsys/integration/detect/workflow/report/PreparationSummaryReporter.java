/**
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.detect.workflow.report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.synopsys.integration.detect.workflow.report.util.ReporterUtils;
import com.synopsys.integration.detect.workflow.report.writer.ReportWriter;
import com.synopsys.integration.detector.base.DetectorEvaluationTree;

public class PreparationSummaryReporter {

    public void write(ReportWriter writer, DetectorEvaluationTree rootEvaluationTree) {
        writeSummary(writer, rootEvaluationTree.asFlatList());
    }

    private void writeSummary(ReportWriter writer, List<DetectorEvaluationTree> detectorEvaluationTrees) {
        List<String> lines = new ArrayList<>();
        EvaluationSummarizer evaluationSummarizer = new EvaluationSummarizer();
        for (DetectorEvaluationTree detectorEvaluationTree : detectorEvaluationTrees) {

            EvaluationSummary summary = evaluationSummarizer.summarize(detectorEvaluationTree);

            if (CollectionUtils.isNotEmpty(summary.getReady()) || CollectionUtils.isNotEmpty(summary.getNotExtractable())) {
                lines.add(detectorEvaluationTree.getDirectory().toString());
                if (CollectionUtils.isNotEmpty(summary.getReady())) {
                    lines.add("\t    READY: " + summary.getReady().stream()
                                                    .map(it -> it.getDetectorRule().getDescriptiveName())
                                                    .sorted()
                                                    .collect(Collectors.joining(", ")));
                }

                //ok ok ok FAILED but a fallback was success.
                lines.addAll(summary.getFailedWithFallback().stream()
                                 .map(it -> "\t FALLBACK: " + it.getDetectorRule().getDescriptiveName() + " - " + it.getExtractabilityMessage())
                                 .sorted()
                                 .collect(Collectors.toList()));

                //actually failed.
                lines.addAll(summary.getFailedNotSkipped().stream()
                                 .map(it -> "\t   FAILED: " + it.getDetectorRule().getDescriptiveName() + " - " + it.getExtractabilityMessage())
                                 .sorted()
                                 .collect(Collectors.toList()));

                //they were NOT run because they were a fallback but they were not needed
                lines.addAll(summary.getSkippedFallbacks().stream()
                                 .map(it -> "\t  SKIPPED: " + it.getDetectorRule().getDescriptiveName() + " - " + it.getExtractabilityMessage())
                                 .sorted()
                                 .collect(Collectors.toList()));
            }
        }

        if (CollectionUtils.isNotEmpty(lines)) {
            ReporterUtils.printHeader(writer, "Preparation for extraction");
            lines.forEach(writer::writeLine);
            ReporterUtils.printFooter(writer);
        }
    }

}
