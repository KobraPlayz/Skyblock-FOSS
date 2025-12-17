package com.skyblock.items.abilities;

import java.util.List;

/**
 * Represents an item ability.
 */
public class ItemAbility {

    private final String name;
    private final AbilityTrigger trigger;
    private final List<String> description;
    private final int cooldownSeconds;
    private final int manaCost;

    public ItemAbility(String name, AbilityTrigger trigger, List<String> description,
                       int cooldownSeconds, int manaCost) {
        this.name = name;
        this.trigger = trigger;
        this.description = description;
        this.cooldownSeconds = cooldownSeconds;
        this.manaCost = manaCost;
    }

    public String getName() {
        return name;
    }

    public AbilityTrigger getTrigger() {
        return trigger;
    }

    public List<String> getDescription() {
        return description;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public int getManaCost() {
        return manaCost;
    }

    /**
     * Get the trigger display name.
     */
    public String getTriggerDisplay() {
        return trigger.getDisplayName();
    }

    /**
     * Check if this ability has a cooldown.
     */
    public boolean hasCooldown() {
        return cooldownSeconds > 0;
    }

    /**
     * Check if this ability costs mana.
     */
    public boolean costsMana() {
        return manaCost > 0;
    }

    /**
     * Builder class for ItemAbility.
     */
    public static class Builder {
        private String name;
        private AbilityTrigger trigger = AbilityTrigger.RIGHT_CLICK;
        private List<String> description;
        private int cooldownSeconds = 0;
        private int manaCost = 0;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder trigger(AbilityTrigger trigger) {
            this.trigger = trigger;
            return this;
        }

        public Builder trigger(String trigger) {
            this.trigger = AbilityTrigger.fromString(trigger);
            return this;
        }

        public Builder description(List<String> description) {
            this.description = description;
            return this;
        }

        public Builder cooldown(int seconds) {
            this.cooldownSeconds = seconds;
            return this;
        }

        public Builder manaCost(int cost) {
            this.manaCost = cost;
            return this;
        }

        public ItemAbility build() {
            return new ItemAbility(name, trigger, description, cooldownSeconds, manaCost);
        }
    }
}
