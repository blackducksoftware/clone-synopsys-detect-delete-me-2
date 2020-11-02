package com.synopsys.integration.detect.interactive;

import static com.synopsys.integration.detect.interactive.BlackDuckConnectionDecisionBranch.SHOULD_RETRY_CONNECTION;
import static com.synopsys.integration.detect.interactive.BlackDuckConnectionDecisionBranch.SHOULD_TEST_CONNECTION;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SET_API_TOKEN;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SET_BLACKDUCK_SERVER_URL;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SET_PASSWORD;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SET_PROXY_HOST;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SET_PROXY_NTLM_DOMAIN;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SET_PROXY_NTLM_WORKSTATION;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SET_PROXY_PASSWORD;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SET_PROXY_PORT;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SET_PROXY_USERNAME;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SET_USERNAME;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SHOULD_CONFIGURE_PROXY;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SHOULD_SET_PASSWORD;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SHOULD_SET_PROXY_NTLM;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SHOULD_SET_PROXY_PASSWORD;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SHOULD_TRUST_CERTS;
import static com.synopsys.integration.detect.interactive.BlackDuckServerDecisionBranch.SHOULD_USE_API_TOKEN;
import static com.synopsys.integration.detect.interactive.InteractiveModeDecisionTree.SET_PROJECT_NAME;
import static com.synopsys.integration.detect.interactive.InteractiveModeDecisionTree.SET_PROJECT_VERSION;
import static com.synopsys.integration.detect.interactive.InteractiveModeDecisionTree.SHOULD_CONNECT_TO_BLACKDUCK;
import static com.synopsys.integration.detect.interactive.InteractiveModeDecisionTree.SHOULD_RUN_CLI_SCAN;
import static com.synopsys.integration.detect.interactive.InteractiveModeDecisionTree.SHOULD_SAVE_TO_APPLICATION_PROPERTIES;
import static com.synopsys.integration.detect.interactive.InteractiveModeDecisionTree.SHOULD_SET_PROJECT_NAME_VERSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import com.synopsys.integration.common.util.Bds;
import com.synopsys.integration.configuration.source.MapPropertySource;
import com.synopsys.integration.detect.configuration.DetectProperties;
import com.synopsys.integration.detect.configuration.DetectProperty;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;

public class InteractiveModeDecisionTreeTest {
    public static final String YES = "y";
    public static final String NO = "n";
    public static final String EXPECTED_BLACKDUCK_SERVER_URL = "https://example.com/blackduck";
    public static final String EXPECTED_BLACKDUCK_API_TOKEN = "Api Token";
    public static final String EXPECTED_BLACKDUCK_USERNAME = "bdUser";
    public static final String EXPECTED_BLACKDUCK_PASSWORD = "bdPassword";
    public static final String EXPECTED_PROXY_HOST = "proxy.example.com";
    public static final String EXPECTED_PROXY_PORT = "1234";
    public static final String EXPECTED_PROXY_USERNAME = "proxyUser";
    public static final String EXPECTED_PROXY_PASSWORD = "proxyPassword";
    public static final String EXPECTED_PROXY_NTLM_DOMAIN = "proxyNtlmDomain";
    public static final String EXPECTED_PROXY_NTLM_WORKSTATION = "proxyNtlmWorkstation";
    public static final String EXPECTED_PROJECT_NAME = "project";
    public static final String EXPECTED_VERSION_NAME = "version";
    public static final String EXPECTED_SCANNER_HOST_URL = "https://example.com/";
    public static final String EXPECTED_SCANNER_LOCAL_PATH = "/path/to/my/scanner";

    private static Stream<Arguments> getTraversalData() {
        Map<String, String> noBlackduck = Bds.mapOf(Pair.of(SHOULD_CONNECT_TO_BLACKDUCK, NO));
        Map<String, String> noCli = Bds.mapOf(Pair.of(SHOULD_RUN_CLI_SCAN, NO));
        Map<String, String> noSave = Bds.mapOf(Pair.of(SHOULD_SAVE_TO_APPLICATION_PROPERTIES, NO));

        Map<String, String> blackduckDefaults = Bds.mapOf(
            Pair.of(SHOULD_CONNECT_TO_BLACKDUCK, YES),
            Pair.of(SET_BLACKDUCK_SERVER_URL, EXPECTED_BLACKDUCK_SERVER_URL),
            Pair.of(SHOULD_TRUST_CERTS, YES),
            Pair.of(SHOULD_CONFIGURE_PROXY, YES),
            Pair.of(SET_PROXY_HOST, EXPECTED_PROXY_HOST),
            Pair.of(SET_PROXY_PORT, EXPECTED_PROXY_PORT),
            Pair.of(SET_PROXY_USERNAME, EXPECTED_PROXY_USERNAME),
            Pair.of(SHOULD_SET_PROXY_PASSWORD, YES),
            Pair.of(SET_PROXY_PASSWORD, EXPECTED_PROXY_PASSWORD),
            Pair.of(SHOULD_SET_PROXY_NTLM, YES),
            Pair.of(SET_PROXY_NTLM_DOMAIN, EXPECTED_PROXY_NTLM_DOMAIN),
            Pair.of(SET_PROXY_NTLM_WORKSTATION, EXPECTED_PROXY_NTLM_WORKSTATION),
            Pair.of(SHOULD_TEST_CONNECTION, NO),
            Pair.of(SHOULD_SET_PROJECT_NAME_VERSON, YES),
            Pair.of(SET_PROJECT_NAME, EXPECTED_PROJECT_NAME),
            Pair.of(SET_PROJECT_VERSION, EXPECTED_VERSION_NAME)
        );

        Map<String, String> blackduckUseApiToken = Bds.mapOf(
            Pair.of(SHOULD_USE_API_TOKEN, YES),
            Pair.of(SET_API_TOKEN, EXPECTED_BLACKDUCK_API_TOKEN)
        );

        Map<String, String> blackduckUseUsernamePassword = Bds.mapOf(
            Pair.of(SHOULD_USE_API_TOKEN, NO),
            Pair.of(SET_USERNAME, EXPECTED_BLACKDUCK_USERNAME),
            Pair.of(SHOULD_SET_PASSWORD, YES),
            Pair.of(SET_PASSWORD, EXPECTED_BLACKDUCK_PASSWORD)
        );

        Map<String, String> blackduckAttemptConnection = Bds.mapOf(
            Pair.of(SHOULD_TEST_CONNECTION, YES),
            Pair.of(SHOULD_RETRY_CONNECTION, NO)
        );

        Map<String, String> downloadCliDefaults = Bds.mapOf(
            Pair.of(SHOULD_RUN_CLI_SCAN, YES),
            Pair.of(CliDecisionBranch.SHOULD_UPLOAD_TO_BLACK_DUCK, YES),
            Pair.of(CliDecisionBranch.SHOULD_USE_CUSTOM_SCANNER, YES),
            Pair.of(CliDecisionBranch.SHOULD_DOWNLOAD_CUSTOM_SCANNER, YES),
            Pair.of(CliDecisionBranch.SET_SCANNER_HOST_URL, EXPECTED_SCANNER_HOST_URL)
        );

        Map<String, String> localCliDefaults = Bds.mapOf(
            Pair.of(SHOULD_RUN_CLI_SCAN, YES),
            Pair.of(CliDecisionBranch.SHOULD_UPLOAD_TO_BLACK_DUCK, NO),
            Pair.of(CliDecisionBranch.SHOULD_USE_CUSTOM_SCANNER, YES),
            Pair.of(CliDecisionBranch.SHOULD_DOWNLOAD_CUSTOM_SCANNER, NO),
            Pair.of(CliDecisionBranch.SET_SCANNER_OFFLINE_LOCAL_PATH, EXPECTED_SCANNER_LOCAL_PATH)
        );

        Map<String, String> doNotBlackduckCliOrSave = new HashMap<>();
        doNotBlackduckCliOrSave.putAll(noBlackduck);
        doNotBlackduckCliOrSave.putAll(noCli);
        doNotBlackduckCliOrSave.putAll(noSave);

        Map<String, String> connectToBlackduckApiToken = new HashMap<>();
        connectToBlackduckApiToken.putAll(blackduckDefaults);
        connectToBlackduckApiToken.putAll(blackduckUseApiToken);
        connectToBlackduckApiToken.putAll(noCli);
        connectToBlackduckApiToken.putAll(noSave);

        Map<String, String> connectToBlackduckUsernamePassword = new HashMap<>();
        connectToBlackduckUsernamePassword.putAll(blackduckDefaults);
        connectToBlackduckUsernamePassword.putAll(blackduckUseUsernamePassword);
        connectToBlackduckUsernamePassword.putAll(noCli);
        connectToBlackduckUsernamePassword.putAll(noSave);

        Map<String, String> failToConnectToBlackduck = new HashMap<>();
        failToConnectToBlackduck.putAll(blackduckDefaults);
        failToConnectToBlackduck.putAll(blackduckUseApiToken);
        failToConnectToBlackduck.putAll(blackduckAttemptConnection);
        failToConnectToBlackduck.putAll(noCli);
        failToConnectToBlackduck.putAll(noSave);

        Map<String, String> downloadCli = new HashMap<>();
        downloadCli.putAll(blackduckDefaults);
        downloadCli.putAll(blackduckUseApiToken);
        downloadCli.putAll(downloadCliDefaults);
        downloadCli.putAll(noSave);

        Map<String, String> localCli = new HashMap<>();
        localCli.putAll(blackduckDefaults);
        localCli.putAll(blackduckUseApiToken);
        localCli.putAll(localCliDefaults);
        localCli.putAll(noSave);

        return Stream.of(
            Arguments.of(
                doNotBlackduckCliOrSave,
                Bds.mapOf(
                    Pair.of(DetectProperties.DETECT_TOOLS_EXCLUDED, DetectTool.SIGNATURE_SCAN.toString()),
                    Pair.of(DetectProperties.BLACKDUCK_OFFLINE_MODE, Boolean.TRUE.toString())
                )
            ),
            Arguments.of(
                connectToBlackduckApiToken,
                Bds.mapOf(
                    Pair.of(DetectProperties.BLACKDUCK_URL, EXPECTED_BLACKDUCK_SERVER_URL),
                    Pair.of(DetectProperties.BLACKDUCK_API_TOKEN, EXPECTED_BLACKDUCK_API_TOKEN),
                    Pair.of(DetectProperties.BLACKDUCK_TRUST_CERT, Boolean.TRUE.toString()),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_HOST, EXPECTED_PROXY_HOST),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_PORT, EXPECTED_PROXY_PORT),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_USERNAME, EXPECTED_PROXY_USERNAME),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_PASSWORD, EXPECTED_PROXY_PASSWORD),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_NTLM_DOMAIN, EXPECTED_PROXY_NTLM_DOMAIN),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_NTLM_WORKSTATION, EXPECTED_PROXY_NTLM_WORKSTATION),
                    Pair.of(DetectProperties.DETECT_PROJECT_NAME, EXPECTED_PROJECT_NAME),
                    Pair.of(DetectProperties.DETECT_PROJECT_VERSION_NAME, EXPECTED_VERSION_NAME),
                    Pair.of(DetectProperties.DETECT_TOOLS_EXCLUDED, DetectTool.SIGNATURE_SCAN.toString())
                )
            ),
            Arguments.of(
                connectToBlackduckUsernamePassword,
                Bds.mapOf(
                    Pair.of(DetectProperties.BLACKDUCK_URL, EXPECTED_BLACKDUCK_SERVER_URL),
                    Pair.of(DetectProperties.BLACKDUCK_USERNAME, EXPECTED_BLACKDUCK_USERNAME),
                    Pair.of(DetectProperties.BLACKDUCK_PASSWORD, EXPECTED_BLACKDUCK_PASSWORD),
                    Pair.of(DetectProperties.BLACKDUCK_TRUST_CERT, Boolean.TRUE.toString()),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_HOST, EXPECTED_PROXY_HOST),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_PORT, EXPECTED_PROXY_PORT),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_USERNAME, EXPECTED_PROXY_USERNAME),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_PASSWORD, EXPECTED_PROXY_PASSWORD),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_NTLM_DOMAIN, EXPECTED_PROXY_NTLM_DOMAIN),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_NTLM_WORKSTATION, EXPECTED_PROXY_NTLM_WORKSTATION),
                    Pair.of(DetectProperties.DETECT_PROJECT_NAME, EXPECTED_PROJECT_NAME),
                    Pair.of(DetectProperties.DETECT_PROJECT_VERSION_NAME, EXPECTED_VERSION_NAME),
                    Pair.of(DetectProperties.DETECT_TOOLS_EXCLUDED, DetectTool.SIGNATURE_SCAN.toString())
                )
            ),
            Arguments.of(
                failToConnectToBlackduck,
                Bds.mapOf(
                    Pair.of(DetectProperties.BLACKDUCK_URL, EXPECTED_BLACKDUCK_SERVER_URL),
                    Pair.of(DetectProperties.BLACKDUCK_API_TOKEN, EXPECTED_BLACKDUCK_API_TOKEN),
                    Pair.of(DetectProperties.BLACKDUCK_TRUST_CERT, Boolean.TRUE.toString()),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_HOST, EXPECTED_PROXY_HOST),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_PORT, EXPECTED_PROXY_PORT),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_USERNAME, EXPECTED_PROXY_USERNAME),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_PASSWORD, EXPECTED_PROXY_PASSWORD),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_NTLM_DOMAIN, EXPECTED_PROXY_NTLM_DOMAIN),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_NTLM_WORKSTATION, EXPECTED_PROXY_NTLM_WORKSTATION),
                    Pair.of(DetectProperties.DETECT_PROJECT_NAME, EXPECTED_PROJECT_NAME),
                    Pair.of(DetectProperties.DETECT_PROJECT_VERSION_NAME, EXPECTED_VERSION_NAME),
                    Pair.of(DetectProperties.DETECT_TOOLS_EXCLUDED, DetectTool.SIGNATURE_SCAN.toString())
                )
            ),
            Arguments.of(
                downloadCli,
                Bds.mapOf(
                    Pair.of(DetectProperties.BLACKDUCK_URL, EXPECTED_BLACKDUCK_SERVER_URL),
                    Pair.of(DetectProperties.BLACKDUCK_API_TOKEN, EXPECTED_BLACKDUCK_API_TOKEN),
                    Pair.of(DetectProperties.BLACKDUCK_TRUST_CERT, Boolean.TRUE.toString()),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_HOST, EXPECTED_PROXY_HOST),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_PORT, EXPECTED_PROXY_PORT),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_USERNAME, EXPECTED_PROXY_USERNAME),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_PASSWORD, EXPECTED_PROXY_PASSWORD),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_NTLM_DOMAIN, EXPECTED_PROXY_NTLM_DOMAIN),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_NTLM_WORKSTATION, EXPECTED_PROXY_NTLM_WORKSTATION),
                    Pair.of(DetectProperties.DETECT_PROJECT_NAME, EXPECTED_PROJECT_NAME),
                    Pair.of(DetectProperties.DETECT_PROJECT_VERSION_NAME, EXPECTED_VERSION_NAME),
                    Pair.of(DetectProperties.DETECT_BLACKDUCK_SIGNATURE_SCANNER_HOST_URL, EXPECTED_SCANNER_HOST_URL)
                )
            ),
            Arguments.of(
                localCli,
                Bds.mapOf(
                    Pair.of(DetectProperties.BLACKDUCK_URL, EXPECTED_BLACKDUCK_SERVER_URL),
                    Pair.of(DetectProperties.BLACKDUCK_API_TOKEN, EXPECTED_BLACKDUCK_API_TOKEN),
                    Pair.of(DetectProperties.BLACKDUCK_TRUST_CERT, Boolean.TRUE.toString()),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_HOST, EXPECTED_PROXY_HOST),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_PORT, EXPECTED_PROXY_PORT),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_USERNAME, EXPECTED_PROXY_USERNAME),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_PASSWORD, EXPECTED_PROXY_PASSWORD),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_NTLM_DOMAIN, EXPECTED_PROXY_NTLM_DOMAIN),
                    Pair.of(DetectProperties.BLACKDUCK_PROXY_NTLM_WORKSTATION, EXPECTED_PROXY_NTLM_WORKSTATION),
                    Pair.of(DetectProperties.DETECT_PROJECT_NAME, EXPECTED_PROJECT_NAME),
                    Pair.of(DetectProperties.DETECT_PROJECT_VERSION_NAME, EXPECTED_VERSION_NAME),
                    Pair.of(DetectProperties.DETECT_BLACKDUCK_SIGNATURE_SCANNER_DRY_RUN, Boolean.TRUE.toString()),
                    Pair.of(DetectProperties.DETECT_BLACKDUCK_SIGNATURE_SCANNER_OFFLINE_LOCAL_PATH, EXPECTED_SCANNER_LOCAL_PATH)

                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("getTraversalData")
    public void testTraverse(Map<String, String> callToResponse, Map<DetectProperty<?>, String> expectedProperties) {
        InteractiveModeDecisionTree decisionTree = new InteractiveModeDecisionTree(new ArrayList<>());

        InteractiveWriter mockWriter = mockWriter(callToResponse);
        InteractivePropertySourceBuilder propertySourceBuilder = new InteractivePropertySourceBuilder(mockWriter);

        decisionTree.traverse(propertySourceBuilder, mockWriter);

        MapPropertySource actualPropertySource = propertySourceBuilder.build();

        expectedProperties.forEach((key, value) -> assertHasPropertyWithValue(actualPropertySource, key, value));

        assertEquals(expectedProperties.keySet().size(), actualPropertySource.getKeys().size());
    }

    private InteractiveWriter mockWriter(Map<String, String> callToResponse) {
        InteractiveWriter mockWriter = Mockito.mock(InteractiveWriter.class);

        // All askYesOrNo call askYesOrNoWithMessage, no sense in stubbing out both -- rotte OCT 2020
        Mockito.when(mockWriter.askYesOrNo(Mockito.any())).thenCallRealMethod();

        callToResponse.forEach((k, v) -> {
            if (YES.equals(v)) {
                Mockito.when(mockWriter.askYesOrNoWithMessage(Mockito.contains(k), Mockito.any())).thenReturn(Boolean.TRUE);
            } else if (NO.equals(v)) {
                Mockito.when(mockWriter.askYesOrNoWithMessage(Mockito.contains(k), Mockito.any())).thenReturn(Boolean.FALSE);
            } else {
                Mockito.when(mockWriter.askQuestion(k)).thenReturn(v);
                Mockito.when(mockWriter.askSecretQuestion(k)).thenReturn(v);
            }
        });

        return mockWriter;
    }

    private void assertHasPropertyWithValue(MapPropertySource mapPropertySource, DetectProperty<?> detectProperty, String value) {
        String detectPropertyKey = detectProperty.getProperty().getKey();
        assertTrue(mapPropertySource.hasKey(detectPropertyKey), "Actual properties were missing " + detectPropertyKey);
        assertEquals(mapPropertySource.getValue(detectPropertyKey), value);
    }

}
