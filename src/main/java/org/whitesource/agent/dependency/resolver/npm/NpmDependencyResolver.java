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
package org.whitesource.agent.dependency.resolver.npm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whitesource.agent.api.model.DependencyInfo;
import org.whitesource.agent.api.model.DependencyType;
import org.whitesource.agent.dependency.resolver.AbstractDependencyResolver;
import org.whitesource.agent.dependency.resolver.BomFile;
import org.whitesource.agent.dependency.resolver.ResolutionResult;
import org.whitesource.agent.dependency.resolver.bower.BowerDependencyResolver;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Dependency Resolver for NPM projects.
 *
 * @author eugen.horovitz
 */
public class NpmDependencyResolver extends AbstractDependencyResolver {

    /* --- Static members --- */

    private static final String PACKAGE_JSON = "package.json";
    private static final String JAVA_SCRIPT_EXTENSION = ".js";
    public static final String JS_PATTERN = "**/*.js";
    private static final String EXAMPLE = "**/example/**/";
    private static final String EXAMPLES = "**/examples/**/";
    private static final String WS_BOWER_FOLDER = "**/.ws_bower/**/";
    private static final String TEST = "**/test/**/";


    private static final Logger logger = LoggerFactory.getLogger(AbstractDependencyResolver.class);
    public static final String EXCLUDE_TOP_FOLDER = "node_modules";

    /* --- Members --- */

    private final NpmLsJsonDependencyCollector bomCollector;
    private final NpmBomParser bomParser;
    private final boolean ignoreJavaScriptFiles;

    /* --- Constructor --- */

    public NpmDependencyResolver(boolean includeDevDependencies, boolean ignoreJavaScriptFiles) {
        super();
        bomCollector = new NpmLsJsonDependencyCollector(includeDevDependencies);
        bomParser = new NpmBomParser();
        this.ignoreJavaScriptFiles = ignoreJavaScriptFiles;
    }

    public NpmDependencyResolver() {
        this(false,false);
    }

    /* --- Overridden methods --- */

    @Override
    protected Collection<String> getLanguageExcludes() {
        // NPM can contain files generated by the WhiteSource Bower plugin
        Set<String> excludes = new HashSet<>();
        excludes.add(BowerDependencyResolver.WS_BOWER_FILE2);
        excludes.add(BowerDependencyResolver.WS_BOWER_FILE1);
        return excludes;
    }

    @Override
    public String getBomPattern() {
        return "**/*" + PACKAGE_JSON;
    }

    @Override
    protected ResolutionResult resolveDependencies(String projectFolder, String topLevelFolder, List<String> bomFiles) {
        // parse package.json files
        Collection<BomFile> parsedBomFiles = new LinkedList<>();

        Map<File, List<File>> mapBomFiles = bomFiles.stream().map(file -> new File(file)).collect(Collectors.groupingBy(File::getParentFile));

        List<File> files = mapBomFiles.entrySet().stream().map(entry -> {
            if (entry.getValue().size() > 1) {
                return entry.getValue().stream().filter(file -> fileShouldBeParsed(file)).findFirst().get();
            } else {
                return entry.getValue().stream().findFirst().get();
            }
        }).collect(Collectors.toList());

        files.forEach(bomFile -> {
            BomFile parsedBomFile = getBomParser().parseBomFile(bomFile.getAbsolutePath());
            if (parsedBomFile != null && parsedBomFile.isValid()) {
                parsedBomFiles.add(parsedBomFile);
            }
        });

        // try to collect dependencies via 'npm ls'
        Collection<DependencyInfo> dependencies = getDependencyCollector().collectDependencies(topLevelFolder);
        boolean lsSuccess = dependencies.size() > 0;
        if (lsSuccess) {
            handleLsSuccess(parsedBomFiles, dependencies);
        } else {
            dependencies.addAll(collectPackageJsonDependencies(parsedBomFiles));
        }

        // create excludes for .js files upon finding NPM dependencies
        List<String> excludes = new LinkedList<>();
        if (!dependencies.isEmpty()) {
            if (ignoreJavaScriptFiles) {
                //return excludes.stream().map(exclude -> finalRes + exclude).collect(Collectors.toList());
                excludes.addAll(normalizeLocalPath(projectFolder, topLevelFolder, Arrays.asList(JS_PATTERN),null));
            } else {
                excludes.addAll(normalizeLocalPath(projectFolder, topLevelFolder, Arrays.asList(JS_PATTERN), EXCLUDE_TOP_FOLDER));
            }
        }
        return new ResolutionResult(dependencies, excludes);
    }

    @Override
    protected Collection<String> getExcludes() {
        Set<String> excludes = new HashSet<>();
        String bomPattern = getBomPattern();
        excludes.add(EXAMPLE + bomPattern);
        excludes.add(EXAMPLES + bomPattern);
        excludes.add(WS_BOWER_FOLDER + bomPattern);
        excludes.add(TEST + bomPattern);

        excludes.addAll(getLanguageExcludes());
        return excludes;
    }

    @Override
    protected Collection<String> getSourceFileExtensions() {
        return Arrays.asList(JAVA_SCRIPT_EXTENSION);
    }

    /* --- Protected methods --- */
    /**
     * These methods are relevant only for npm and bower
     */

    protected String getPreferredFileName() {
        return PACKAGE_JSON;
    }

    protected NpmBomParser getBomParser() {
        return bomParser;
    }

    protected DependencyType getDependencyType() {
        return DependencyType.NPM;
    }

    protected NpmLsJsonDependencyCollector getDependencyCollector() {
        return bomCollector;
    }

    protected boolean isMatchChildDependency(DependencyInfo childDependency, String name, String version) {
        return childDependency.getArtifactId().equals(NpmBomParser.getNpmArtifactId(name, version));
    }

    protected void enrichDependency(DependencyInfo dependency, BomFile packageJson) {
        dependency.setSha1(packageJson.getSha1());
        dependency.setGroupId(packageJson.getName());
        dependency.setArtifactId(packageJson.getFileName());
        dependency.setVersion(packageJson.getVersion());
        dependency.setSystemPath(packageJson.getLocalFileName());
        dependency.setFilename(packageJson.getLocalFileName());
        dependency.setDependencyType(getDependencyType());
    }


    /* --- Private methods --- */

    /**
     * Collect dependencies from package.json files - without 'npm ls'
     */
    private Collection<DependencyInfo> collectPackageJsonDependencies(Collection<BomFile> packageJsons) {
        Collection<DependencyInfo> dependencies = new LinkedList<>();
        Map<DependencyInfo, BomFile> dependencyPackageJsonMap = new HashMap<>();
        for (BomFile packageJson : packageJsons) {
            if (packageJson != null && packageJson.isValid()) {
                // do not add new dependencies if 'npm ls' already returned all
                DependencyInfo dependency = new DependencyInfo();
                dependencies.add(dependency);
                enrichDependency(dependency, packageJson);
                dependencyPackageJsonMap.put(dependency, packageJson);
            }
        }
        // set hierarchy in case the 'npm ls' did not run or it did not return results
        setHierarchy(dependencyPackageJsonMap);
        return dependencies;
    }

    private boolean fileShouldBeParsed(File file) {
        return (file.getAbsolutePath().endsWith(getPreferredFileName()));
    }

    private void setHierarchy(Map<DependencyInfo, BomFile> dependencyPackageJsonMap) {
        dependencyPackageJsonMap.forEach((dependency, packageJson) -> {
            packageJson.getDependencies().forEach((name, version) -> {
                Optional<DependencyInfo> childDep = dependencyPackageJsonMap.keySet().stream()
                        .filter(childDependency -> isMatchChildDependency(childDependency, name, version))
                        .findFirst();

                if (childDep.isPresent()) {
                    dependency.getChildren().add(childDep.get());
                }
            });
        });
    }

    private void handleLsSuccess(Collection<BomFile> packageJsonFiles, Collection<DependencyInfo> dependencies) {
        Map<String, BomFile> resultFiles = packageJsonFiles.stream()
                .filter(packageJson -> packageJson != null && packageJson.isValid())
                .filter(distinctByKey(file -> file.getFileName()))
                .collect(Collectors.toMap(BomFile::getUniqueDependencyName, Function.identity()));

        dependencies.forEach(dependency -> handleLSDependencyRecursivelyImpl(dependency, resultFiles));
    }

    private void handleLSDependencyRecursivelyImpl(DependencyInfo dependency, Map<String, BomFile> resultFiles) {
        String uniqueName = BomFile.getUniqueDependencyName(dependency.getGroupId(), dependency.getVersion());
        BomFile packageJson = resultFiles.get(uniqueName);
        if (packageJson != null) {
            enrichDependency(dependency, packageJson);
        } else {
            logger.debug("Dependency {} could not be enriched.'package.json' could not be found", dependency.getArtifactId());
        }
        dependency.getChildren().forEach(childDependency -> handleLSDependencyRecursivelyImpl(childDependency, resultFiles));
    }

    private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}