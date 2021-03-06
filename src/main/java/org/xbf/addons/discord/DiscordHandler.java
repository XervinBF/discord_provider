package org.xbf.addons.discord;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

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

import com.google.gson.Gson;

import ch.qos.logback.classic.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

@XHandler(providerName="discord")
public class DiscordHandler extends Handler {

	public JDA client;

	private boolean ready;

	private Logger l = (Logger) LoggerFactory.getLogger(getClass());

	public static ArrayList<Object> customEventHandlers = new ArrayList<Object>();
	
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
					.addEventListeners(new ReactionListener())
					.addEventListeners(customEventHandlers.toArray(new EventListener[0]));
			
					
			l.info("Building client");
			client = null;
			try {
				client = builder.build();
			} catch (LoginException e1) {
				l.error("Login failed!");
				e1.printStackTrace();
			}
			client = client.awaitReady();
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
	public void sendMessageToChannel(String channelId, Response message) {
		SendResponse(message, client.getTextChannelById(channelId));
	}

	@Override
	public void stop() {
		client.shutdown();
		client = null;
		super.stop();
	}

	public final String[] em = new String[] { "\uD83C\uDDE6", "\uD83C\uDDE7", "\uD83C\uDDE8", "\uD83C\uDDE9",
			"\uD83C\uDDEA", "\uD83C\uDDEB", "\uD83C\uDDEC", "\uD83C\uDDED", "\uD83C\uDDEE", "\uD83C\uDDEF",
			"\uD83C\uDDF0", "\uD83C\uDDF1", "\uD83C\uDDF2", "\uD83C\uDDF3", "\uD83C\uDDF4", "\uD83C\uDDF5",
			"\uD83C\uDDF6", "\uD83C\uDDF7", "\uD83C\uDDF8", "\uD83C\uDDF9", "\uD83C\uDDFA", "\uD83C\uDDFB",
			"\uD83C\uDDFC", "\uD83C\uDDFD", "\uD83C\uDDFE", "\uD83C\uDDFF" };

	public void RunCommand(Request req, MessageChannel c, long userId, long messageId, User u) {
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

	public void RunCommand(final Request req, final MessageChannel c, final long messageId, final User usr) {
		if(req.user == null) return;
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
//				l.trace(new Gson().toJson(req));
				Response res = CommandProcessor.runRequest(req);
//				l.trace(new Gson().toJson(res));
				if (res == null)
					return;

				if (res.removeMessage) {
					c.retrieveMessageById(messageId).complete().delete().complete();
				}

				if(res.destination == ResponseDestination.USER_PRIVATE) {
					SendResponse(res, usr.openPrivateChannel().complete());
				} else if(res.destination == ResponseDestination.SAME_CHANNEL) {
					SendResponse(res, c);
				}
			}
		}, CommandProcessor.getGenericRequestThreadName(req));
		t.start();
	}

	public void SendResponse(Response res, MessageChannel c) {
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
				if(r.url != null) {
					emb.setTitle(r.title, r.url);
				} else {
					emb.setTitle(r.title);
				}
			for (Pair<String, String> k : r.fields) {
				String f = k.getValue();
				if (f == null || f.trim().equals(""))
					f = "[EMPTY]";
				emb.addField(k.getKey(), f, false);
//						System.out.println("Added field " + k + " (" + r.fields.get(k) + ")");
			}
			if (r.footer != null) {
				if(r.footerImage != null) {
					emb.setFooter(r.footer, r.footerImage);
				} else {
					emb.setFooter(r.footer, null);
				}
			}

			if (r.description != null)
				emb.setDescription(r.description);

			if (r.color != null)
				emb.setColor(r.color);
			else
				emb.setColor(Color.BLUE);
			
			System.out.println(r.title + ": Img(" + r.image + ")");
			
			if(r.image != null)
				emb.setImage(r.image);
			
			if (r.commands.size() != 0) {
				emb.addField("Select", commands, false);
			}
			
			MessageAction act = c.sendMessage(emb.build());
			
			if(r.image != null){
				InputStream file;
				try {
					file = new URL(r.image).openStream();
					emb.setImage("attachment://eimg.png");
					act = c.sendFile(file, "eimg.png").embed(emb.build());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			act.queue(msg -> {
				for (Pair<String, String> string : reactions) {
					MessageCommand cmd = new MessageCommand();
					cmd.command = string.getValue();
					cmd.reactionEmote = "e:" + Arrays.asList(em).indexOf(string.getKey());
					cmd.messageId = msg.getIdLong();
					cmd.source = "Discord";
					MessageCommand.getSmartTable().set(cmd);
					msg.addReaction(string.getKey()).queue();
				}
			});
			
		}

	}
	
	public String stringToBytes(String str) {
		String s = "";
		byte[] barr = str.getBytes();
		for (int i = 0; i < barr.length; i++) {
			s += " " + barr[i];
		}
		return s.trim();
	}
	
}
