package org.xbf.addons.discord;

import java.util.ArrayList;
import java.util.List;

import org.xbf.core.Messages.RequestSourceData;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DiscordSourceData extends RequestSourceData {

	public MessageReceivedEvent msgCreateEvent;
	
	
	public List<Attachment> attachments = new ArrayList<>();
	
	
	public DiscordSourceData(MessageReceivedEvent mcev) {
		this.msgCreateEvent = mcev;
		attachments = mcev.getMessage().getAttachments();
	}
	
}
