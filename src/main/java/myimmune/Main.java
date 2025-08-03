// Source code is decompiled from a .class file using FernFlower decompiler.
package myimmune;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

   // Added allowedWorlds list, no changes to existing variables/methods
   private List<String> allowedWorlds;

   public Main() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      this.loadConfigSettings();

      // Load allowed worlds list from config (new addition)
      this.allowedWorlds = getConfig().getStringList("allowed-worlds");
      if (allowedWorlds == null) {
         allowedWorlds = List.of(); // empty list if none specified
      }

      this.getLogger().info("MyImmunePlugin enabled!");
      Bukkit.getPluginManager().registerEvents(this, this);
      this.getCommand("immunecheck").setExecutor((sender, command, label, args) -> {
         if (sender instanceof Player player) {
            String var10001 = String.valueOf(ChatColor.GREEN);
            player.sendMessage(var10001 + "You are immune for " + this.immuneSeconds + " seconds after respawn!");
            return true;
         } else {
            return false;
         }
      });
   }

   private void loadConfigSettings() {
      this.immuneSeconds = this.getConfig().getInt("immune-seconds", 15);
      this.enableInvisibility = this.getConfig().getBoolean("enable-invisibility", true);
      this.enableDamageResistance = this.getConfig().getBoolean("enable-damage-resistance", true);
   }

   public void onDisable() {
      this.getLogger().info("MyImmunePlugin disabled!");
   }

   @EventHandler
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      Player player = event.getPlayer();

      // Check if allowedWorlds is set and if player's world is allowed
      if (!allowedWorlds.isEmpty() && !allowedWorlds.contains(player.getWorld().getName())) {
         return; // Do nothing if player is not in an allowed world
      }

      this.getLogger().info("Player respawn event triggered for: " + player.getName());
      Bukkit.getScheduler().runTaskLater(this, () -> {
         if (this.enableDamageResistance) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * this.immuneSeconds, 4, false, false));
         }

         if (this.enableInvisibility) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * this.immuneSeconds, 1, false, false));
         }

         String var10001 = String.valueOf(ChatColor.YELLOW);
         player.sendMessage(var10001 + "You are now immune" + (this.enableInvisibility ? " and invisible" : "") + " for " + this.immuneSeconds + " seconds!");
      }, 1L);
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      String var10001 = String.valueOf(ChatColor.RED);
      player.sendMessage(var10001 + "You died! You will be immune for " + this.immuneSeconds + " seconds after respawn.");
   }
}
