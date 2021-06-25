package net.minecraftforge.fml.network;

import com.electronwill.nightconfig.toml.TomlFormat;
import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfigSync {
    public List<Pair<String, FMLHandshakeMessages.S2CConfigData>> syncConfigs(boolean isLocal) {
        final Map<String, byte[]> configData = configSets.get(ModConfig.Type.SERVER).stream().collect(Collectors.toMap(ModConfig::getFileName, mc -> { //TODO: Test cpw's LambdaExceptionUtils on Oracle javac.
            try {
                return Files.readAllBytes(mc.getFullPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        return configData.entrySet().stream().map(e->Pair.of("Config "+e.getKey(), new FMLHandshakeMessages.S2CConfigData(e.getKey(), e.getValue()))).collect(Collectors.toList());
    }

    public void receiveSyncedConfig(final FMLHandshakeMessages.S2CConfigData s2CConfigData, final Supplier<NetworkEvent.Context> contextSupplier) {
        if (!Minecraft.getInstance().isLocalServer()) {
            Optional.ofNullable(fileMap.get(s2CConfigData.getFileName())).ifPresent(mc-> {
                mc.setConfigData(TomlFormat.instance().createParser().parse(new ByteArrayInputStream(s2CConfigData.getBytes())));
                mc.fireEvent(new ModConfig.Reloading(mc));
            });
        }
    }

    /**
     * @return the modinfo used to create this mod instance
     */    public List<Pair<String, FMLHandshakeMessages.S2CConfigData>> syncConfigs(boolean isLocal) {
        final Map<String, byte[]> configData = configSets.get(ModConfig.Type.SERVER).stream().collect(Collectors.toMap(ModConfig::getFileName, mc -> { //TODO: Test cpw's LambdaExceptionUtils on Oracle javac.
            try {
                return Files.readAllBytes(mc.getFullPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        return configData.entrySet().stream().map(e->Pair.of("Config "+e.getKey(), new FMLHandshakeMessages.S2CConfigData(e.getKey(), e.getValue()))).collect(Collectors.toList());
    }

    public void receiveSyncedConfig(final FMLHandshakeMessages.S2CConfigData s2CConfigData, final Supplier<NetworkEvent.Context> contextSupplier) {
        if (!Minecraft.getInstance().isLocalServer()) {
            Optional.ofNullable(fileMap.get(s2CConfigData.getFileName())).ifPresent(mc-> {
                mc.setConfigData(TomlFormat.instance().createParser().parse(new ByteArrayInputStream(s2CConfigData.getBytes())));
                mc.fireEvent(new ModConfig.Reloading(mc));
            });
        }
    }

}
