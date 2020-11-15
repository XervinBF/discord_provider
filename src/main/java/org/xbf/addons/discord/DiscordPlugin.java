package org.xbf.addons.discord;

import org.xbf.core.XBF;
import org.xbf.core.Exceptions.AnnotationNotPresent;
import org.xbf.core.Exceptions.HandlerLoadingFailed;
import org.xbf.core.Plugins.DependsOn;
import org.xbf.core.Plugins.PluginLoader;
import org.xbf.core.Plugins.PluginVersion;
import org.xbf.core.Plugins.XPlugin;
import org.xbf.core.Plugins.XervinJavaPlugin;

@XPlugin(name="discord-jda-provider", displayname="Discord JDA Bot Provider", description="Provides a Discord Bot interface for XBF")
@PluginVersion(currentVersion="1.1.0")
@DependsOn(pluginName="xbf", minimumVersion="0.0.8")
public class DiscordPlugin extends XervinJavaPlugin {

	@Override
	public void register() {
		try {
			handler = (DiscordHandler) XBF.registerHandler(DiscordHandler.class);
		} catch (AnnotationNotPresent e) {
			e.printStackTrace();
		} catch (HandlerLoadingFailed e) {
			e.printStackTrace();
		}
		cfg = getConfig(DiscordPluginConfig.class);
		if(cfg.discordBotToken == null) {
			logger.error("No discord bot token provided!");
		}
	}
	
	DiscordHandler handler;
	
	public DiscordHandler getHandler() {
		return handler;
	}
	
	public static DiscordPlugin getInstance() {
		return (DiscordPlugin) PluginLoader.getPlugins().get("discord-jda-provider");
	}
	
	public static DiscordPluginConfig cfg;
	
	public static void main(String[] args) {
		System.out.println("This is a discord provider for XBF");
	}
	
}
