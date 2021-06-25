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

package net.minecraftforge.fml.loading;

import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import net.minecraftforge.api.distmarker.Dist;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.minecraftforge.fml.loading.LogMarkers.CORE;

public abstract class FMLCommonLaunchHandler
{
    private static final Logger LOGGER = LogManager.getLogger();

    public abstract Dist getDist();

    protected void processModClassesEnvironmentVariable(final Map<String, List<Pair<Path, List<Path>>>> arguments) {
        final String modClasses = Optional.ofNullable(System.getenv("MOD_CLASSES")).orElse("");
        LOGGER.debug(CORE, "Got mod coordinates {} from env", modClasses);

        // "a/b/;c/d/;" -> "modid%%c:\fish\pepper;modid%%c:\fish2\pepper2\;modid2%%c:\fishy\bums;modid2%%c:\hmm"
        final Map<String, List<Path>> modClassPaths = Arrays.stream(modClasses.split(File.pathSeparator)).
                map(inp -> inp.split("%%", 2)).map(this::buildModPair).
                collect(Collectors.groupingBy(Pair::getLeft, Collectors.mapping(Pair::getRight, Collectors.toList())));

        LOGGER.debug(CORE, "Found supplied mod coordinates [{}]", modClassPaths);

        final List<Pair<Path, List<Path>>> explodedTargets = arguments.computeIfAbsent("explodedTargets", a -> new ArrayList<>());
        modClassPaths.forEach((modlabel,paths) -> explodedTargets.add(Pair.of(paths.get(0), paths.subList(1, paths.size()))));
    }

    private Pair<String, Path> buildModPair(String[] splitString) {
        String modid = splitString.length == 1 ? "defaultmodid" : splitString[0];
        Path path = Paths.get(splitString[splitString.length - 1]);
        return Pair.of(modid, path);
    }

    protected abstract String getNaming();

    public boolean isProduction() {
        return false;
    }

    public boolean isData() {
        return false;
    };

    public abstract List<Path> getMinecraftPaths();

    public void configureTransformationClassLoader(final ITransformingClassLoaderBuilder builder) {

    }

}
