package com.synopsys.integration.detect.workflow.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.tool.detector.DetectExtractionEnvironment;
import com.synopsys.integration.detect.workflow.report.util.ObjectPrinter;
import com.synopsys.integration.detect.workflow.report.util.ReportConstants;
import com.synopsys.integration.detect.workflow.report.writer.DebugLogReportWriter;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detector.base.DetectorEvaluation;

public class ExtractionLogger {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Integer extractionCount = 0;

    public void setExtractionCount(Integer count) {
        extractionCount = count;
    }

    public void extractionStarted(DetectorEvaluation detectorEvaluation) {
        DetectExtractionEnvironment detectExtractionEnvironment = (DetectExtractionEnvironment) detectorEvaluation.getExtractionEnvironment();
        Integer i = detectExtractionEnvironment.getExtractionId().getId();
        String progress = Integer.toString((int) Math.floor((i * 100.0f) / extractionCount));
        logger.debug(String.format("Extracting %d of %d (%s%%)", i + 1, extractionCount, progress));
        logger.debug(ReportConstants.SEPARATOR);

        logger.debug("Starting extraction: " + detectorEvaluation.getDetectorType() + " - " + detectorEvaluation.getDetectorRule().getName());
        logger.debug("Identifier: " + detectExtractionEnvironment.getExtractionId().toUniqueString());
        ObjectPrinter.printObjectPrivate(new DebugLogReportWriter(logger), detectorEvaluation.getDetectable());
        logger.debug(ReportConstants.SEPARATOR);
    }

    public void extractionEnded(DetectorEvaluation detectorEvaluation) {
        logger.debug(ReportConstants.SEPARATOR);
        logger.debug("Finished extraction: " + detectorEvaluation.getExtraction().getResult().toString());
        logger.debug("Code locations found: " + detectorEvaluation.getExtraction().getCodeLocations().size());
        if (detectorEvaluation.getExtraction().getResult() == Extraction.ExtractionResultType.EXCEPTION) {
            logger.debug("Exception: " + ExceptionUtil.oneSentenceDescription(detectorEvaluation.getExtraction().getError()));
            logger.debug("Details: ", detectorEvaluation.getExtraction().getError());
        } else if (detectorEvaluation.getExtraction().getResult() == Extraction.ExtractionResultType.FAILURE) {
            logger.debug(detectorEvaluation.getExtraction().getDescription());
        }
        logger.debug(ReportConstants.SEPARATOR);
    }
}
