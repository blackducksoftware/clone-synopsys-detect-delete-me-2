package com.synopsys.integration.detect.workflow.git;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.bdio2.model.GitInfo;
import com.synopsys.integration.detect.tool.detector.report.DetectorDirectoryReport;
import com.synopsys.integration.detect.tool.detector.report.rule.ExtractedDetectorRuleReport;
import com.synopsys.integration.detectable.detectables.git.cli.GitCliExtractor;
import com.synopsys.integration.detector.base.DetectorType;
// TODO clean up
/*
public class DetectorGitProjectInfoDecider {
    public Optional<GitInfo> decideSuggestion(List<DetectorEvaluation> detectorEvaluations) {
        return detectorEvaluations.stream()
            .filter(DetectorEvaluation::wasExtractionSuccessful)
            .filter(detectorEvaluation -> detectorEvaluation.getDetectorType().equals(DetectorType.GIT))
            .filter(evaluation -> evaluation.getExtraction().hasMetadata(GitCliExtractor.EXTRACTION_METADATA_KEY))
            .min(Comparator.comparingInt(eval -> eval.getSearchEnvironment().getDepth()))
            .flatMap(evaluation -> evaluation.getExtraction().getMetaData(GitCliExtractor.EXTRACTION_METADATA_KEY));
    }
}
*/
public class DetectorGitProjectInfoDecider {
    public Optional<GitInfo> decideSuggestion(List<DetectorDirectoryReport> detectorDirectoryReports) {
        List<ExtractedDetectorRuleReport> gitExtractions = new ArrayList<>();
        for (DetectorDirectoryReport detectorDirectoryReport : detectorDirectoryReports) {
            List<ExtractedDetectorRuleReport> extractionsForDir = detectorDirectoryReport.getExtractedDetectors();
            List<ExtractedDetectorRuleReport> gitExtractionsForDir = extractionsForDir.stream()
                .filter(r -> r.getRule().getDetectorType().equals(DetectorType.GIT))
                .filter(r -> r.getExtractedDetectable().getExtraction().hasMetadata(GitCliExtractor.EXTRACTION_METADATA_KEY))
                .collect(Collectors.toList());
            gitExtractions.addAll(gitExtractionsForDir);
        }
        return gitExtractions.stream()
            .min(Comparator.comparingInt(eval -> eval.getDepth()))
            .flatMap(evaluation -> evaluation.getExtractedDetectable().getExtraction().getMetaData(GitCliExtractor.EXTRACTION_METADATA_KEY));
    }
}
