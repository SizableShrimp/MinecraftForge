package net.minecraftforge.fml.loading.moddiscovery;

import cpw.mods.jarhandling.JarMetadata;
import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;

import java.lang.module.ModuleDescriptor;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ModJarMetadata implements JarMetadata {
    private IModFile modFile;
    private ModuleDescriptor descriptor;

    static Optional<IModFile> buildFile(IModLocator locator, Predicate<SecureJar> jarTest, Path... files) {
        return buildFile(locator, jarTest, (a,b) -> true, files);
    }

    static Optional<IModFile> buildFile(IModLocator locator, Predicate<SecureJar> jarTest, BiPredicate<String, String> filter, Path... files) {
        return buildFile(j->ModFile.newFMLInstance(locator, j), jarTest, filter, files);
    }

    static IModFile buildFile(IModLocator locator, Path... files) {
        return buildFile(locator, j->true, files).orElseThrow(()->new IllegalArgumentException("Failed to find valid JAR file"));
    }

    static Optional<IModFile> buildFile(Function<SecureJar, IModFile> mfConstructor, Predicate<SecureJar> jarTest, BiPredicate<String, String> filter, Path... files) {
        var mjm = new ModJarMetadata();
        var sj = SecureJar.from(()->ModFile.DEFAULTMANIFEST, j->ModFile.getFmlModType(j.getManifest()) == IModFile.Type.MOD ? mjm : JarMetadata.from(j, files), filter, files);
        if (jarTest.test(sj)) {
            var mf = mfConstructor.apply(sj);
            mjm.setModFile(mf);
            return Optional.of(mf);
        } else {
            return Optional.empty();
        }
    }

    private ModJarMetadata() {
    }

    public void setModFile(IModFile file) {
        this.modFile = file;
    }

    @Override
    public String name() {
        return modFile.getModFileInfo().moduleName();
    }

    @Override
    public String version() {
        return modFile.getModFileInfo().versionString();
    }

    @Override
    public ModuleDescriptor descriptor() {
        if (descriptor != null) return descriptor;
        var bld = ModuleDescriptor.newAutomaticModule(name())
                .version(version())
                .packages(modFile.getSecureJar().getPackages());
        modFile.getSecureJar().getProviders().stream()
                .filter(p -> !p.providers().isEmpty())
                .forEach(p -> bld.provides(p.serviceName(), p.providers()));
        modFile.getModFileInfo().usesServices().forEach(bld::uses);
        descriptor = bld.build();
        return descriptor;
    }

    public IModFile modFile() {
        return modFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ModJarMetadata) obj;
        return Objects.equals(this.modFile, that.modFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modFile);
    }

    @Override
    public String toString() {
        return "ModJarMetadata[" +"modFile=" + modFile + ']';
    }
}
