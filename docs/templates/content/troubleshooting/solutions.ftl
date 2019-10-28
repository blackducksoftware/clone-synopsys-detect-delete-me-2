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

## Docker Inspector error fails after logging: "The Black Duck url must be specified"

### Symptom

When running a version of ${solution_name} prior to ${solution_name} version 5.6.0, the ${solution_name} Status block reports DOCKER: FAILURE, and the following error appears in the Docker Inspector log:
Docker Inspector error: Error inspecting image: The Black Duck url must be specified. Either an API token or a username/password must be specified.

### Possible cause

${solution_name} 5.5.1 and earlier have a bug that prevent them from working with Docker Inspector 8.2.0 and newer. The fix is in ${solution_name} 5.6.0.

### Solution

There are two possible solutions:
1. Upgrade to ${solution_name} 5.6.0 or newer, or:
1. Configure ${solution_name} to use Docker Inspector 8.1.6 with the argument: --detect.docker.inspector.version=8.1.6

## ${solution_name} fails and a TRACE log shows an HTTP response from Black Duck of "402 Payment Required" or "502 Bad Gateway"

### Symptom

${solution_name} fails, and a TRACE log contains "402 Payment Required" or "502 Bad Gateway".

### Possible cause

Black Duck does not have a required feature (notifications, binary analysis, etc.) enabled.

### Solution

Enable the required feature on the Black Duck server.