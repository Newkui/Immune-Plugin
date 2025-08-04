package myimmune;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class Main extends JavaPlugin implements Listener {
    private int immuneSeconds;
    private boolean enableInvisibility;
    private boolean enableDamageResistance;
    private boolean enableSpeed;
    private boolean enableRegeneration;

    // Visual
    private boolean enableGlowing;
    private boolean enableParticles;
    private Particle particleType;

    private String messageStart;
    private String messageEnd;
    private boolean allWorlds;
    private List<String> allowedWorlds;

    public Main() {}

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.loadConfigSettings();
        this.getLogger().info("MyImmunePlugin enabled!");
        Bukkit.getPluginManager().registerEvents(this, this);

        // Command to check immunity status and enabled effects
        this.getCommand("immunecheck").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player player) {
                player.sendMessage(ChatColor.GREEN + "Immunity duration: " + this.immuneSeconds + " seconds.");
                player.sendMessage(ChatColor.GREEN + "Invisibility: " + (enableInvisibility ? "Enabled" : "Disabled"));
                player.sendMessage(ChatColor.GREEN + "Resistance: " + (enableDamageResistance ? "Enabled" : "Disabled"));
                player.sendMessage(ChatColor.GREEN + "Speed: " + (enableSpeed ? "Enabled" : "Disabled"));
                player.sendMessage(ChatColor.GREEN + "Regeneration: " + (enableRegeneration ? "Enabled" : "Disabled"));
                player.sendMessage(ChatColor.GREEN + "Glowing: " + (enableGlowing ? "Enabled" : "Disabled"));
                player.sendMessage(ChatColor.GREEN + "Particles: " + (enableParticles ? particleType.name() : "Disabled"));
                return true;
            }
            return false;
        });
    }

    private void loadConfigSettings() {
        this.immuneSeconds = this.getConfig().getInt("immunity-seconds", 15);

        // Potion effects
        this.enableInvisibility = this.getConfig().getBoolean("effects.invisibility", true);
        this.enableDamageResistance = this.getConfig().getBoolean("effects.resistance", true);
        this.enableSpeed = this.getConfig().getBoolean("effects.speed", false);
        this.enableRegeneration = this.getConfig().getBoolean("effects.regeneration", false);

        // Visual effects
        this.enableGlowing = this.getConfig().getBoolean("visual.glowing", true);
        this.enableParticles = this.getConfig().getBoolean("visual.particles", true);

        String particleName = this.getConfig().getString("visual.particle-type", "ENCHANTMENT_TABLE");
        try {
            this.particleType = Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.particleType = Particle.ENCHANTMENT_TABLE;
            getLogger().warning("Invalid particle type in config.yml, defaulting to ENCHANTMENT_TABLE");
        }

        // Messages
        this.messageStart = ChatColor.translateAlternateColorCodes('&',
                this.getConfig().getString("messages.start", "&aYou are immune for %time% seconds!"))
                .replace("%time%", String.valueOf(immuneSeconds));
        this.messageEnd = ChatColor.translateAlternateColorCodes('&',
                this.getConfig().getString("messages.end", "&cYour immunity has ended!"));

        // World settings
        this.allWorlds = this.getConfig().getBoolean("settings.all-worlds", true);
        this.allowedWorlds = this.getConfig().getStringList("settings.worlds");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("MyImmunePlugin disabled!");
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        // Check if world is allowed
        if (!allWorlds && !allowedWorlds.contains(world.getName())) {
            return;
        }

        this.getLogger().info("Player respawn event triggered for: " + player.getName());

        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Potion effects
            if (this.enableDamageResistance) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * this.immuneSeconds, 4, false, false));
            }

            if (this.enableInvisibility) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * this.immuneSeconds, 1, false, false));
            }

            if (this.enableSpeed) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * this.immuneSeconds, 1, false, false));
            }

            if (this.enableRegeneration) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * this.immuneSeconds, 1, false, false));
            }

            // Glowing effect
            if (this.enableGlowing) {
                player.setGlowing(true);
                Bukkit.getScheduler().runTaskLater(this, () -> player.setGlowing(false), immuneSeconds * 20L);
            }

            // Particle loop
            if (this.enableParticles) {
                long immuneEnd = System.currentTimeMillis() + (immuneSeconds * 1000L);
                Bukkit.getScheduler().runTaskTimer(this, task -> {
                    if (!player.isOnline() || System.currentTimeMillis() >= immuneEnd) {
                        task.cancel();
                        return;
                    }
                    player.getWorld().spawnParticle(
                            particleType,
                            player.getLocation().add(0, 1, 0),
                            20, 0.5, 1, 0.5, 0.1
                    );
                }, 0L, 20L); // every 1 second
            }

            // Start & end messages
            player.sendMessage(messageStart);
            Bukkit.getScheduler().runTaskLater(this, () -> player.sendMessage(messageEnd), immuneSeconds * 20L);

        }, 1L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.sendMessage(ChatColor.RED + "You died! You will be immune for " + this.immuneSeconds + " seconds after respawn.");
    }
}
