/**
 * Copyright (C) 2014 WhiteSource Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.whitesource.fs.configuration;


import org.whitesource.fs.FSAConfiguration;

import java.util.Properties;

import static org.whitesource.agent.ConfigPropertyKeys.*;

public class ResolverConfiguration {

    /* --- Constructors --- */

    public ResolverConfiguration(){
        this(new Properties());
    }

    public ResolverConfiguration(Properties config) {
        dependencyResolverNpmRunPreStep = FSAConfiguration.getBooleanProperty(config, NPM_RUN_PRE_STEP, false);
        dependencyResolverNpmResolveDependencies = FSAConfiguration.getBooleanProperty(config, NPM_RESOLVE_DEPENDENCIES, true);
        dependencyResolverNpmIncludeDevDependencies = FSAConfiguration.getBooleanProperty(config, NPM_INCLUDE_DEV_DEPENDENCIES, false);
        dependencyResolverNpmIgnoreJavaScriptFiles = FSAConfiguration.getBooleanProperty(config, NPM_IGNORE_JAVA_SCRIPT_FILES, true);
        dependencyResolverNpmTimeoutDependenciesCollector = FSAConfiguration.getLongProperty(config, NPM_TIMEOUT_DEPENDENCIES_COLLECTOR_SECONDS, 60);
        dependencyResolverBowerResolveDependencies = FSAConfiguration.getBooleanProperty(config, BOWER_RESOLVE_DEPENDENCIES, true);
        dependencyResolverBowerRunPreStep = FSAConfiguration.getBooleanProperty(config, BOWER_RUN_PRE_STEP, false);
        dependencyResolverNugetResolveDependencies = FSAConfiguration.getBooleanProperty(config, NUGET_RESOLVE_DEPENDENCIES, true);
        dependencyResolverMavenResolveDependencies = FSAConfiguration.getBooleanProperty(config, MAVEN_RESOLVE_DEPENDENCIES, true);
        dependencyResolverMavenIgnoredScopes = FSAConfiguration.getListProperty(config, MAVEN_IGNORED_SCOPES, null);
        dependencyResolverMavenAggregateModules = FSAConfiguration.getBooleanProperty(config, MAVEN_AGGREGATE_MODULES, true);
        dependencyResolverDependenciesOnly = FSAConfiguration.getBooleanProperty(config, DEPENDENCIES_ONLY, false);
        dependencyResolverWhitesourceConfiguration = config.getProperty(PROJECT_CONFIGURATION_PATH);
    }

    /* --- Members --- */

    private final boolean dependencyResolverNpmRunPreStep;
    private final boolean dependencyResolverNpmResolveDependencies;
    private final boolean dependencyResolverNpmIncludeDevDependencies;
    private final boolean dependencyResolverNpmIgnoreJavaScriptFiles;
    private final long dependencyResolverNpmTimeoutDependenciesCollector;
    private final boolean dependencyResolverBowerResolveDependencies;
    private final boolean dependencyResolverBowerRunPreStep;
    private final boolean dependencyResolverNugetResolveDependencies;
    private final boolean dependencyResolverMavenResolveDependencies;
    private final String[] dependencyResolverMavenIgnoredScopes;
    private final boolean dependencyResolverMavenAggregateModules;
    private final boolean dependencyResolverDependenciesOnly;
    private final String dependencyResolverWhitesourceConfiguration;

    /* --- Public getters --- */

    public boolean isDependencyResolverNpmRunPreStep() {
        return dependencyResolverNpmRunPreStep;
    }

    public boolean isDependencyResolverNpmResolveDependencies() {
        return dependencyResolverNpmResolveDependencies;
    }

    public boolean isDependencyResolverNpmIncludeDevDependencies() {
        return dependencyResolverNpmIncludeDevDependencies;
    }

    public boolean isDependencyResolverNpmIgnoreJavaScriptFiles() {
        return dependencyResolverNpmIgnoreJavaScriptFiles;
    }

    public long getDependencyResolverNpmTimeoutDependenciesCollector() {
        return dependencyResolverNpmTimeoutDependenciesCollector;
    }

    public boolean isDependencyResolverBowerResolveDependencies() {
        return dependencyResolverBowerResolveDependencies;
    }

    public boolean isDependencyResolverBowerRunPreStep() {
        return dependencyResolverBowerRunPreStep;
    }

    public boolean isDependencyResolverNugetResolveDependencies() {
        return dependencyResolverNugetResolveDependencies;
    }

    public boolean isDependencyResolverMavenResolveDependencies() {
        return dependencyResolverMavenResolveDependencies;
    }

    public String[] getDependencyResolverMavenIgnoredScopes() {
        return dependencyResolverMavenIgnoredScopes;
    }

    public boolean isDependencyResolverMavenAggregateModules() {
        return dependencyResolverMavenAggregateModules;
    }

    public boolean isDependencyResolverDependenciesOnly() {
        return dependencyResolverDependenciesOnly;
    }

    public String getDependencyResolverWhitesourceConfiguration() {
        return dependencyResolverWhitesourceConfiguration;
    }
}