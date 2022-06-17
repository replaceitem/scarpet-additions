package ScarpetAdditions;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.script.CarpetExpression;
import carpet.script.annotation.AnnotationParser;
import carpet.settings.SettingsManager;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScarpetAdditions implements CarpetExtension, ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("scarpet-additions");

	@Override
	public void onInitialize() {
		CarpetServer.manageExtension(new ScarpetAdditions());
		LOGGER.info("Scarpet-additions loaded");
	}

	@Override
	public void onGameStarted() {
		AnnotationParser.parseFunctionClass(ScarpetFunctions.class);
	}

	@Override
	public void onServerLoaded(MinecraftServer server) {

	}

	@Override
	public void onTick(MinecraftServer server) {
	}

	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandBuildContext) {
	}

	@Override
	public SettingsManager customSettingsManager() {
		return null;
	}

	@Override
	public void onPlayerLoggedIn(ServerPlayerEntity player) {
	}

	@Override
	public void onPlayerLoggedOut(ServerPlayerEntity player) {
	}

	@Override
	public void scarpetApi(CarpetExpression expression) {
	}
}
