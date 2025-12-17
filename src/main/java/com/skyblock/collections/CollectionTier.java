package com.skyblock.collections;

import java.util.List;

/**
 * Represents a collection tier with requirements and rewards.
 */
public class CollectionTier {

    private final int tier;
    private final long requirement;
    private final List<CollectionReward> rewards;

    public CollectionTier(int tier, long requirement, List<CollectionReward> rewards) {
        this.tier = tier;
        this.requirement = requirement;
        this.rewards = rewards;
    }

    public int getTier() {
        return tier;
    }

    public long getRequirement() {
        return requirement;
    }

    public List<CollectionReward> getRewards() {
        return rewards;
    }
}
