package org.xbf.addons.discord;

import org.slf4j.LoggerFactory;
import org.xbf.core.Messages.Request;
import org.xbf.core.Models.XUser;

import ch.qos.logback.classic.Logger;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

	static Logger l = (Logger) LoggerFactory.getLogger(MessageListener.class);

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (DiscordHandler.fail)
			return;
		try {
			if (DiscordHandler.client.getSelfUser().getIdLong() == event.getAuthor().getIdLong()) {
				return;
			}
			if(event.getMessage().getAuthor().isBot()) return;
			Request req = new Request();
			req.channel = event.getChannel().getId();
			req.server = event.isFromGuild() ? event.getGuild().getId() : "";
			req.message = event.getMessage().getContentDisplay();
			req.source = "Discord";
			req.user = XUser.getFromProvider(event.getAuthor().getId(), "Discord", event.getAuthor().getName());
			req.origid = event.getAuthor().getId();
			
			if(event.getMessage() != null && event.getMessage().getAuthor() != null)
				req.providerName = event.isFromGuild() && event.getMember() != null ? event.getMember().getEffectiveName() : event.getMessage().getAuthor().getName();
			req.data = new DiscordSourceData(event);
			
			MessageChannel c = event.getMessage().getChannel();
			
			
			DiscordHandler.RunCommand(req, c,
					event.getMessage().getAuthor().getIdLong(), event.getMessageIdLong(), event.getMessage().getAuthor());
		} catch (Exception e) {
			try {
				l.error("Discord Message Translation Failed", e);
			} catch (Exception ex) {
				System.err.println("Error in errorlogging for PassEvent");
				ex.printStackTrace();
			}
			System.err.println("Exception when passing messageevent to runtime");
			l.error("Exception Caught - PassEvent");
			e.printStackTrace();
		}
	}
	

}
