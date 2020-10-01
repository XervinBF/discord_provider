package org.xbf.addons.discord;

import java.awt.Color;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import org.slf4j.LoggerFactory;
import org.xbf.core.CommandProcessor;
import org.xbf.core.Messages.Pair;
import org.xbf.core.Messages.Request;
import org.xbf.core.Messages.Response;
import org.xbf.core.Messages.ResponseDestination;
import org.xbf.core.Messages.RichResponse;
import org.xbf.core.Models.MessageCommand;
import org.xbf.core.Plugins.Handler;
import org.xbf.core.Plugins.XHandler;
import org.xbf.core.Utils.Timings.Stopwatch;

import ch.qos.logback.classic.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

@XHandler(providerName="discord")
public class DiscordHandler extends Handler {

	public static JDA client;

	private boolean ready;

	private Logger l = (Logger) LoggerFactory.getLogger(getClass());

	public static boolean fail;

	@Override
	public void start() {
		if(DiscordPlugin.cfg.discordBotToken == null) {
			l.warn("No discord bot token provided!");
			return;
		}
		try {
			// Init discord hook
			Stopwatch sw = Stopwatch.startnew("Discord.RegisterDebug");
			l.info("Creating builder with settings");
			sw.start("Discord.CreateClient");
			JDABuilder builder = new JDABuilder(DiscordPlugin.cfg.discordBotToken)
					.addEventListeners(new MessageListener())
					.addEventListeners(new ReactionListener());
			l.info("Building client");
			client = null;
			try {
				client = builder.build();
			} catch (LoginException e1) {
				l.error("Login failed!");
				e1.printStackTrace();
			}
			l.info("Registering events");
			sw.start("Discord.RegisterFramework");
			sw.stop();
	
			super.start();
		} catch (Exception ex) {
			l.error("Failed to start discord hook", ex);
		}
	}
	
	@Override
	public void sendMessage(String userid, Response message) {
		SendResponse(message, client.getUserById(userid).openPrivateChannel().complete());
	}

	@Override
	public void stop() {
		client.shutdown();
		client = null;
		super.stop();
	}

	public final static String[] em = new String[] { "\uD83C\uDDE6", "\uD83C\uDDE7", "\uD83C\uDDE8", "\uD83C\uDDE9",
			"\uD83C\uDDEA", "\uD83C\uDDEB", "\uD83C\uDDEC", "\uD83C\uDDED", "\uD83C\uDDEE", "\uD83C\uDDEF",
			"\uD83C\uDDF0", "\uD83C\uDDF1", "\uD83C\uDDF2", "\uD83C\uDDF3", "\uD83C\uDDF4", "\uD83C\uDDF5",
			"\uD83C\uDDF6", "\uD83C\uDDF7", "\uD83C\uDDF8", "\uD83C\uDDF9", "\uD83C\uDDFA", "\uD83C\uDDFB",
			"\uD83C\uDDFC", "\uD83C\uDDFD", "\uD83C\uDDFE", "\uD83C\uDDFF" };

	public static void RunCommand(Request req, MessageChannel c, long userId, long messageId, User u) {
		if (req.user == null) {

			// Old user confirmation code
//			if(req.message.equals(".X.ACC.NEW")) {
//				SendResponse(new Response(new Dict(Config.getDefaultLang())).addRichResponse(new RichResponse("Account Created!")), c);
//			} else if(req.message.equals(".X.ACC.LINK")) {
//				SendResponse(new Response(new Dict(Config.getDefaultLang())).addRichResponse(new RichResponse("Link Account").setDescription("*!link " + LinkUtillity.createLinkCode("Discord",
//					userId + ""))), c);
//			} else {
//				SendResponse(new Response(new Dict(Config.getDefaultLang())).addRichResponse(new RichResponse("No Account Found!")
//						.setDescription("You cant undo this action!")
//						.addCommand("New User", ".X.ACC.NEW")
//						.addCommand("Link User", ".X.ACC.LINK")), c);
//			}
		}
		RunCommand(req, c, messageId, u);
	}

	public static void RunCommand(final Request req, final MessageChannel c, final long messageId, final User usr) {
		if(req.user == null) return;
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				Response res = CommandProcessor.runRequest(req);
				if (res == null)
					return;

				if (res.removeMessage) {
					c.retrieveMessageById(messageId).complete().delete().complete();
				}

//				l.debug(new Gson().toJson(res));
				if(res.destination == ResponseDestination.USER_PRIVATE) {
					SendResponse(res, usr.openPrivateChannel().complete());
				} else if(res.destination == ResponseDestination.SAME_CHANNEL) {
					SendResponse(res, c);
				}
			}
		}, CommandProcessor.getGenericRequestThreadName(req));
		t.start();
	}

	public static void SendResponse(Response res, MessageChannel c) {
		if (res.text != null)
			c.sendMessage(res.text).complete();
		for (RichResponse r : res.responses) {
			String commandRep = "";
			String[] alf = "abcdefghijklmnop".split("");
			int i = 0;
			final ArrayList<Pair<String, String>> reactions = new ArrayList<Pair<String, String>>();
			for (Pair<String, String> a : r.commands) {
				String emo = em[i];
				reactions.add(new Pair<String, String>(em[i], a.getValue()));
				i++;
				commandRep += emo + " " + a.getKey() + "\n";
			}
			final String commands = commandRep;
			EmbedBuilder emb = new EmbedBuilder();
			if(r.title != null && !r.title.trim().equals(""))
				emb.setTitle(r.title);
			for (Pair<String, String> k : r.fields) {
				String f = k.getValue();
				if (f == null || f.trim().equals(""))
					f = "[EMPTY]";
				emb.addField(k.getKey(), f, false);
//						System.out.println("Added field " + k + " (" + r.fields.get(k) + ")");
			}
			if (r.footer != null)
				emb.setFooter(r.footer, null);

			if (r.description != null)
				emb.setDescription(r.description);

			if (r.color != null)
				emb.setColor(r.color);
			else
				emb.setColor(Color.BLUE);

			if (r.commands.size() != 0) {
				emb.addField("Select", commands, false);
			}
			
			c.sendMessage(emb.build()).queue(msg -> {
				for (Pair<String, String> string : reactions) {
					MessageCommand cmd = new MessageCommand();
					cmd.command = string.getValue();
					
					cmd.reactionEmote = stringToBytes(string.getKey());
					cmd.messageId = msg.getIdLong();
					cmd.source = "Discord";
					MessageCommand.getSmartTable().set(cmd);
					msg.addReaction(string.getKey()).queue();
				}
			});
			
		}

	}
	
	public static String stringToBytes(String str) {
		String s = "";
		byte[] barr = str.getBytes();
		for (int i = 0; i < barr.length; i++) {
			s += " " + barr[i];
		}
		return s.trim();
	}
	
}
