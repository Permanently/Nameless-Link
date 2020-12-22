package com.namelessmc.bot.listeners;

import java.util.Optional;

import com.namelessmc.bot.Language;
import com.namelessmc.bot.Language.Term;
import com.namelessmc.bot.Main;
import com.namelessmc.bot.Utils;
import com.namelessmc.bot.connections.BackendStorageException;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildJoinHandler extends ListenerAdapter {

	@Override
	public void onGuildJoin(final GuildJoinEvent event) {
		Main.getLogger().info("Joined guild: " + event.getGuild().getName());

		Language language = Language.getDefaultLanguage();

		Optional<NamelessAPI> api;
		try {
			api = Main.getConnectionManager().getApi(event.getGuild().getIdLong());
		} catch (final BackendStorageException e) {
			e.printStackTrace();
			return;
		}
		
		final String apiUrlCommand = "!apiurl"; // TODO Configurable command prefix
		final long guildId = event.getGuild().getIdLong();

		if (api.isEmpty()) {
			// DM owner that we don't have an api for this guild
			Utils.messageGuildOwner(event.getGuild().getId(), language.get(Term.GUILD_JOIN_SUCCESS, "command", apiUrlCommand, "guildId", guildId));
			Main.getLogger().info("Sent new join message to " + event.getGuild().retrieveOwner().complete().getEffectiveName()
					+ " for guild " + event.getGuild().getName());
		} else {
			try {
				api.get().checkWebAPIConnection();
				// Good to go
				language = Language.getDiscordUserLanguage(api.get(),
						event.getGuild().retrieveOwner().complete().getUser());
				Utils.messageGuildOwner(event.getGuild().getId(), language.get(Term.GUILD_JOIN_WELCOME_BACK, "command", apiUrlCommand, "guildId", guildId));
			} catch (final NamelessException e) {
				// Error with their stored url. Make them update the url
				Utils.messageGuildOwner(event.getGuild().getId(), language.get(Term.GUILD_JOIN_NEEDS_RENEW, "command", apiUrlCommand, "guildId", guildId));
			}
		}
	}

}
