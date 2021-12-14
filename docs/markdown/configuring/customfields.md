# Setting [blackduck_product_name] custom fields

The information in this section applies to properties 
detect.custom.fields.project and
detect.custom.fields.version.

Use these properties to set custom field values on the [blackduck_product_name] projects (detect.custom.fields.project) and project versions (detect.custom.fields.version)
that [solution_name] creates or updates.
In each case, you supply a list of custom field labels (names), and a corresponding list of custom field values.
For each, [solution_name] requests that [blackduck_product_name] assign the value to the field.

The example below focuses on property detect.custom.fields.version, but the same concepts and rules also apply to property detect.custom.fields.project.

For example, to set the value of Project Version custom field "TBD" (defined in [blackduck_product_name] as a text field) to "TBD", set the following properties:
````
--detect.custom.fields.version[0].label=TBD
--detect.custom.fields.version[0].value=TBD
````
Two set additional Project Version custom field values in the same [solution_name] run, add additional property pairs, incrementing the index within the square
brackets for each additional custom field. For example, to set the value of "TBD" to "TBD" and set the value of "TBD" to "TBD", set the following properties:
````
--detect.custom.fields.version[0].label=TBD
--detect.custom.fields.version[0].value=TBD
--detect.custom.fields.version[1].label=TBD
--detect.custom.fields.version[1].value=TBD
````

multivalue example:




***** also mention that "set every time" property: that applies too?
