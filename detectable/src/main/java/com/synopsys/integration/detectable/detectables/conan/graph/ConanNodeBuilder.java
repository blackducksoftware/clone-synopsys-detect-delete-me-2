/**
 * detectable
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
package com.synopsys.integration.detectable.detectables.conan.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConanNodeBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String ref;
    private String path;
    private String name;
    private String version;
    private String user;
    private String channel;
    private String recipeRevision;
    private String packageId;
    private String packageRevision;
    private final List<String> requiresRefs = new ArrayList<>();
    private final List<String> buildRequiresRefs = new ArrayList<>();
    private final List<String> requiredByRefs = new ArrayList<>();
    private boolean valid = true;

    public ConanNodeBuilder setRefFromLockfile(String ref) {
        if (StringUtils.isBlank(ref)) {
            return this;
        }
        // TODO some of this is duplicated in build; I think it should be done here only
        // Move all that kind of stuff to setters (from build())?
        // build should just build, not set fields
        ref = ref.trim();
        StringTokenizer tokenizer = new StringTokenizer(ref, "@/#");
        if (!ref.startsWith("conanfile.")) {
            if (tokenizer.hasMoreTokens()) {
                name = tokenizer.nextToken();
            }
            if (tokenizer.hasMoreTokens()) {
                version = tokenizer.nextToken();
            }
            if (ref.contains("@")) {
                user = tokenizer.nextToken();
                channel = tokenizer.nextToken();
            }
            if (ref.contains("#")) {
                recipeRevision = tokenizer.nextToken();
            }
        }
        this.ref = ref;
        return this;
    }

    public ConanNodeBuilder setRefFromConanInfo(String ref) {
        if (StringUtils.isBlank(ref)) {
            return this;
        }
        // if rootNode: conanfile.{txt,py}[ (projectname/version)]
        // else       : package/version[@user/channel]
        if (ref.startsWith("conanfile.")) {
            StringTokenizer tokenizer = new StringTokenizer(ref, " \t()/");
            path = tokenizer.nextToken();
            if (tokenizer.hasMoreTokens()) {
                name = tokenizer.nextToken();
                if (tokenizer.hasMoreTokens()) {
                    version = tokenizer.nextToken();
                }
            }
            logger.info(String.format("path: %s; name: %s; version: %s", path, name, version));
        } else {
            StringTokenizer tokenizer = new StringTokenizer(ref, "/@");
            name = tokenizer.nextToken();
            if (name.contains(" ")) {
                valid = false;
                return this;
            }
            if (tokenizer.hasMoreTokens()) {
                version = tokenizer.nextToken();
                if (version.contains(" ")) {
                    valid = false;
                    return this;
                } else if (tokenizer.hasMoreTokens()) {
                    user = tokenizer.nextToken();
                    if (tokenizer.hasMoreTokens()) {
                        channel = tokenizer.nextToken();
                    }
                }
            }
        }
        this.ref = ref;
        return this;
    }

    public ConanNodeBuilder setPath(String path) {
        if (path != null) {
            this.path = path.trim();
        }
        return this;
    }

    public ConanNodeBuilder setRecipeRevision(String recipeRevision) {
        this.recipeRevision = recipeRevision;
        return this;
    }

    public ConanNodeBuilder setPackageId(String packageId) {
        this.packageId = packageId;
        return this;
    }

    public ConanNodeBuilder setPackageRevision(String packageRevision) {
        this.packageRevision = packageRevision;
        return this;
    }

    public ConanNodeBuilder addRequiresRef(String requiresRef) {
        this.requiresRefs.add(requiresRef);
        return this;
    }

    public ConanNodeBuilder addBuildRequiresRef(String buildRequiresRef) {
        this.buildRequiresRefs.add(buildRequiresRef);
        return this;
    }

    public ConanNodeBuilder addRequiredByRef(String requiredByRef) {
        this.requiredByRefs.add(requiredByRef);
        return this;
    }

    public Optional<ConanNode> build() {
        if (StringUtils.isBlank(ref) && StringUtils.isBlank(path)) {
            valid = false;
        }
        if (!valid) {
            logger.debug("This wasn't a node");
            return Optional.empty();
        }
        // if CLI rootNode: conanfile.{txt,py}[ (projectname/version)]
        // if lck rootNode: ref=null; path=conanfile.{txt,py}
        // else           : package/version[@user/channel]
        //        if (StringUtils.isBlank(path) && StringUtils.isNotBlank(ref) && ref.startsWith("conanfile.")) {
        //            StringTokenizer tokenizer = new StringTokenizer(ref, " \t()/");
        //            path = tokenizer.nextToken();
        //            if (tokenizer.hasMoreTokens()) {
        //                name = tokenizer.nextToken();
        //                if (tokenizer.hasMoreTokens()) {
        //                    version = tokenizer.nextToken();
        //                }
        //            }
        //            logger.info(String.format("path: %s; name: %s; version: %s", path, name, version));
        //        }
        if (StringUtils.isBlank(ref) && StringUtils.isNotBlank(path)) {
            ref = path;
        } //else {
        //            StringTokenizer tokenizer = new StringTokenizer(ref, "/@");
        //            name = tokenizer.nextToken();
        //            if (tokenizer.hasMoreTokens()) {
        //                version = tokenizer.nextToken();
        //                if (tokenizer.hasMoreTokens()) {
        //                    user = tokenizer.nextToken();
        //                    if (tokenizer.hasMoreTokens()) {
        //                        channel = tokenizer.nextToken();
        //                    }
        //                }
        //            }
        //        }
        boolean isRootNode = false;
        // TODO revisit this
        if ((path != null) && CollectionUtils.isEmpty(requiredByRefs)) {
            isRootNode = true;
        } else if (CollectionUtils.isEmpty(requiredByRefs)) {
            logger.warn(String.format("Node %s doesn't look like a root node, but its requiredBy list is empty; treating it as a non-root node", ref));
            // TODO this may need to change after requiredBy parsing implemented
            isRootNode = false;
        } else {
            isRootNode = false;
        }
        ConanNode node = new ConanNode(ref, path, name, version, user, channel,
            recipeRevision, packageId, packageRevision, requiresRefs, buildRequiresRefs, requiredByRefs, isRootNode);
        logger.info(String.format("node: %s", node));
        return Optional.of(node);
    }
}
