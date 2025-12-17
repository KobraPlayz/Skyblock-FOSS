package com.skyblock.coop;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents an active kick vote.
 */
public class KickVote {

    private final UUID islandId;
    private final UUID targetUuid;
    private final long expiresAt;
    private final Set<UUID> votes;

    public KickVote(UUID islandId, UUID targetUuid, long expiresAt) {
        this.islandId = islandId;
        this.targetUuid = targetUuid;
        this.expiresAt = expiresAt;
        this.votes = new HashSet<>();
    }

    public UUID getIslandId() {
        return islandId;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public void addVote(UUID voterUuid) {
        votes.add(voterUuid);
    }

    public boolean hasVoted(UUID voterUuid) {
        return votes.contains(voterUuid);
    }

    public int getVoteCount() {
        return votes.size();
    }

    public Set<UUID> getVotes() {
        return new HashSet<>(votes);
    }
}
