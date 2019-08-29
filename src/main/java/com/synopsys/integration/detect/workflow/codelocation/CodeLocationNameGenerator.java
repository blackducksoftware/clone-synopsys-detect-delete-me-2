/**
 * synopsys-detect
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
package com.synopsys.integration.detect.workflow.codelocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.misc.Nullable;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.detect.workflow.file.DetectFileUtils;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocationId;

public class CodeLocationNameGenerator {
    private final String PIECES_SEPARATOR = "/";
    private final Map<String, Integer> nameCounters = new HashMap<>();
    private static final int MAXIMUM_CODE_LOCATION_NAME_LENGTH = 250;

    private final String codeLocationNameOverride;

    public CodeLocationNameGenerator(@Nullable final String codeLocationNameOverride) {
        this.codeLocationNameOverride = codeLocationNameOverride;
    }

    public String createBomCodeLocationName(final String detectSourcePath, final String sourcePath, final String projectName, final String projectVersionName, final DetectCodeLocation detectCodeLocation, final String prefix,
        final String suffix) {
        final String pathPiece = FileNameUtils.relativize(detectSourcePath, sourcePath);
        final String projectVersionNamePiece = projectName + PIECES_SEPARATOR + projectVersionName;
        final List<String> idPieces = detectCodeLocation.getCodeLocationId()
                                          .map(CodeLocationId::getIdPieces)
                                          .orElse(Collections.emptyList());
        final String idPiece = idPieces.stream()
                                   .map(String::trim)
                                   .filter(piece -> !projectName.equals(piece))
                                   .filter(piece -> !projectVersionName.equals(piece))
                                   .collect(Collectors.joining(PIECES_SEPARATOR));
        final List<String> bomCodeLocationNamePieces = Arrays.asList(projectVersionNamePiece, pathPiece, idPiece);

        // misc pieces
        final String codeLocationTypeString = CodeLocationNameType.BOM.toString().toLowerCase();
        final String bomToolTypeString = deriveCreator(detectCodeLocation).toLowerCase();
        final List<String> bomCodeLocationEndPieces = Arrays.asList(bomToolTypeString, codeLocationTypeString);

        return createCodeLocationName(prefix, bomCodeLocationNamePieces, suffix, bomCodeLocationEndPieces);
    }

    public String createDockerCodeLocationName(final String sourcePath, final String projectName, final String projectVersionName, final String dockerImage, final String prefix,
        final String suffix) {
        final String finalSourcePathPiece = DetectFileUtils.extractFinalPieceFromPath(sourcePath);
        final String codeLocationTypeString = getCodeLocationTypeString(CodeLocationNameType.DOCKER);
        final String bomToolTypeString = "docker";

        final List<String> dockerCodeLocationNamePieces = Arrays.asList(finalSourcePathPiece, projectName, projectVersionName, dockerImage);
        final List<String> dockerCodeLocationEndPieces = Arrays.asList(codeLocationTypeString, bomToolTypeString);

        return createCodeLocationName(prefix, dockerCodeLocationNamePieces, suffix, dockerCodeLocationEndPieces);
    }

    public String createDockerScanCodeLocationName(final String dockerTarFilename, final String projectName, final String projectVersionName, final String prefix, final String suffix) {
        final String codeLocationTypeString = getCodeLocationTypeString(CodeLocationNameType.SCAN);

        final List<String> fileCodeLocationNamePieces = Arrays.asList(dockerTarFilename, projectName, projectVersionName);
        final List<String> fileCodeLocationEndPieces = Collections.singletonList(codeLocationTypeString);

        return createCodeLocationName(prefix, fileCodeLocationNamePieces, suffix, fileCodeLocationEndPieces);
    }

    public String createScanCodeLocationName(final String sourcePath, final String scanTargetPath, final String projectName, final String projectVersionName, final String prefix, final String suffix) {
        final String pathPiece = cleanScanTargetPath(scanTargetPath, sourcePath);
        final String codeLocationTypeString = getCodeLocationTypeString(CodeLocationNameType.SCAN);

        final List<String> fileCodeLocationNamePieces = Arrays.asList(pathPiece, projectName, projectVersionName);
        final List<String> fileCodeLocationEndPieces = Collections.singletonList(codeLocationTypeString);

        return createCodeLocationName(prefix, fileCodeLocationNamePieces, suffix, fileCodeLocationEndPieces);
    }

    public String createBinaryScanCodeLocationName(final String filename, final String projectName, final String projectVersionName, final String prefix, final String suffix) {
        final String codeLocationTypeString = getCodeLocationTypeString(CodeLocationNameType.SCAN);

        final List<String> fileCodeLocationNamePieces = Arrays.asList(filename, projectName, projectVersionName);
        final List<String> fileCodeLocationEndPieces = Collections.singletonList(codeLocationTypeString);

        return createCodeLocationName(prefix, fileCodeLocationNamePieces, suffix, fileCodeLocationEndPieces);
    }

    private String getCodeLocationTypeString(final CodeLocationNameType codeLocationNameType) {
        return codeLocationNameType.toString().toLowerCase();
    }

    private String createCodeLocationName(final String prefix, final List<String> codeLocationNamePieces, final String suffix, final List<String> codeLocationEndPieces) {
        String codeLocationName = createCommonName(prefix, codeLocationNamePieces, suffix, codeLocationEndPieces);

        if (codeLocationName.length() > MAXIMUM_CODE_LOCATION_NAME_LENGTH) {
            codeLocationName = createShortenedCodeLocationName(codeLocationNamePieces, prefix, suffix, codeLocationEndPieces);
        }

        return codeLocationName;
    }

    private String cleanScanTargetPath(final String scanTargetPath, final String sourcePath) {
        final String finalSourcePathPiece = DetectFileUtils.extractFinalPieceFromPath(sourcePath);
        String cleanedTargetPath = "";
        if (StringUtils.isNotBlank(scanTargetPath) && StringUtils.isNotBlank(finalSourcePathPiece)) {
            cleanedTargetPath = scanTargetPath.replace(sourcePath, finalSourcePathPiece);
        }

        return cleanedTargetPath;
    }

    private String createShortenedCodeLocationName(final List<String> namePieces, final String prefix, final String suffix, final List<String> endPieces) {
        final List<String> shortenedNamePieces = namePieces.stream().map(this::shortenPiece).collect(Collectors.toList());

        final String shortenedPrefix = shortenPiece(prefix);
        final String shortenedSuffix = shortenPiece(suffix);

        return createCommonName(shortenedPrefix, shortenedNamePieces, shortenedSuffix, endPieces);
    }

    private String shortenPiece(final String piece) {
        if (piece.length() <= 40) {
            return piece;
        } else {
            return piece.substring(0, 19) + "..." + piece.substring(piece.length() - 18);
        }
    }

    private String createCommonName(final String prefix, final List<String> namePieces, final String suffix, final List<String> endPieces) {
        final ArrayList<String> commonNamePieces = new ArrayList<>();

        if (StringUtils.isNotBlank(prefix)) {
            commonNamePieces.add(prefix);
        }

        commonNamePieces.addAll(namePieces);

        if (StringUtils.isNotBlank(suffix)) {
            commonNamePieces.add(suffix);
        }

        final String name = StringUtils.join(commonNamePieces, PIECES_SEPARATOR);
        final String endPiece = StringUtils.join(endPieces, PIECES_SEPARATOR);

        return String.format("%s %s", name, endPiece);
    }

    public boolean useCodeLocationOverride() {
        return StringUtils.isNotBlank(codeLocationNameOverride);
    }

    public String getNextCodeLocationOverrideNameUnSourced(final CodeLocationNameType codeLocationNameType) {
        final String baseName = codeLocationNameOverride + " " + codeLocationNameType.toString().toLowerCase();
        final int nameIndex = deriveNameNumber(baseName);
        final String nextName = deriveUniqueCodeLocationName(baseName, nameIndex);
        return nextName;
    }

    public String getNextCodeLocationOverrideNameSourcedBom(final DetectCodeLocation detectCodeLocation) {
        final String creator = deriveCreator(detectCodeLocation);
        final String baseName = createBomCodeLocationName(codeLocationNameOverride, creator);

        final int nameIndex = deriveNameNumber(baseName);
        final String nextName = deriveUniqueCodeLocationName(baseName, nameIndex);
        return nextName;
    }

    public String deriveCreator(final DetectCodeLocation detectCodeLocation) {
        return detectCodeLocation.getCreatorName().orElse("detect");
    }

    private String createBomCodeLocationName(final String givenCodeLocationName, final String creatorName) {
        final String codeLocationTypeString = getCodeLocationTypeString(CodeLocationNameType.BOM);
        final String bomToolTypeString = creatorName.toLowerCase();

        final int givenNameMaxLength = MAXIMUM_CODE_LOCATION_NAME_LENGTH - bomToolTypeString.length() - codeLocationTypeString.length() - 2;
        final String adjustedGivenCodeLocationName;
        if (givenCodeLocationName.length() > givenNameMaxLength) {
            adjustedGivenCodeLocationName = givenCodeLocationName.substring(0, givenNameMaxLength);
        } else {
            adjustedGivenCodeLocationName = givenCodeLocationName;
        }
        final String codeLocationName = String.format("%s %s/%s", adjustedGivenCodeLocationName, bomToolTypeString, codeLocationTypeString);

        return codeLocationName;
    }

    private String deriveUniqueCodeLocationName(final String baseName, final int nameIndex) {
        final String nextName;
        if (nameIndex > 1) {
            nextName = baseName + " " + nameIndex;
        } else {
            nextName = baseName;
        }
        return nextName;
    }

    private int deriveNameNumber(final String baseName) {
        int nameIndex;
        if (nameCounters.containsKey(baseName)) {
            nameIndex = nameCounters.get(baseName);
            nameIndex++;
        } else {
            nameIndex = 1;
        }
        nameCounters.put(baseName, nameIndex);
        return nameIndex;
    }

}
