package org.xbf.addons.discord;

import org.xbf.core.Plugins.config.EnvironmentVariable;
import org.xbf.core.Plugins.config.PluginConfig;

public class DiscordPluginConfig extends PluginConfig {

	@EnvironmentVariable("XBF_DISCORD_BOTTOKEN")
	public String discordBotToken;
	
}
