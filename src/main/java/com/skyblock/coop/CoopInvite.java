package com.skyblock.coop;

import java.util.UUID;

/**
 * Represents a pending co-op invite.
 */
public class CoopInvite {

    private final UUID islandId;
    private final UUID inviterUuid;
    private final UUID inviteeUuid;
    private final long invitedAt;
    private final long expiresAt;

    public CoopInvite(UUID islandId, UUID inviterUuid, UUID inviteeUuid, long invitedAt, long expiresAt) {
        this.islandId = islandId;
        this.inviterUuid = inviterUuid;
        this.inviteeUuid = inviteeUuid;
        this.invitedAt = invitedAt;
        this.expiresAt = expiresAt;
    }

    public UUID getIslandId() {
        return islandId;
    }

    public UUID getInviterUuid() {
        return inviterUuid;
    }

    public UUID getInviteeUuid() {
        return inviteeUuid;
    }

    public long getInvitedAt() {
        return invitedAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public long getTimeRemaining() {
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }
}
