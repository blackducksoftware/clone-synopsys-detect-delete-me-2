# Lerna support

The Lerna detector will register in the presence of a lerna.json file.

It will then execute a lerna command to retrieve all the packages defined in the project.

Each package has a location within the project structure.

It is expected to find a package.json and some type of lock file.
Supported lockfile types are package-lock.json, npm-shrinkwrap.json, and yarn.lock.

If no lockfile is present in the package, it will be assumed that all the dependencies defined within the package's package.json file will be resolved in the lockfile at the root of the project.
If no lockfile is present at the root of the project, Lerna extraction will fail.

## Extracting from package-lock.json

The Lerna detect will execute the same code as the [NPM package lock detector](../../../advanced/package-managers/npm/#npm-package-lock).

The [NPM package lock detector](../../../properties/detectors/npm/) related properties also apply.

Since the Lerna detector is currently not using the NPM Cli, only the [detect.npm.include.dev.dependencies](../../../properties/detectors/npm/#include-npm-development-dependencies) property applies.

## Extracting from npm-shrinkwrap.json

The Lerna detect will execute the same code as the [NPM shrinkwrap detector](../../../advanced/package-managers/npm/#npm-shrinkwrap).

The [NPM shrinkwrap detector](../../../properties/detectors/npm/) related properties also apply.

Since the Lerna detector is currently not using the NPM Cli, only the [detect.npm.include.dev.dependencies](../../../properties/detectors/npm/#include-npm-development-dependencies) property applies.

## Extracting from yarn.lock

The Lerna detect will execute the same code as the [Yarn detector](../../../advanced/package-managers/yarn/#yarn-support).

The [Yarn detector related properties](../../../properties/detectors/yarn/) also apply.

## Private packages

With the [detect.lerna.include.private](../../../properties/detectors/lerna/#include-lerna-packages-defined-as-private46) property, users can specify whether or not to include private packages as defined by Lerna.

## Lerna path

${solution_name} executes commands against the Lerna executable to determine package information.

${solution_name} will attempt to find the Lerna executable, but if the user wishes to override the executable Detect uses, they can supply a path to the executable using [detect.lerna.path](../../../detectors/lerna/#lerna-executable)