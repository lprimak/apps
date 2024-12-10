/*
 * Copyright (C) 2011-2024 Flow Logix, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flowlogix.starter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.omnifaces.util.Faces;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;
import static com.flowlogix.util.Streams.readString;
import static java.util.function.Predicate.not;

@Slf4j
@ApplicationScoped
public class ArchetypeGenerator {
    @SuppressWarnings("checkstyle:MagicNumber")
    private static final int BUFFER_SIZE = 4096;
    private final Semaphore semaphore;
    private final String jvmOptions;

    public record Parameter(@NonNull String key, String value) { }
    public record ReturnValue(Path temporaryPath, int status, String output) implements AutoCloseable {
        @Override
        @SneakyThrows(IOException.class)
        public void close() {
            cleanup(this);
        }
    }

    public ArchetypeGenerator() {
        this(1, "-Xmx256m");
    }

    @Inject
    public ArchetypeGenerator(@ConfigProperty(name = "com.flowlogix.starter.generator-threads", defaultValue = "4")
                              int generatorThreads,
                              @ConfigProperty(name = "com.flowlogix.starter.jvm-options", defaultValue = "-Xmx256m")
                              String jvmOptions) {
        log.debug("Generator threads: {}", generatorThreads);
        semaphore = new Semaphore(generatorThreads);
        this.jvmOptions = jvmOptions;
    }

    @SneakyThrows({IOException.class, InterruptedException.class})
    public ReturnValue generateArchetype(Parameter[] inputParameters) {
        log.debug("Available Permits: {}", semaphore.availablePermits());
        semaphore.acquire();
        try {
            Path temporaryPath = getTemporaryPath();
            Files.writeString(temporaryPath.resolve(".mvn").resolve("jvm.config"),
                    jvmOptions + System.lineSeparator());
            String projectDirectory = temporaryPath.toString();
            List<String> options = generateMavenCommandLine(inputParameters, projectDirectory);
            log.debug("Options: {}", options);
            try {
                Process mavenProcess = new ProcessBuilder().command(options).directory(temporaryPath.toFile()).start();
                return new ReturnValue(temporaryPath, mavenProcess.waitFor(), readString(mavenProcess.getInputStream()));
            } catch (IOException e) {
                log.debug("Failed to execute Maven process", e);
                return new ReturnValue(temporaryPath, -1, e.getMessage());
            }
        } finally {
            semaphore.release();
        }
    }

    public static List<String> generateMavenCommandLine(Parameter[] inputParameters, String projectDirectory) {
        return Stream.concat(Stream.of("mvn", "archetype:generate"),
                extractParameters(inputParameters, projectDirectory).entrySet().stream()
                        .map(entry -> (projectDirectory != null ? "-D%s=%s" : "-D%s=\"%s\"")
                        .formatted(entry.getKey(), entry.getValue()))).toList();
    }

    public Future<ReturnValue> zipToStream(ReturnValue returnValue, OutputStream zipFileStream,
                                           ExecutorService executorService) {
        return executorService.submit(() -> zipToStream(returnValue, zipFileStream));
    }

    @SneakyThrows(IOException.class)
    public void createZipStream(ReturnValue result, OutputStream outputStream, ExecutorService executorService) {
        try (var output = new PipedOutputStream();
             var input = new PipedInputStream(output, BUFFER_SIZE)) {
            zipToStream(result, output, executorService);
            writer(result, input, outputStream, false);
        }
    }

    @SneakyThrows(IOException.class)
    public InputStream createZipStream(ReturnValue result, ExecutorService executorService) {
        var output = new PipedOutputStream();
        zipToStream(result, output, executorService);
        return new PipedInputStream(output, BUFFER_SIZE);
    }

    public void writer(ReturnValue result, InputStream inputStream, OutputStream outputStream, boolean closeStreams) {
        try {
            while (true) {
                byte[] readBytes = inputStream.readNBytes(BUFFER_SIZE);
                if (readBytes.length == 0) {
                    break;
                }
                log.debug("Writing {} bytes to output stream", readBytes.length);
                outputStream.write(readBytes);
                outputStream.flush();
            }
        } catch (IOException e) {
            log.debug("Failed to stream zip file.", e);
            if (Faces.hasContext()) {
                Faces.responseComplete();
            }
        } finally {
            log.debug("Cleanup");
            result.close();
            if (closeStreams) {
                try {
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    log.debug("Failed to close zip streams", e);
                }
            }
        }
    }

    @SneakyThrows(IOException.class)
    private static ReturnValue zipToStream(ReturnValue returnValue, OutputStream zipFileStream) {
        if (returnValue.status == 0) {
            createZipFile(returnValue.temporaryPath, zipFileStream);
        }
        return returnValue;
    }

    private static void cleanup(ReturnValue returnValue) throws IOException {
        try (var paths = Files.walk(returnValue.temporaryPath)) {
            paths.sorted(Comparator.reverseOrder()).forEach(ArchetypeGenerator::deleteFile);
        }
    }

    private static Map<String, String> extractParameters(Parameter[] inputParameters, String projectDirectory) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("archetypeGroupId", "com.flowlogix.archetypes");
        parameters.put("archetypeArtifactId", "starter");
        parameters.put("archetypeVersion", "LATEST");
        parameters.put("interactiveMode", "false");
        if (projectDirectory != null) {
            parameters.put("maven.multiModuleProjectDirectory", projectDirectory);
            parameters.put("outputDirectory", projectDirectory);
        }
        parameters.put("groupId", "com.example");
        parameters.put("artifactId", "starter");
        parameters.put("projectName", "Starter Project");
        parameters.put("package", "%s.%s".formatted(parameters.get("groupId"), parameters.get("artifactId")));
        parameters.put("version", "1.x-SNAPSHOT");
        parameters.put("baseType", "payara");

        if (inputParameters != null) {
            for (Parameter parameter : inputParameters) {
                if (parameter.value() != null && !parameter.value().isBlank()) {
                    parameters.put(parameter.key(), parameter.value().trim());
                }
            }
        }
        return parameters;
    }

    private Path getTemporaryPath() throws IOException {
        Path path =  Files.createTempDirectory("starter-generator-project-");
        if (!path.resolve(".mvn").toFile().mkdirs()) {
            throw new IOException("Unable to create directory");
        }
        Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rwx------"));
        log.debug("Created temporary project directory: {}", path);
        return path;
    }

    @SneakyThrows(IOException.class)
    private static void deleteFile(Path path) {
        Files.delete(path);
    }

    private static void createZipFile(Path sourceDirPath, OutputStream outputStream) throws IOException {
        try (var zipOutputStream = new ZipArchiveOutputStream(outputStream);
             var sourceDirPaths = Files.walk(sourceDirPath)) {
            sourceDirPaths.filter(not(Files::isDirectory))
                    .forEach(path -> addZipEntry(sourceDirPath, path, zipOutputStream));
        }
    }

    @SneakyThrows(IOException.class)
    @SuppressWarnings({"checkstyle:IllegalTokenText", "checkstyle:MagicNumber"})
    private static void addZipEntry(Path sourceDirPath, Path path, ZipArchiveOutputStream zipOutputStream) {
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(sourceDirPath.relativize(path).toString());
        if (Files.isExecutable(path)) {
            zipEntry.setUnixMode(0755);
        }
        zipOutputStream.putArchiveEntry(zipEntry);
        Files.copy(path, zipOutputStream);
        zipOutputStream.closeArchiveEntry();
    }
}
