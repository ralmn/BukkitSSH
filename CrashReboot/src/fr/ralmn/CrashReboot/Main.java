package fr.ralmn.CrashReboot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class Main extends JavaPlugin implements Listener {

	Logger log = Logger.getLogger("Minecraft");

	@Override
	public void onDisable() {
		log.info("BukkitSSH : Disable");
	}

	@Override
	public void onEnable() {
		log.info("BukkitSSH : Activer");
		getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		saveConfig();
		reloadConfig();
	}

	@EventHandler
	public void onPlayerCommandPreprosses(PlayerCommandPreprocessEvent e) {
		final Player p = e.getPlayer();
		String message = e.getMessage();
		String[] args = message.split(" ");

		final String cmd = args[0].replace("/", "");

		if (getConfig().contains(cmd) && p.isOp()) {

			(new Thread() {
				public void run() {
					try {
						executeCommand(cmd);
					} catch (Exception e) {
						e.printStackTrace();
					}
					p.sendMessage(getConfig().getConfigurationSection(cmd)
							.getString("cmd") + " envoyer ! ");
				}
			}).start();

			e.setCancelled(true);

		}

	}

	private void executeCommand(String cmd) throws Exception {
		System.out.println("Debut SSh");
		ConfigurationSection c = getConfig().getConfigurationSection(cmd);
		String h = c.getString("host");
		String u = c.getString("user");
		String p = c.getString("pwd");
		String command = c.getString("cmd");
		Session session = null;
		Channel channel = null;

		JSch jsch = new JSch();
		session = jsch.getSession(u, h);

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.setTimeout(150000);
		session.setPassword(p);
		session.connect();

		channel = session.openChannel("shell");
		// ((ChannelExec) channel).setCommand(command);

		channel.connect();

		channel.setOutputStream(System.out, false);
		PrintStream shellStream = new PrintStream(channel.getOutputStream()); // printStream
																				// for
																				// convenience
		channel.connect();

		shellStream.println(command);
		shellStream.flush();

		Thread.sleep(120000);
		// ((ChannelExec) channel).start();

		shellStream.close();
		channel.setOutputStream(new OutputStream() {

			@Override
			public void write(int b) throws IOException {
			}
		});
		channel.disconnect();
		session.disconnect();
		System.out.println("Fin ssh");
	}
}
