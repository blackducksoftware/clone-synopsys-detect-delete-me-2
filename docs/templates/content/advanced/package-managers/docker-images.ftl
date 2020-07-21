# Docker image support

On Linux and Mac, ${solution_name} can invoke Docker Inspector to inspect Linux Docker images to discover packages installed by the Linux package manager.
For simple use cases, add either ```--detect.docker.image={repo}:{tag}``` or ```--detect.docker.tar={path to a Docker saved tarfile}``` to the ${solution_name} command line.

The documentation for Docker Inspector is available [here](https://blackducksoftware.github.io/blackduck-docker-inspector/latest/overview/).

When passed a value for either detect.docker.image or detect.docker.tar,
${solution_name} runs Docker Inspector on given image (the "target image"),
creating one code location. ${solution_name} by default runs
the ${blackduck_signature_scanner_name} on the "container file system"
(the file system a container created from the image has at startup time).
Refer to [${solution_name}'s scan target](#scantarget) for more details.
This creates a second code location.

### Passing Docker Inspector property values to Docker Inspector from ${solution_name}

For more complex use cases, you may need to pass Docker Inspector property values to Docker Inspector using ${solution_name}. To do this, construct the ${solution_name} property name by prefixing the Docker Inspector property name with ```detect.docker.passthrough.```.

For example, suppose you need to set Docker Inspector's service.timeout value (the length of time Docker Inspector waits for a response from the Image Inspector services that it uses) to 480000 milliseconds. You add the prefix to the Docker Inspector property name to derive the ${solution_name} property name ```detect.docker.passthrough.service.timeout```. Therefore, add ```--detect.docker.passthrough.service.timeout=480000``` to the ${solution_name} command line.

For example:
```
./detect.sh --detect.docker.image=ubuntu:latest --detect.docker.passthrough.service.timeout=480000
```

You can set any Docker Inspector property using this method.
However, you usually should not override the values of the following Docker Inspector properties (which ${solution_name} sets)
because changing their values is likely to interfere with ${solution_name}'s ability to work with Docker Inspector:

* output.path
* output.include.squashedimage
* output.include.containerfilesystem
* upload.bdio

<a name="scantarget"></a>
### ${solution_name}'s scan target

When a Docker image is run; for example, using a `docker run` command, a container is created. That container has a file system; in other words, the container file system. The container file system at the instant the container is created; in other words, the initial container file system, can be determined in advance from the image without running the image. Because the target image is not yet trusted, Docker Inspector does not run the image; that is, it does not create a container from the image, but it does construct the initial container file system, which is the file system a container has at the instant it is created.

When ${solution_name} invokes both Docker Inspector because either detect.docker.image or detect.docker.tar is set, and the ${blackduck_signature_scanner_name}, as it does by default, the target of that ${blackduck_signature_scan_act} is the initial container file system constructed by Docker Inspector, packaged in a way to optimize results from ${blackduck_product_name}'s matching algorithms. Rather than directly running the ${blackduck_signature_scanner_name} on the initial container file system, ${solution_name} runs the ${blackduck_signature_scanner_name} on a new image; in other words, the squashed image, constructed using the initial container file system built by Docker Inspector. Packaging the initial container file system in a Docker image triggers matching algorithms within ${blackduck_product_name} that optimize match results for Linux file systems.

In earlier versions of ${solution_name} / Docker Inspector, ${solution_name} ran the ${blackduck_signature_scanner_name} directly on the target image. This approach had the disadvantage of potentially producing false positives under certain circumstances. For example, suppose your target image consists of multiple layers. If version 1 of a package is installed in layer 0, and then replaced with a newer version of that package in layer 1, both versions exist in the image, even though the initial container file system only includes version 2. A ${blackduck_signature_scan_act} of the target image shows both versions, even though version 1 has been effectively replaced with version 2. The current ${solution_name} / Docker Inspector functionality avoids this potential for false positives.

### Isolating application components

If you are interested in components from the application layers of your image, but not interested in components from the underlying platform layers, you can exclude components from platform layers from the results.

For example, if you build your application on ubuntu:latest (your Dockerfile starts with FROM ubuntu:latest), you can exclude components from the Ubuntu layer(s) so that the components generated by ${solution_name} using Docker Inspector and the ${blackduck_signature_scanner_name} contain only components from your application layers.

First, find the layer ID of the platform's top layer. To do this task:

Run the docker inspect command on the base image; in our example this is ubuntu:latest.
Find the last element in the RootFS.Layers array. This is the platform top layer ID. In the following example, this is sha256:b079b3fa8d1b4b30a71a6e81763ed3da1327abaf0680ed3ed9f00ad1d5de5e7c.
Set the value of the Docker Inspector property docker.platform.top.layer.id to the platform top layer ID. For example:

./detect.sh ... --detect.docker.image={your application image} --detect.docker.platform.top.layer.id=sha256:b079b3fa8d1b4b30a71a6e81763ed3da1327abaf0680ed3ed9f00ad1d5de5e7c

In this mode, there may be some loss in match accuracy from the ${blackduck_signature_scanner_name} because, in this scenario, the ${blackduck_signature_scanner_name} may be deprived of some contextual information, such as the operating system files that enable it to determine the Linux distribution, and that that may negatively affect its ability to accurately identify components.

### Inspecting Windows images

The Docker Inspector only runs on Linux and Mac. Running on these systems,
Docker can neither pull nor build a Windows image.
Consequently, you cannot run ${solution_name} on a Windows Docker image
using the either the *detect.docker.image* or *detect.docker.image.id* property.
Instead, you must, using a Windows system,
pull and save the target image as a .tar file, and pass that .tar file
to ${solution_name} using the *detect.docker.tar* property.

Given a Windows Image, Docker Inspector, since it can only discover packages using
a Linux package manager will not contribute any components to the BOM, but will
return the container filesystem (in the form of a squashed image),
which ${solution_name} will scan.