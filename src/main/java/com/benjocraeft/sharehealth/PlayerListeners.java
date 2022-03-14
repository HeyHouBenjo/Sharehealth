package com.benjocraeft.sharehealth;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;


public class PlayerListeners implements Listener{

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if (Sharehealth.GetPlayers().contains(p))
            Sharehealth.Instance.onPlayerJoin(e.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent e){
        Player p = e.getPlayer();
        if (Sharehealth.GetPlayers().contains(p))
            Sharehealth.Instance.onPlayerRespawn(e.getPlayer());
    }

    //Normal Totem Of Undying interaction is disabled
    @EventHandler
    public void onResurrect(final EntityResurrectEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityGotDamage(final EntityDamageEvent event){
        Entity damagedEntity = event.getEntity();

        if (damagedEntity instanceof Player){
            if (!Sharehealth.GetPlayers().contains(damagedEntity))
                return;

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

            boolean isMessageAllowed = !Arrays.asList(messageNotAllowed).contains(cause);
            double absorbedDamage = -event.getOriginalDamage(DamageModifier.ABSORPTION);
            Sharehealth.Instance.onPlayerGotDamage((Player) damagedEntity, damage, cause, isMessageAllowed, absorbedDamage, event::setCancelled);
        }
    }

    //Only for logging/messaging
    @EventHandler
    public void onEntityGotDamageByEntity(final EntityDamageByEntityEvent event){
        Entity damagedEntity = event.getEntity();

        if (damagedEntity instanceof Player) {
            if (!Sharehealth.GetPlayers().contains(damagedEntity))
                return;

            double damage = event.getFinalDamage();
            Entity cause = event.getDamager();
            double absorbedDamage = -event.getOriginalDamage(DamageModifier.ABSORPTION);
            Sharehealth.Instance.onPlayerGotDamageByEntity((Player)damagedEntity, damage, cause, absorbedDamage);
        }
    }

    //Only for logging/messaging
    @EventHandler
    public void onEntityGotDamageByBlock(final EntityDamageByBlockEvent event){
        Entity damagedEntity = event.getEntity();

        if (damagedEntity instanceof Player) {
            if (!Sharehealth.GetPlayers().contains(damagedEntity))
                return;

            double damage = event.getFinalDamage();
            Block cause = event.getDamager();
            double absorbedDamage = -event.getOriginalDamage(DamageModifier.ABSORPTION);
            Sharehealth.Instance.onPlayerGotDamageByBlock((Player)damagedEntity, damage, cause, absorbedDamage);
        }
    }

    @EventHandler
    public void onEntityRegainedHealth(final EntityRegainHealthEvent event){
        Entity healedEntity = event.getEntity();

        if (healedEntity instanceof Player){
            if (!Sharehealth.GetPlayers().contains(healedEntity))
                return;

            double amount = event.getAmount();
            RegainReason reason = event.getRegainReason();
            if (!Sharehealth.Instance.onPlayerRegainedHealth((Player) healedEntity, amount, reason)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityPotionEffectModified(final EntityPotionEffectEvent event){
        Entity entity = event.getEntity();
        if (entity instanceof Player){
            if (!Sharehealth.GetPlayers().contains(entity))
                return;
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

    @EventHandler
    public void onPlayerItemConsumed(final PlayerItemConsumeEvent event){
        Player consumer = event.getPlayer();
        if (!Sharehealth.GetPlayers().contains(consumer))
            return;

        if (event.getItem().getType().equals(Material.MILK_BUCKET))
            Sharehealth.Instance.onMilkBucketConsumed();
    }
}
