package org.xbf.addons.discord;

import java.util.List;

import org.xbf.core.Messages.Request;
import org.xbf.core.Models.MessageCommand;
import org.xbf.core.Models.XUser;
import org.xbf.core.Utils.Map.FastMap;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionListener extends ListenerAdapter {

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if (DiscordPlugin.getInstance().getHandler().client.getSelfUser().getIdLong() == event.getUserIdLong()) {
			return;
		}
		long msgid = Long.parseLong(event.getMessageId());
		FastMap<String, String> query = new FastMap<String, String>().add("source", "Discord").add("messageId", msgid + "");
		if (MessageCommand.getSmartTable().hasWithQuery(query)) {
			String command = null;
			List<MessageCommand> cmds = MessageCommand.getSmartTable().getMultiple(query);
			for (MessageCommand p : cmds) {
				if (p.reactionEmote.equals(DiscordPlugin.getInstance().getHandler().stringToBytes(event.getReactionEmote().getEmoji()))) {
					command = p.command;
				}
			}
			if (command != null) {
				Request req = new Request();
				req.channel = event.getChannel().getId();
				req.server = event.isFromGuild() ? event.getGuild().getId() : "";
				req.message = command;
				req.source = "Discord";
				req.user = XUser.getFromProvider(event.getUser().getId(), "Discord", event.getUser().getName());
				req.origid = event.getUserId();
				req.providerName = event.isFromGuild() ? event.getMember().getEffectiveName() : event.getUser().getName();
				DiscordPlugin.getInstance().getHandler().RunCommand(req, event.getChannel(), event.getMessageIdLong(), event.getUser());
			}
		}
	}
	
}
