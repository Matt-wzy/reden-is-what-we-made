package com.github.zly2006.reden;

import com.github.zly2006.reden.malilib.KeyCallbacksKt;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.pearl.PearlTask;
import com.github.zly2006.reden.report.ClientReportKt;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.util.FileUtils;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.github.zly2006.reden.Reden.MOD_NAME;

public class RedenClient implements ClientModInitializer {
    public static final String CONFIG_FILE = "reden.json";
    public static final Logger LOGGER = LoggerFactory.getLogger("reden");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    @Override
    public void onInitializeClient() {
        ClientReportKt.initReport();
        PearlTask.Companion.register();
        InitializationHandler.getInstance().registerInitializationHandler(() -> {
            ConfigManager.getInstance().registerConfigHandler("reden", new IConfigHandler() {
                @Override
                public void load() {
                    try {
                        File file = new File(FileUtils.getConfigDirectory(), CONFIG_FILE);
                        if (!file.exists()) {
                            return;
                        }
                        JsonObject jo = GSON.fromJson(Files.readString(file.toPath()), JsonObject.class);
                        ConfigUtils.readConfigBase(jo, MOD_NAME, MalilibSettingsKt.getAllOptions());
                    } catch (IOException e) {
                        save();
                    }
                }

                @Override
                public void save() {
                    JsonObject jo = new JsonObject();
                    ConfigUtils.writeConfigBase(jo, MOD_NAME, MalilibSettingsKt.getAllOptions());
                    try {
                        Files.writeString(new File(FileUtils.getConfigDirectory(), CONFIG_FILE).toPath(), GSON.toJson(jo));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            InputEventHandler.getKeybindManager().registerKeybindProvider(new IKeybindProvider() {
                @Override
                public void addKeysToMap(IKeybindManager iKeybindManager) {
                    MalilibSettingsKt.HOTKEYS.stream()
                            .map(IHotkey::getKeybind)
                            .forEach(iKeybindManager::addKeybindToMap);
                }

                @Override
                public void addHotkeys(IKeybindManager iKeybindManager) {
                    iKeybindManager.addHotkeysForCategory("Reden", "reden.hotkeys.category.generic_hotkeys", MalilibSettingsKt.HOTKEYS);
                }
            });
            KeyCallbacksKt.configureKeyCallbacks(MinecraftClient.getInstance());
        });
    }
}
