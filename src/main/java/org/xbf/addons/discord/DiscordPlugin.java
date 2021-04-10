package org.xbf.addons.discord;

import org.xbf.addons.core_http.APIServer;
import org.xbf.addons.core_http.HttpServerPlugin;
import org.xbf.addons.discord.http_addon.Login;
import org.xbf.core.XBF;
import org.xbf.core.Exceptions.AnnotationNotPresent;
import org.xbf.core.Exceptions.HandlerLoadingFailed;
import org.xbf.core.Plugins.DependsOn;
import org.xbf.core.Plugins.PluginLoader;
import org.xbf.core.Plugins.PluginVersion;
import org.xbf.core.Plugins.XPlugin;
import org.xbf.core.Plugins.XervinJavaPlugin;

@XPlugin(name="discord-jda-provider", displayname="Discord JDA Bot Provider", description="Provides a Discord Bot interface for XBF")
@PluginVersion(currentVersion="1.2.0")
@DependsOn(pluginName="xbf", minimumVersion="0.0.13")
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
	
	@Override
	public void onEnable() {
		Object obj = PluginLoader.getPlugins().get("core_http");
		if(obj != null) {
			HttpServerPlugin pl = (HttpServerPlugin) obj;
			APIServer.handlers.add(new Login());
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
