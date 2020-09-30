package com.synopsys.integration.detectable.detectable.result.status;

public enum DetectorStatusCode {
    CARGO_LOCKFILE_NOT_FOUND,
    EXCEPTION,
    EXCLUDED,
    EXECUTABLE_NOT_FOUND,
    FAILED,
    FALLBACK_NOT_NEEDED,
    FILES_NOT_FOUND,
    FILE_NOT_FOUND,
    FORCED_NESTED_PASSED,
    GO_PKG_LOCKFILE_NOT_FOUND,
    INSPECTOR_NOT_FOUND,
    MAX_DEPTH_EXCEEDED,
    NOT_NESTABLE,
    NOT_SELF_NESTABLE,
    NPM_NODE_MODULES_NOT_FOUND,
    PASSED,
    POETRY_LOCKFILE_NOT_FOUND,
    PROPERTY_INSUFFICIENT,
    WRONG_OPERATING_SYSTEM_RESULT,
    YIELDED
}
