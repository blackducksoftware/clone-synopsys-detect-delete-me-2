package com.synopsys.integration.configuration.help

import com.synopsys.integration.configuration.config.PropertyConfiguration
import com.synopsys.integration.configuration.property.Property
import com.synopsys.integration.configuration.property.base.NullableProperty
import com.synopsys.integration.configuration.property.base.TypedProperty
import com.synopsys.integration.configuration.property.base.ValuedProperty
import org.apache.commons.lang3.StringUtils
import java.util.function.Consumer

//The idea is that this is here to help you log information about a particular property configuration with particular things you want to express.
//  For example you may want to log deprecation warning when a particular property is set.
//  For example you may want to THROW when a particular deprecated property is set.
//  For example you may want to log when an invalid value is set.
//  For example you may want to THROW when an invalid value is set.
//  For example you may want to log help about specific properties.
//  For example you may want to search properties by key and log help.

//Maybe split into 'ValueContext' and a 'HelpContext' 
class PropertyConfigurationHelpContext(val propertyConfiguration: PropertyConfiguration) {
    fun sourceDisplayNames(): Map<String, String> {
        return mapOf(
                "configurationProperties" to "cfg",
                "systemEnvironment" to "env",
                "commandLineArgs" to "cmd",
                "systemProperties" to "jvm"
        )
    }

    fun printCurrentValues(logger: Consumer<String>, knownProperties: List<Property>, additionalNotes: Map<String, String>) {
        logger.accept("")
        logger.accept("Current property values:")
        logger.accept("--property = value [notes]")
        logger.accept(StringUtils.repeat("-", 60))

        val sortedProperties = knownProperties.sortedBy { property -> property.key }

        for (property in sortedProperties) {
            if (!propertyConfiguration.wasKeyProvided(property.key)) {
                continue
            }

            val value = when (property) {
                is ValuedProperty<*> -> propertyConfiguration.getValueOrDefault(property).toString()
                is NullableProperty<*> -> propertyConfiguration.getValueOrNull(property).toString()
                else -> propertyConfiguration.getRaw(property)
            }

            val containsPassword = property.key.toLowerCase().contains("password") || property.key.toLowerCase().contains("api.token") || property.key.toLowerCase().contains("access.token")
            val maskedValue = if (containsPassword) {
                StringUtils.repeat('*', value?.length ?: 0)
            } else {
                value ?: ""
            }

            val sourceName = propertyConfiguration.getPropertySource(property) ?: "unknown"
            val sourceDisplayName = sourceDisplayNames().getOrDefault(sourceName, sourceName)

            val notes = additionalNotes[property.key] ?: ""

            logger.accept(property.key + " = " + maskedValue + " [" + sourceDisplayName + "] " + notes)
        }

        logger.accept(StringUtils.repeat("-", 60))
        logger.accept("")
    }

    fun printPropertyErrors(logger: Consumer<String>, knownProperties: List<Property>, errors: Map<String, List<String>>) {
        val sortedProperties = knownProperties.sortedBy { property -> property.key }

        sortedProperties
                .filter { errors.containsKey(it.key) }
                .forEach { property ->
                    errors[property.key]?.let { errorMessages ->
                        logger.accept(StringUtils.repeat("=", 60))
                        val header = when (val size = errorMessages.size) {
                            1 -> "ERROR (1)"
                            else -> "ERRORS ($size)"
                        }
                        logger.accept(header)
                        errorMessages.forEach { errorMessage -> logger.accept(property.key + ": " + errorMessage) }
                    }
                }
    }

    fun findPropertyParseErrors(knownProperties: List<Property>): Map<String, List<String>> {
        val exceptions = mutableMapOf<String, List<String>>()
        val sortedProperties = knownProperties.sortedBy { property -> property.key }
        for (property in sortedProperties) {
            if (property is TypedProperty<*>) {
                val exception = propertyConfiguration.getPropertyException(property);
                if (exception != null) {
                    exceptions[property.key] = listOf(exception.exception.message ?: exception.toString())
                }
            }
        }
        return exceptions;
    }
}