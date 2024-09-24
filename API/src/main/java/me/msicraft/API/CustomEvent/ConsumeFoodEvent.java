package me.msicraft.API.CustomEvent;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class ConsumeFoodEvent extends CustomEvent {

    public enum UseHand {
        HAND, OFFHAND
    }

    private final boolean isSuccess;
    private final double leftCooldown;
    private final Player player;
    private final UseHand useHand;

    public ConsumeFoodEvent(boolean isSuccess, double leftCooldown, Player player, EquipmentSlot useSlot) {
        this.isSuccess = isSuccess;
        this.leftCooldown = leftCooldown;
        this.player = player;
        if (useSlot == EquipmentSlot.HAND) {
            this.useHand = UseHand.HAND;
        } else if (useSlot == EquipmentSlot.OFF_HAND) {
            this.useHand = UseHand.OFFHAND;
        } else {
            this.useHand = UseHand.HAND;
        }
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public double getLeftCooldown() {
        return leftCooldown;
    }

    public Player getPlayer() {
        return player;
    }

    public UseHand getUseHand() {
        return useHand;
    }

}
