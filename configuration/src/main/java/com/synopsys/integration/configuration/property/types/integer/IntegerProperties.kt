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
package com.synopsys.integration.configuration.property.types.integer

import com.synopsys.integration.configuration.parse.ListValueParser
import com.synopsys.integration.configuration.property.base.NullableProperty
import com.synopsys.integration.configuration.property.base.ValuedListProperty
import com.synopsys.integration.configuration.property.base.ValuedProperty

const val TYPE_DESCRIPTION = "Integer"

class NullableIntegerProperty(key: String) : NullableProperty<Int>(key, IntegerValueParser()) {
    override fun describeType(): String? = TYPE_DESCRIPTION
}

class IntegerProperty(key: String, default: Int) : ValuedProperty<Int>(key, IntegerValueParser(), default) {
    override fun describeDefault(): String? = default.toString()

    override fun describeType(): String? = TYPE_DESCRIPTION
}

class IntegerListProperty(key: String, default: List<Int>) : ValuedListProperty<Int>(key, ListValueParser(IntegerValueParser()), default) {
    override fun describeType(): String? = "$TYPE_DESCRIPTION List"
}