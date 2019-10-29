# Solutions to common problems

## DETECT_SOURCE was not set or computed correctly

### Symptom

detect.sh fails with: DETECT_SOURCE was not set or computed correctly, please check your configuration and environment.

### Possible cause

detect.sh is trying to execute this command:
````
curl --silent --header \"X-Result-Detail: info\" https://sig-repo.synopsys.com/api/storage/bds-integrations-release/com/synopsys/integration/synopsys-detect?properties=DETECT_LATEST
````
The response to this command should be similar to:
```
{
"properties" : {
"DETECT_LATEST" : [ "https://sig-repo.synopsys.com/bds-integrations-release/com/synopsys/integration/synopsys-detect/5.6.1/synopsys-detect-5.6.1.jar" ]
},
"uri" : "https://sig-repo.synopsys.com/api/storage/bds-integrations-release/com/synopsys/integration/synopsys-detect"
}
```
When that command does not successfully return a value for property DETECT_LATEST, detect.sh reports:
````
DETECT_SOURCE was not set or computed correctly, please check your configuration and environment.
````

### Solution

If the curl command described above does not successfully return a value for property DETECT_LATEST, you must determine why, and make the changes necessary so that curl command works.

## Docker Inspector error fails after logging: "The ${blackduck_product_name} url must be specified"

### Symptom

When running a version of ${solution_name} prior to ${solution_name} version 5.6.0, the ${solution_name} Status block reports DOCKER: FAILURE, and the following error appears in the Docker Inspector log:
Docker Inspector error: Error inspecting image: The ${blackduck_product_name} url must be specified. Either an API token or a username/password must be specified.

### Possible cause

${solution_name} 5.5.1 and earlier have a bug that prevent them from working with Docker Inspector 8.2.0 and newer. The fix is in ${solution_name} 5.6.0.

### Solution

There are two possible solutions:
1. Upgrade to ${solution_name} 5.6.0 or newer, or:
1. Configure ${solution_name} to use Docker Inspector 8.1.6 with the argument: --detect.docker.inspector.version=8.1.6

## ${solution_name} fails and a TRACE log shows an HTTP response from ${blackduck_product_name} of "402 Payment Required" or "502 Bad Gateway"

### Symptom

${solution_name} fails, and a TRACE log contains "402 Payment Required" or "502 Bad Gateway".

### Possible cause

${blackduck_product_name} does not have a required feature (notifications, binary analysis, etc.) enabled.

### Solution

Enable the required feature on the ${blackduck_product_name} server.

## Unexpected behavior running ${solution_name} on a project that uses Spring Boot

### Symptom

Unexpected behavior, and/or unexpected property values shown in the log.

### Possible cause

If your source directory contains Spring Framework configuration files named application.properties, application.yml,
or application.xml that are written for any application other than ${solution_name}, you should not run ${solution_name} from your source directory.

### Solution

To prevent ${solution_name} from reading those files, run ${solution_name} from a different directory. Use the following property to point to your source directory.
```
--detect.source.path={project directory path}
```