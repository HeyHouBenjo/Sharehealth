package com.benjocraeft.sharehealth;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class PlayerListeners implements Listener{

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Sharehealth.Instance.onPlayerJoin(e.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event){
        Sharehealth.Instance.onPlayerRespawn(event.getPlayer());
    }

    @EventHandler
    public void onEntityGotDamage(final EntityDamageEvent event){
        Entity entity = event.getEntity();
        double damage = event.getFinalDamage();

        DamageCause cause = event.getCause();

        DamageCause[] notAllowed = new DamageCause[]{
                DamageCause.ENTITY_ATTACK,
                DamageCause.ENTITY_EXPLOSION,
                DamageCause.PROJECTILE,
                DamageCause.CONTACT
        };

        if (entity instanceof Player){
            boolean allowed = true;
            for (DamageCause damageCause : notAllowed) {
                if (cause.equals(damageCause)){
                    allowed = false;
                    break;
                }
            }
            double originalAbsorptionDamage = -event.getOriginalDamage(DamageModifier.ABSORPTION);
            Sharehealth.Instance.onPlayerGotDamage((Player) entity, damage, cause, allowed, originalAbsorptionDamage);
        }
    }

    @EventHandler
    public void onEntityGotDamageByEntity(final EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();
        double damage = event.getFinalDamage();
        Entity cause = event.getDamager();

        if (entity instanceof Player) {
            Sharehealth.Instance.onPlayerGotDamageByEntity((Player)entity, damage, cause);
        }
    }

    @EventHandler
    public void onEntityGotDamageByBlock(final EntityDamageByBlockEvent event){
        Entity entity = event.getEntity();
        double damage = event.getFinalDamage();
        Block cause = event.getDamager();

        if (entity instanceof Player) {
            Sharehealth.Instance.onPlayerGotDamageByBlock((Player)entity, damage, cause);
        }
    }

    @EventHandler
    public void onEntityRegainedHealth(final EntityRegainHealthEvent event){
        Entity entity = event.getEntity();
        double amount = event.getAmount();
        RegainReason reason = event.getRegainReason();

        if (entity instanceof Player){
            Player player = (Player) entity;
            if (!Sharehealth.Instance.onPlayerRegainedHealth(player, amount, reason)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityPotionEffectModified(final EntityPotionEffectEvent event){
        Entity entity = event.getEntity();
        if (entity instanceof Player){
            PotionEffect newEffect = event.getNewEffect();
            if (newEffect != null){
                if (newEffect.getType().equals(PotionEffectType.ABSORPTION)){
                    event.setCancelled(true);
                    int amplifier = newEffect.getAmplifier();
                    int duration = newEffect.getDuration();
                    Sharehealth.Instance.onAbsorptionConsumed(duration, amplifier);
                }
            }
        }
    }
}
