package net.replaceitem.scarpet.additions;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.script.annotation.AnnotationParser;
import carpet.script.exception.Throwables;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScarpetAdditions implements CarpetExtension, ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("scarpet-additions");
	public static Component MOTD = null;

	public static final Throwables HTTP_REQUEST_ERROR = Throwables.register("http_request_error", Throwables.IO_EXCEPTION);

    @Override
	public void onInitialize() {
		CarpetServer.manageExtension(new ScarpetAdditions());
		LOGGER.info("Scarpet-additions loaded");
	}

	@Override
	public void onGameStarted() {
		AnnotationParser.parseFunctionClass(ScarpetFunctions.class);
	}
}
