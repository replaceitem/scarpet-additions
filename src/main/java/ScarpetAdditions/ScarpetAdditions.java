package ScarpetAdditions;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.script.CarpetExpression;
import carpet.script.value.Value;
import carpet.settings.SettingsManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;


public class ScarpetAdditions implements CarpetExtension {
	public static void noop() {
	}

	public static final Logger LOGGER = LogManager.getLogger();

	public static LiteralText customHeader = new LiteralText("");
	public static LiteralText customFooter = new LiteralText("");
	public static boolean updateTabHeader = false;

	public static HashMap<String, SimpleInventory> virtualInventories = new HashMap<>();

	static
	{
		CarpetServer.manageExtension(new ScarpetAdditions());
	}
	@Override
	public void onGameStarted() {

	}

	@Override
	public void onServerLoaded(MinecraftServer server) {

	}

	@Override
	public void onTick(MinecraftServer server) {
	}

	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
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
		ScarpetFunctions.apply(expression.getExpr());
		ScarpetAdditions.LOGGER.info("scarpet-additions loaded");
	}
}
