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
package com.synopsys.integration.detectable.detectables.yarn.parse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detectable.detectables.yarn.parse.entry.YarnLockEntry;
import com.synopsys.integration.detectable.detectables.yarn.parse.entry.YarnLockEntryParseResult;
import com.synopsys.integration.detectable.detectables.yarn.parse.entry.YarnLockEntryParser;

public class YarnLockParserNew {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final YarnLockEntryParser yarnLockNodeParser;

    public YarnLockParserNew(YarnLockEntryParser yarnLockNodeParser) {
        this.yarnLockNodeParser = yarnLockNodeParser;
    }

    public YarnLock parseYarnLock(List<String> yarnLockFileAsList) {
        List<YarnLockEntry> entries = new ArrayList<>();
        int lineIndex = 0;
        while (lineIndex < yarnLockFileAsList.size()) {
            String line = yarnLockFileAsList.get(lineIndex);
            logger.trace("Parsing line: {}: {}", lineIndex + 1, line);
            // Parse the entire entry
            YarnLockEntryParseResult entryParseResult = yarnLockNodeParser.parseEntry(yarnLockFileAsList, lineIndex);
            entryParseResult.getYarnLockEntry().ifPresent(entry -> entries.add(entry));
            lineIndex = entryParseResult.getLastParsedLineIndex();
            lineIndex++;
        }
        return new YarnLock(entries);
    }
}
