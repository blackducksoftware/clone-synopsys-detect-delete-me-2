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
package com.synopsys.integration.configuration.property.types.enumsoft

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SoftEnumValueParserTest {
    private enum class Example {
        THING,
        ANOTHER,
        THIRD
    }

    @ParameterizedTest
    @ValueSource(strings = ["unknown", "Thing ", " THING"])
    fun unknownValues(expectedValue: String) {
        val resolvedValue = SoftEnumValueParser(Example::class.java).parse(expectedValue);
        if (resolvedValue.softValue.isPresent) {
            Assertions.assertEquals(expectedValue, resolvedValue.softValue.get(), "Should parse value to a soft string.")
        } else {
            fail("Should have resolved to an soft string and not an enum.")
        }
    }

    @Test
    fun parsesEnumValue() {
        fun assertValidSoftEnum(expectedValue: Example, rawValue: String) {
            val actualValue = SoftEnumValueParser(Example::class.java).parse(rawValue)
            Assertions.assertEquals(SoftEnumValue.ofEnumValue(expectedValue), actualValue)
            Assertions.assertEquals(rawValue, actualValue.toString())
        }

        assertValidSoftEnum(Example.THING, "THING")
        assertValidSoftEnum(Example.ANOTHER, "ANOTHER")
        assertValidSoftEnum(Example.THIRD, "THIRD")
    }
}