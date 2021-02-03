/**
 * detectable
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
package com.synopsys.integration.detectable.detectables.yarn.parse.entry;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.detectable.detectables.yarn.parse.YarnLockLineAnalyzer;
import com.synopsys.integration.detectable.detectables.yarn.parse.entry.section.YarnLockEntrySectionParserSet;

public class YarnLockEntryParser {
    private final YarnLockLineAnalyzer yarnLockLineAnalyzer;
    private final YarnLockEntrySectionParserSet yarnLockEntryElementParser;

    public YarnLockEntryParser(YarnLockLineAnalyzer yarnLockLineAnalyzer, YarnLockEntrySectionParserSet yarnLockEntryElementParser) {
        this.yarnLockLineAnalyzer = yarnLockLineAnalyzer;
        this.yarnLockEntryElementParser = yarnLockEntryElementParser;
    }

    public YarnLockEntryParseResult parseNextEntry(List<String> yarnLockFileLines, int entryStartIndex) {
        YarnLockEntryBuilder yarnLockEntryBuilder = new YarnLockEntryBuilder();
        int fileLineIndex = entryStartIndex;
        int entryLineIndex = 0;
        while (fileLineIndex < yarnLockFileLines.size()) {
            String curLine = yarnLockFileLines.get(fileLineIndex);
            if (finishedWithThisEntry(entryLineIndex, curLine)) {
                return createResult(fileLineIndex, yarnLockEntryBuilder);
            }
            // parseElement returns the last line it consumed; parsing resumes on the next line
            fileLineIndex = yarnLockEntryElementParser.parseElement(yarnLockEntryBuilder, yarnLockFileLines, fileLineIndex);
            entryLineIndex++;
            fileLineIndex++;
        }
        Optional<YarnLockEntry> entry = yarnLockEntryBuilder.build();
        return new YarnLockEntryParseResult(yarnLockFileLines.size() - 1, entry.orElse(null));
    }

    private YarnLockEntryParseResult createResult(int fileLineIndex, YarnLockEntryBuilder entryBuilder) {
        Optional<YarnLockEntry> entry = entryBuilder.build();
        return new YarnLockEntryParseResult(fileLineIndex - 1, entry.orElse(null));
    }

    private boolean finishedWithThisEntry(int entryLineIndex, String curLine) {
        if (entryLineIndex == 0) {
            // we're still on the first line of the entry, so can't be done yet
            return false;
        }
        // If we've left the indented lines, we're done parsing this entry
        int indentDepth = yarnLockLineAnalyzer.measureIndentDepth(curLine);
        return (indentDepth == 0);
    }
}
