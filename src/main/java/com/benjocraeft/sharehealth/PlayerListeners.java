package com.benjocraeft.sharehealth;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;


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
        Entity damagedEntity = event.getEntity();
        double damage = event.getFinalDamage();

        DamageCause cause = event.getCause();
        // not allowed triggering message
        // because these types trigger an extra event by entity or by block with more
        // detailed information for the message
        DamageCause[] messageNotAllowed = new DamageCause[]{
                DamageCause.ENTITY_ATTACK,
                DamageCause.ENTITY_EXPLOSION,
                DamageCause.PROJECTILE,
                DamageCause.CONTACT
        };

        if (damagedEntity instanceof Player){
            boolean isMessageAllowed = !Arrays.asList(messageNotAllowed).contains(cause);
            double absorbedDamage = -event.getOriginalDamage(DamageModifier.ABSORPTION);
            Sharehealth.Instance.onPlayerGotDamage((Player) damagedEntity, damage, cause, isMessageAllowed, absorbedDamage);
        }
    }

    @EventHandler
    public void onEntityGotDamageByEntity(final EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();
        double damage = event.getFinalDamage();
        Entity cause = event.getDamager();

        if (entity instanceof Player) {
            double absorbedDamage = -event.getOriginalDamage(DamageModifier.ABSORPTION);
            Sharehealth.Instance.onPlayerGotDamageByEntity((Player)entity, damage, cause, absorbedDamage);
        }
    }

    @EventHandler
    public void onEntityGotDamageByBlock(final EntityDamageByBlockEvent event){
        Entity entity = event.getEntity();
        double damage = event.getFinalDamage();
        Block cause = event.getDamager();

        if (entity instanceof Player) {
            double absorbedDamage = -event.getOriginalDamage(DamageModifier.ABSORPTION);
            Sharehealth.Instance.onPlayerGotDamageByBlock((Player)entity, damage, cause, absorbedDamage);
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
