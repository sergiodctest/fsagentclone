/**
 * Copyright (C) 2017 WhiteSource Ltd.
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
package org.whitesource.agent.dependency.resolver.maven;
import org.whitesource.agent.api.model.DependencyInfo;
import org.whitesource.agent.api.model.DependencyType;
import org.whitesource.agent.dependency.resolver.AbstractDependencyResolver;
import org.whitesource.agent.dependency.resolver.ResolutionResult;
import java.util.*;


/**
     * Dependency Resolver for Maven projects.
     *
     * @author eugen.horovitz
     */
    public class MavenDependencyResolver extends AbstractDependencyResolver {

    /* --- Static Members --- */

    private static final String POM_XML = "pom.xml";
    private static final String JAVA_EXTENSION = ".java";
    private static final String JAVA_EXTENSION_PATTERN = "**/*" + JAVA_EXTENSION;
    private static final String CLASS_EXTENSION_PATTERN = "**/*" + ".class";
    private static final String JAR_EXTENSION_PATTERN = "**/*" + ".jar";

    /* --- Constructor --- */

    public MavenDependencyResolver(boolean mavenIncludeDevDependencies) {
        super();
        this.dependencyCollector = new MavenTreeDependencyCollector(mavenIncludeDevDependencies);
    }

    /* --- Members --- */

    private final MavenTreeDependencyCollector dependencyCollector;

    @Override
    protected ResolutionResult resolveDependencies(String projectFolder, String topLevelFolder, List<String> bomFiles) {
        // try to collect dependencies via 'npm ls'
        Collection<DependencyInfo> dependencies = dependencyCollector.collectDependencies(topLevelFolder);
        // create excludes for .js files upon finding NPM dependencies
        List<String> excludes = new LinkedList<>();
        if (!dependencies.isEmpty()) {
            excludes.addAll(normalizeLocalPath(projectFolder, topLevelFolder, Arrays.asList(JAVA_EXTENSION_PATTERN), null));
            excludes.add(CLASS_EXTENSION_PATTERN);
            excludes.add(JAR_EXTENSION_PATTERN);
        }
        return new ResolutionResult(dependencies, excludes);
    }

    @Override
    protected Collection<String> getExcludes() {
        Set<String> excludes = new HashSet<>();
        excludes.addAll(getLanguageExcludes());
        return excludes;
    }

    @Override
    protected Collection<String> getSourceFileExtensions() {
        return Arrays.asList(JAVA_EXTENSION);
    }

    @Override
    protected DependencyType getDependencyType() {
        return DependencyType.MAVEN;
    }

    @Override
    public String getBomPattern() {
        return "**/*" + POM_XML;
    }

    @Override
    protected Collection<String> getLanguageExcludes() {
        // exclude files generated by the WhiteSource Bower plugin
        Set<String> excludes = new HashSet<>();
        excludes.add(JAVA_EXTENSION_PATTERN);
        //excludes.add(XML_EXTENSION_PATTERN);
        //excludes.add(CLASS_EXTENSION_PATTERN);
        return excludes;
    }
}