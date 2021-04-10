package org.xbf.addons.discord;

import java.util.HashMap;

import org.xbf.core.Plugins.config.EnvironmentVariable;
import org.xbf.core.Plugins.config.PluginConfig;

public class DiscordPluginConfig extends PluginConfig {

	@EnvironmentVariable("XBF_DISCORD_BOTTOKEN")
	public String discordBotToken;
	
	public String clientId;
	public String clientSecret;
	public String redirectUri;
	
	public HashMap<String, String> clients = new HashMap<String, String>();
	
}
