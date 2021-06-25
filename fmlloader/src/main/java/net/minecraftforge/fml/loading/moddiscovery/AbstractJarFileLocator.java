/*
 * Minecraft Forge
 * Copyright (c) 2016-2021.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.loading.moddiscovery;

import cpw.mods.jarhandling.JarMetadata;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.lang.reflect.Method;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSigner;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipError;

import static net.minecraftforge.fml.loading.LogMarkers.SCAN;

public abstract class AbstractJarFileLocator implements IModLocator {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final Map<Path, IModFile> jarToModFiles = new HashMap<>();

    @Override
    public Path findPath(final IModFile modFile, final String... path) {
        if (path.length < 1) {
            throw new IllegalArgumentException("Missing path");
        }
        return modFile.getSecureJar().getPath(String.join("/", path));
    }

    @Override
    public void scanFile(final IModFile file, final Consumer<Path> pathConsumer) {
        LOGGER.debug(SCAN,"Scan started: {}", file);
        final Function<Path, SecureJar.Status> status = p->file.getSecureJar().verifyPath(p);

        try (Stream<Path> files = Files.find(file.getSecureJar().getRootPath(), Integer.MAX_VALUE, (p, a) -> p.getNameCount() > 0 && p.getFileName().toString().endsWith(".class"))) {
            file.setSecurityStatus(files.peek(pathConsumer).map(status).reduce((s1, s2)-> SecureJar.Status.values()[Math.min(s1.ordinal(), s2.ordinal())]).orElse(SecureJar.Status.INVALID));
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.debug(SCAN,"Scan finished: {}", file);
    }

    @Override
    public Optional<Manifest> findManifest(final Path file)
    {
        return Optional.empty();
    }

    @Override
    public boolean isValid(final IModFile modFile) {
        return true;
    }

    public JarMetadata metadataSupplier(final SecureJar secureJar) {
        return new JarMetadata() {
            private ModuleDescriptor descriptor;
            @Override
            public String name() {
                return jarToModFiles.get(secureJar.getPrimaryPath()).getModInfos().get(0).getModId();
            }

            @Override
            public String version() {
                return jarToModFiles.get(secureJar.getPrimaryPath()).getModInfos().get(0).getVersion().toString();
            }

            @Override
            public ModuleDescriptor descriptor() {
                if (descriptor != null) return descriptor;
                descriptor = ModuleDescriptor.newAutomaticModule(name())
                        .version(version())
                        .packages(secureJar.getPackages())
                        .build();
                return descriptor;
            }
        };
    }

    protected IModFile trackModFile(IModFile file) {
        jarToModFiles.put(file.getSecureJar().getPrimaryPath(), file);
        return file;
    }
}
