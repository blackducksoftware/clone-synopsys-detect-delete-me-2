package com.synopsys.integration.detector.evaluation;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detectable.detectable.result.PassedDetectableResult;
import com.synopsys.integration.detector.base.DetectorEvaluation;
import com.synopsys.integration.detector.base.DetectorEvaluationTree;
import com.synopsys.integration.detector.result.DetectorResult;
import com.synopsys.integration.detector.rule.DetectorRule;
import com.synopsys.integration.detector.rule.DetectorRuleSet;

public class DetectorEvaluatorTest {

    @Test
    public void testSearchAndApplicableEvaluation() {
        final DetectorEvaluationOptions evaluationOptions = Mockito.mock(DetectorEvaluationOptions.class);
        final DetectorEvaluator evaluator = new DetectorEvaluator(evaluationOptions);
        final DetectorEvaluationTree detectorEvaluationTree = Mockito.mock(DetectorEvaluationTree.class);
        Mockito.when(detectorEvaluationTree.getDirectory()).thenReturn(new File("."));
        final Set<DetectorRule> appliedInParent = new HashSet<>();

        final DetectorEvaluation detectorEvaluation = Mockito.mock(DetectorEvaluation.class);
        Mockito.when(detectorEvaluation.isApplicable()).thenReturn(true);
        List<DetectorEvaluation> detectorEvaluations = Arrays.asList(detectorEvaluation);
        Mockito.when(detectorEvaluationTree.getOrderedEvaluations()).thenReturn(detectorEvaluations);

        final DetectorEvaluatorListener detectorEvaluatorListener = Mockito.mock(DetectorEvaluatorListener.class);
        evaluator.setDetectorEvaluatorListener(detectorEvaluatorListener);

        DetectorRule detectorRule = Mockito.mock(DetectorRule.class);
        Mockito.when(detectorRule.getDescriptiveName()).thenReturn("test rule");
        Mockito.when(detectorEvaluation.getDetectorRule()).thenReturn(detectorRule);

        Mockito.when(detectorEvaluationTree.getDepthFromRoot()).thenReturn(0);
        Mockito.when(evaluationOptions.isForceNested()).thenReturn(true);
        final Predicate<DetectorRule> rulePredicate = it -> true;
        Mockito.when(evaluationOptions.getDetectorFilter()).thenReturn(rulePredicate);
        Mockito.when(detectorEvaluation.isSearchable()).thenReturn(true);

        final Detectable detectable = Mockito.mock(Detectable.class);
        Mockito.when(detectorRule.createDetectable(Mockito.any(DetectableEnvironment.class))).thenReturn(detectable);
        Mockito.when(detectable.applicable()).thenReturn(new PassedDetectableResult());

        evaluator.searchAndApplicableEvaluation(detectorEvaluationTree, appliedInParent);

        Mockito.verify(detectorEvaluatorListener).applicableStarted(detectorEvaluation);
        Mockito.verify(detectorRule).createDetectable(Mockito.any(DetectableEnvironment.class));
        Mockito.verify(detectorEvaluation).setDetectableEnvironment(Mockito.any(DetectableEnvironment.class));
        Mockito.verify(detectorEvaluation).setDetectable(detectable);
        Mockito.verify(detectorEvaluatorListener).applicableEnded(detectorEvaluation);
    }

    @Test
    public void testExtractableEvaluation() throws DetectableException {
        final DetectorEvaluationOptions evaluationOptions = Mockito.mock(DetectorEvaluationOptions.class);
        final DetectorEvaluator evaluator = new DetectorEvaluator(evaluationOptions);
        final DetectorEvaluationTree detectorEvaluationTree = Mockito.mock(DetectorEvaluationTree.class);
        Mockito.when(detectorEvaluationTree.getDirectory()).thenReturn(new File("."));

        final DetectorEvaluation detectorEvaluation = Mockito.mock(DetectorEvaluation.class);

        final Detectable detectable = Mockito.mock(Detectable.class);
        final DetectableResult detectableExtractableResult = Mockito.mock(DetectableResult.class);
        Mockito.when(detectableExtractableResult.getPassed()).thenReturn(true);
        Mockito.when(detectableExtractableResult.toDescription()).thenReturn("test detectable");
        Mockito.when(detectable.extractable()).thenReturn(detectableExtractableResult);
        Mockito.when(detectorEvaluation.getDetectable()).thenReturn(detectable);
        List<DetectorEvaluation> detectorEvaluations = Arrays.asList(detectorEvaluation);
        Mockito.when(detectorEvaluationTree.getOrderedEvaluations()).thenReturn(detectorEvaluations);

        final DetectorRuleSet detectorRuleSet = Mockito.mock(DetectorRuleSet.class);
        Mockito.when(detectorEvaluationTree.getDetectorRuleSet()).thenReturn(detectorRuleSet);

        final DetectorEvaluatorListener detectorEvaluatorListener = Mockito.mock(DetectorEvaluatorListener.class);
        evaluator.setDetectorEvaluatorListener(detectorEvaluatorListener);

        DetectorRule detectorRule = Mockito.mock(DetectorRule.class);
        Mockito.when(detectorRule.getDescriptiveName()).thenReturn("test rule");
        Mockito.when(detectorEvaluation.getDetectorRule()).thenReturn(detectorRule);
        Mockito.when(detectorRuleSet.getFallbackFrom(Mockito.any())).thenReturn(Optional.empty());

        Mockito.when(detectorEvaluationTree.getDepthFromRoot()).thenReturn(0);
        Mockito.when(evaluationOptions.isForceNested()).thenReturn(true);
        final Predicate<DetectorRule> rulePredicate = it -> true;
        Mockito.when(evaluationOptions.getDetectorFilter()).thenReturn(rulePredicate);
        Mockito.when(detectorEvaluation.isSearchable()).thenReturn(true);
        Mockito.when(detectorEvaluation.isApplicable()).thenReturn(true);
        Mockito.when(detectorRule.createDetectable(Mockito.any(DetectableEnvironment.class))).thenReturn(detectable);
        Mockito.when(detectable.applicable()).thenReturn(new PassedDetectableResult());

        evaluator.extractableEvaluation(detectorEvaluationTree);

        Mockito.verify(detectorEvaluatorListener).extractableStarted(detectorEvaluation);
        Mockito.verify(detectorEvaluation).setExtractable(Mockito.any(DetectorResult.class));
        Mockito.verify(detectorEvaluatorListener).extractableEnded(detectorEvaluation);
    }
}
