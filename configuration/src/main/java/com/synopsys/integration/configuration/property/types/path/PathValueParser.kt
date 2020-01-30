/**
 * configuration
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.configuration.property.types.path

import com.synopsys.integration.configuration.parse.ValueParseException
import com.synopsys.integration.configuration.parse.ValueParser
import org.apache.commons.lang3.StringUtils
import java.nio.file.Path

class PathValueParser : ValueParser<PathValue>() {
    override fun parse(value: String): PathValue {
        val trimmedValue = value.trim()
        if (StringUtils.isNotBlank(trimmedValue)) {
            return PathValue(trimmedValue)
        } else {
            throw ValueParseException(trimmedValue, "Path", "A path must have at least one non-whitespace character!")
        }
    }
}

data class PathValue(private val value: String) {
    fun resolvePath(pathResolver: PathResolver): Path? {
        return pathResolver.resolvePath(value)
    }

    override fun toString(): String {
        return value
    }
}


