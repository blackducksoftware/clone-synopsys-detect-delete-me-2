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
package com.synopsys.integration.configuration.property.types.string

import com.synopsys.integration.configuration.parse.ListValueParser
import com.synopsys.integration.configuration.property.base.NullableProperty
import com.synopsys.integration.configuration.property.base.ValuedListProperty
import com.synopsys.integration.configuration.property.base.ValuedProperty

class NullableStringProperty(key: String) : NullableProperty<String>(key, StringValueParser()) {
    override fun describeType(): String? = "Optional String"
    override fun listExampleValues(): List<String>? = listOf("abc", "example-value")
}

class StringProperty(key: String, default: String) : ValuedProperty<String>(key, StringValueParser(), default) {
    override fun describeDefault(): String? = default
    override fun describeType(): String? = "String"
    override fun listExampleValues(): List<String>? = listOf("abc", "example-value")
}

class StringListProperty(key: String, default: List<String>) : ValuedListProperty<String>(key, ListValueParser(StringValueParser()), default) {
    override fun describeType(): String? = "String List"
    override fun listExampleValues(): List<String>? = listOf("example", "example-value,example-value-2")
}