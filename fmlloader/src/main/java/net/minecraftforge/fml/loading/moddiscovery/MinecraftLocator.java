package net.minecraftforge.fml.loading.moddiscovery;

import com.electronwill.nightconfig.core.file.FileConfig;
import cpw.mods.jarhandling.JarMetadata;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.api.NamedPath;
import net.minecraftforge.fml.loading.FMLCommonLaunchHandler;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LibraryFinder;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import net.minecraftforge.forgespi.locating.ModFileFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipError;

import static net.minecraftforge.fml.loading.LogMarkers.LOADING;
import static net.minecraftforge.fml.loading.LogMarkers.SCAN;

public class MinecraftLocator implements IModLocator {
    private static final Logger LOGGER = LogManager.getLogger();
    private SecureJar minecraft;

    public MinecraftLocator() {
        super();
    }
    @Override
    public List<IModFile> scanMods() {
        final var launchHandler = FMLLoader.getLaunchHandler();
        var baseMC = launchHandler.getMinecraftPaths();
        var fmlcore = LibraryFinder.findPathForMaven("net.minecraftforge", "fmlcore", "", "", FMLLoader.versionInfo().forgeVersion());
        var javafmllang = LibraryFinder.findPathForMaven("net.minecraftforge", "javafmllanguage", "", "", FMLLoader.versionInfo().forgeVersion());
        var mclang = LibraryFinder.findPathForMaven("net.minecraftforge", "mclanguage", "", "", FMLLoader.versionInfo().forgeVersion());
        var fmlonly = LibraryFinder.findPathForMaven("net.minecraftforge", "fmlonly", "", "", FMLLoader.versionInfo().forgeVersion());

        this.minecraft = SecureJar.from(()->ModFile.DEFAULTMANIFEST, this::mcMetadata, Stream.concat(baseMC.stream(),Stream.of(fmlonly)).toArray(Path[]::new));
        var mc = SecureJar.from(mclang);
        var core = SecureJar.from(fmlcore);
        var javafml = SecureJar.from(javafmllang);
        return List.of(ModFileFactory.FACTORY.build(this.minecraft, this, this::buildMinecraftTOML), ModFile.newFMLInstance(mc, this), ModFile.newFMLInstance(javafml, this), ModFile.newFMLInstance(core, this));
    }

    private IModFileInfo buildMinecraftTOML(final IModFile iModFile) {
        ModFile modFile = (ModFile) iModFile;
        final Path modsjson = modFile.getLocator().findPath(modFile, "META-INF", "minecraftmod.toml");
        if (!Files.exists(modsjson)) {
            LOGGER.warn(LOADING, "Mod file {} is missing mods.toml file", modFile.getFilePath());
            return null;
        }

        final FileConfig fileConfig = FileConfig.builder(modsjson).build();
        fileConfig.load();
        fileConfig.close();
        final NightConfigWrapper configWrapper = new NightConfigWrapper(fileConfig);
        final ModFileInfo modFileInfo = new ModFileInfo(modFile, configWrapper);
        configWrapper.setFile(modFileInfo);
        return modFileInfo;
    }

    @Override
    public String name() {
        return "minecraft";
    }

    @Override
    public Path findPath(final IModFile modFile, final String... path) {
        return this.minecraft.getPath(String.join("/",path));
    }

    @Override
    public void scanFile(final IModFile modFile, final Consumer<Path> pathConsumer) {
        LOGGER.debug(SCAN, "Scan started: {}", modFile);
        try (Stream<Path> files = Files.find(this.minecraft.getRootPath(), Integer.MAX_VALUE, (p, a) -> p.getNameCount() > 0 && p.getFileName().toString().endsWith(".class"))) {
            files.forEach(pathConsumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.debug(SCAN, "Scan finished: {}", modFile);
    }

    @Override
    public Optional<Manifest> findManifest(final Path file) {
        return Optional.empty();
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {
        // no op
    }

    @Override
    public boolean isValid(final IModFile modFile) {
        return true;
    }

    public JarMetadata mcMetadata(final SecureJar secureJar) {
        return new JarMetadata() {
            private ModuleDescriptor descriptor;
            @Override
            public String name() {
                return "minecraft";
            }

            @Override
            public String version() {
                return FMLLoader.versionInfo().mcVersion();
            }

            @Override
            public ModuleDescriptor descriptor() {
                if (descriptor != null) return descriptor;
                var bld = ModuleDescriptor.newAutomaticModule(name())
                        .version(version())
                        .packages(secureJar.getPackages());
                var providers = secureJar.getProviders();
                providers.stream().filter(p->!p.providers().isEmpty()).forEach(p->bld.provides(p.serviceName(), p.providers()));
                descriptor = bld.build();
                return descriptor;
            }
        };
    }
}
