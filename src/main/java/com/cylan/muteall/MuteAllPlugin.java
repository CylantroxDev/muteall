package com.cylan.muteall;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MuteAllPlugin extends JavaPlugin implements VoicechatPlugin, Listener {

    // Cambia a false si NO quieres silenciar el chat o la voz
    private static final boolean MUTE_CHAT = true;
    private static final boolean MUTE_VOICE = true;

    private static final String BYPASS_PERMISSION = "muteall.bypass";

    private volatile boolean muted = false;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            service.registerPlugin(this);
        } else {
            getLogger().warning("Simple Voice Chat no esta cargado: solo funcionara el mute del chat.");
        }
        getLogger().info("MuteAll activado.");
    }

    // ---------- Comando /mute ----------

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        muted = !muted;
        String name = (sender instanceof Player) ? sender.getName() : "Consola";
        if (muted) {
            Bukkit.broadcastMessage(name + ": Creo que es buen momento para guardar silencio.");
        } else {
            Bukkit.broadcastMessage(name + ": Creo que es buen momento para hablar.");
        }
        return true;
    }

    // ---------- Chat ----------

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        if (!MUTE_CHAT || !muted) return;
        Player player = event.getPlayer();
        if (canSpeak(player)) return;
        event.setCancelled(true);
        player.sendMessage("§cEl chat esta silenciado.");
    }

    // ---------- Voz (Simple Voice Chat) ----------

    @Override
    public String getPluginId() {
        return "muteall";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
    }

    private void onMicrophone(MicrophonePacketEvent event) {
        if (!MUTE_VOICE || !muted) return;
        if (event.getSenderConnection() == null) return;
        if (!(event.getSenderConnection().getPlayer().getPlayer() instanceof Player player)) return;
        if (canSpeak(player)) return;
        event.cancel();
    }

    private boolean canSpeak(Player player) {
        return player.isOp() || player.hasPermission(BYPASS_PERMISSION);
    }
}
