package org.xbf.addons.discord.http_addon;

import java.util.HashMap;
import java.util.Random;

import org.xbf.addons.core_http.HttpHandler;
import org.xbf.addons.core_http.HttpStatus;
import org.xbf.addons.core_http.KeyLevel;
import org.xbf.addons.core_http.KeyManager;
import org.xbf.addons.core_http.annotations.HttpParameter;
import org.xbf.addons.core_http.annotations.Permission;
import org.xbf.addons.core_http.models.APIKey;
import org.xbf.addons.discord.DiscordPlugin;
import org.xbf.core.Models.XUser;
import org.xbf.core.Utils.Map.FastMap;

import bell.oauth.discord.main.OAuthBuilder;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.CookieHandler;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public class Login extends HttpHandler {
	
	public Login() {
		level = KeyLevel.NONE;
	}
	
	OAuthBuilder builder;
	
	private OAuthBuilder getOAuthBuilder() {
		if(builder == null) {
			builder = new OAuthBuilder(DiscordPlugin.cfg.clientId, DiscordPlugin.cfg.clientSecret)
					.setScopes(new String[] {"identify"})
					.setRedirectURI(DiscordPlugin.cfg.redirectUri);
		}
		return builder;
	}

	private static HashMap<String, String> returnAddresses = new HashMap<>();
	
	private Response applyTracker(IHTTPSession sess, Response r, String sourcePath) {
		int leftLimit = 97; // letter 'a'
	    int rightLimit = 122; // letter 'z'
	    int targetStringLength = 10;
	    Random random = new Random();
	    StringBuilder buffer = new StringBuilder(targetStringLength);
	    for (int i = 0; i < targetStringLength; i++) {
	        int randomLimitedInt = leftLimit + (int) 
	          (random.nextFloat() * (rightLimit - leftLimit + 1));
	        buffer.append((char) randomLimitedInt);
	    }
	    String generatedString = buffer.toString();
	    CookieHandler ch = sess.getCookies();
		ch.set("LOGIN", generatedString, 1);
		ch.unloadQueue(r);
		System.out.println("Set logon key to " + generatedString);
		returnAddresses.put(generatedString.trim(), sourcePath);
		return r;
	}
	
	private Response redir(String redir) {
		Response r = NanoHTTPD.newFixedLengthResponse(HttpStatus.TEMPORARY_REDIRECT, "text", "Redirecting...");
		r.addHeader("location", redir);
		return r;
	}
	
	public Response begin(IHTTPSession sess, @HttpParameter("clientId") String clientId) {
		String key = getKey(sess);
		if(key != null) {
			return redir(getReturnAddr(clientId) + "?key=" + key);
		}
		String redir = getOAuthBuilder().getAuthorizationUrl(null);
		return applyTracker(sess, redir(redir), clientId);
	}
	
	public Response login(IHTTPSession sess, @HttpParameter("code") String code) {
		bell.oauth.discord.main.Response res = getOAuthBuilder().exchange(code);
		if(res == bell.oauth.discord.main.Response.ERROR) {
			return NanoHTTPD.newFixedLengthResponse("An error occured");
		} else {
			int uid = XUser.getFromProvider(builder.getUser().getId(), "Discord", builder.getUser().getUsername()).id;
			String loginKey = sess.getCookies().read("LOGIN").trim();
			System.out.println("Got login key " + loginKey);
			System.out.println(returnAddresses.get(loginKey));
			if(!returnAddresses.containsKey(loginKey)) {
				return NanoHTTPD.newFixedLengthResponse("Login Failed! (No login key, please allow cookies)");
			}
			String key = getOrCreate(uid);
			Response r = redir(getReturnAddr(returnAddresses.get(loginKey)) + "?key=" + key);
			returnAddresses.remove(loginKey);
			CookieHandler ch = sess.getCookies();
			ch.set("APIKEY", key, 1);
			ch.unloadQueue(r);
			return r;
		}
	}
	
	private String getReturnAddr(String string) {
		return DiscordPlugin.cfg.clients.get(string);
	}

	public String getKey(IHTTPSession sess) {
		String cookie = sess.getCookies().read("APIKEY");
		if(cookie != null) return cookie;
		String header = sess.getHeaders().get("NRPAPI");
		return header;
	}
	
	@Permission("discord.http.getOrCreate")
	public String getOrCreate(int uid) {
		APIKey key = APIKey.getSmartTable().get(new FastMap<String, String>().add("uid", uid + ""));
		if(key != null) return key.apiKey;
		key = new APIKey();
		key.apiKey = KeyManager.generateKey(uid);
		key.active = true;
		key.uid = uid;
		key.keyLevelInt = KeyLevel.USER.level;
		APIKey.getSmartTable().set(key);
		return key.apiKey;
	}
	
	public LoginData getLogin(APIKey apiKey) {
		if(apiKey == null) return null;
		String key = apiKey.apiKey;
		if(key == null) return null;
		LoginData d = new LoginData();
		APIKey k = KeyManager.getKey(key);
		XUser x = new XUser((int) k.uid);
		d.username = x.getName("*");
		d.userId = x.id;
		d.apiKey = key;
		return d;
	}
	
}
