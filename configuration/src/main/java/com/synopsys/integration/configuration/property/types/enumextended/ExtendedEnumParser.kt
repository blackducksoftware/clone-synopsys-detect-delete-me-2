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
package com.synopsys.integration.configuration.property.types.enumextended

import com.synopsys.integration.configuration.parse.ValueParseException
import com.synopsys.integration.configuration.parse.ValueParser
import com.synopsys.integration.configuration.property.types.enums.ValueOfOrNullParser

class ExtendedEnumValueOfParser<E, B>(private val valueOfE: (String) -> E?, private val valueOfB: (String) -> B?) : ValueParser<ExtendedEnumValue<E, B>>() {
    var extendedParser = ValueOfOrNullParser(valueOfE)
    var baseParser = ValueOfOrNullParser(valueOfB)

    override fun parse(value: String): ExtendedEnumValue<E, B> {
        val eValue = extendedParser.parse(value);
        if (eValue != null) {
            return ExtendedValue(eValue)
        }
        val bValue = baseParser.parse(value);
        if (bValue != null) {
            return BaseValue(bValue)
        }
        throw ValueParseException(value, "either enum", additionalMessage = "Value was not a member of either enum set.")//TODO: Mention enum types?
    }
}
