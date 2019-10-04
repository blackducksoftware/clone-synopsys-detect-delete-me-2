package com.synopsys.integration.detectable.detectables.bazel.functional.bazel.pipeline.stepexecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.detectable.detectables.bazel.model.Step;
import com.synopsys.integration.detectable.detectables.bazel.model.StepType;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.stepexecutor.StepExecutor;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.stepexecutor.StepExecutorParseEachXml;
import com.synopsys.integration.exception.IntegrationException;

public class StepExecutorParseEachXmlTest {

    private static final String COMMONS_IO_XML = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                                    + "<query version=\"2\">\n"
                                    + "    <rule class=\"maven_jar\" location=\"/root/home/steve/examples/java-tutorial/WORKSPACE:6:1\" name=\"//external:org_apache_commons_commons_io\">\n"
                                    + "        <string name=\"name\" value=\"org_apache_commons_commons_io\"/>\n"
                                    + "        <string name=\"artifact\" value=\"org.apache.commons:commons-io:1.3.2\"/>\n"
                                    + "    </rule>\n"
                                    + "</query>";

    private static final String GUAVA_XML = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                                + "<query version=\"2\">\n"
                                + "    <rule class=\"maven_jar\" location=\"/root/home/steve/examples/java-tutorial/WORKSPACE:1:1\" name=\"//external:com_google_guava_guava\">\n"
                                + "        <string name=\"name\" value=\"com_google_guava_guava\"/>\n"
                                + "        <string name=\"artifact\" value=\"com.google.guava:guava:18.0\"/>\n"
                                + "    </rule>\n"
                                + "</query>";

    @Test
    public void test() throws IntegrationException {
        final StepExecutor stepExecutor = new StepExecutorParseEachXml();

        assertTrue(stepExecutor.applies(StepType.PARSE_EACH_XML));

        final Step step = new Step(StepType.PARSE_EACH_XML, Arrays.asList("/query/rule[@class='maven_jar']/string[@name='artifact']", "value"));
        final List<String> input = Arrays.asList(COMMONS_IO_XML, GUAVA_XML);

        final List<String> results = stepExecutor.process(step, input);

        assertEquals(2, results.size());
        assertEquals("org.apache.commons:commons-io:1.3.2", results.get(0));
        assertEquals("com.google.guava:guava:18.0", results.get(1));
    }
}
