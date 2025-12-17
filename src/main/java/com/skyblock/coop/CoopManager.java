package com.skyblock.coop;

import com.skyblock.SkyblockPlugin;
import com.skyblock.island.Island;
import com.skyblock.island.IslandRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages co-op system for islands.
 */
public class CoopManager {

    private final SkyblockPlugin plugin;

    // Pending invites: invitee UUID -> CoopInvite
    private final Map<UUID, CoopInvite> pendingInvites;

    // Active kick votes: island ID -> KickVote
    private final Map<UUID, KickVote> activeKickVotes;

    // Configuration
    private final int inviteExpireMinutes;
    private final int kickVoteExpireMinutes;
    private final int maxMembersDefault;
    private final int maxMembersUpgraded;

    public CoopManager(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.pendingInvites = new ConcurrentHashMap<>();
        this.activeKickVotes = new ConcurrentHashMap<>();

        // Load config
        inviteExpireMinutes = plugin.getConfigManager().getIslandsConfig()
            .getInt("coop.invite_expire_minutes", 5);
        kickVoteExpireMinutes = plugin.getConfigManager().getIslandsConfig()
            .getInt("coop.kick_vote_expire_minutes", 10);
        maxMembersDefault = plugin.getConfigManager().getIslandsConfig()
            .getInt("coop.max_members_default", 5);
        maxMembersUpgraded = plugin.getConfigManager().getIslandsConfig()
            .getInt("coop.max_members_upgraded", 8);

        // Start cleanup task
        startCleanupTask();
    }

    /**
     * Send a co-op invite to a player.
     */
    public void sendInvite(Player inviter, Player invitee, Island island) {
        UUID inviteeUuid = invitee.getUniqueId();

        // Check for existing invite
        if (pendingInvites.containsKey(inviteeUuid)) {
            inviter.sendMessage("§cThat player already has a pending invite!");
            return;
        }

        // Check if already member
        if (island.isMember(inviteeUuid)) {
            inviter.sendMessage("§cThat player is already an island member!");
            return;
        }

        // Create invite
        CoopInvite invite = new CoopInvite(
            island.getId(),
            inviter.getUniqueId(),
            inviteeUuid,
            System.currentTimeMillis(),
            System.currentTimeMillis() + (inviteExpireMinutes * 60 * 1000L)
        );

        pendingInvites.put(inviteeUuid, invite);

        // Save to database
        saveInvite(invite);

        // Notify players
        inviter.sendMessage("§aInvite sent to §e" + invitee.getName() + "§a!");
        inviter.sendMessage("§7The invite will expire in " + inviteExpireMinutes + " minutes.");

        invitee.sendMessage("§6§lCo-op Invite!");
        invitee.sendMessage("§e" + inviter.getName() + " §7has invited you to their island!");
        invitee.sendMessage("§aType §2/coopadd accept §ato join.");
        invitee.sendMessage("§cType §4/coopadd deny §cto decline.");
        invitee.sendMessage("§7This invite expires in " + inviteExpireMinutes + " minutes.");
    }

    /**
     * Accept a co-op invite.
     */
    public void acceptInvite(Player player) {
        CoopInvite invite = pendingInvites.remove(player.getUniqueId());

        if (invite == null) {
            player.sendMessage("§cYou don't have any pending invites!");
            return;
        }

        if (invite.isExpired()) {
            player.sendMessage("§cThis invite has expired!");
            deleteInvite(invite);
            return;
        }

        // Get the island
        plugin.getIslandManager().getIsland(getProfileIdForIsland(invite.getIslandId()))
            .thenAccept(island -> {
                if (island == null) {
                    player.sendMessage("§cThe island no longer exists!");
                    return;
                }

                // Check member limit
                if (island.getMemberCount() >= maxMembersDefault) {
                    player.sendMessage("§cThe island has reached its member limit!");
                    return;
                }

                // Add as member
                island.addMember(player.getUniqueId(), IslandRole.MEMBER);
                plugin.getIslandManager().saveIsland(island);

                // Delete invite from database
                deleteInvite(invite);

                // Notify
                player.sendMessage("§aYou have joined the island!");

                Player inviter = Bukkit.getPlayer(invite.getInviterUuid());
                if (inviter != null) {
                    inviter.sendMessage("§a" + player.getName() + " has joined your island!");
                }

                // Notify all members
                for (UUID memberUuid : island.getMembers().keySet()) {
                    Player member = Bukkit.getPlayer(memberUuid);
                    if (member != null && !member.equals(player) && !member.equals(inviter)) {
                        member.sendMessage("§e" + player.getName() + " §7has joined the island!");
                    }
                }
            });
    }

    /**
     * Deny a co-op invite.
     */
    public void denyInvite(Player player) {
        CoopInvite invite = pendingInvites.remove(player.getUniqueId());

        if (invite == null) {
            player.sendMessage("§cYou don't have any pending invites!");
            return;
        }

        deleteInvite(invite);
        player.sendMessage("§cYou have declined the invite.");

        Player inviter = Bukkit.getPlayer(invite.getInviterUuid());
        if (inviter != null) {
            inviter.sendMessage("§c" + player.getName() + " has declined your island invite.");
        }
    }

    /**
     * Start a kick vote against a member.
     */
    public void startKickVote(Player initiator, Player target, Island island) {
        UUID targetUuid = target.getUniqueId();

        // Check for existing vote
        KickVote existingVote = activeKickVotes.get(island.getId());
        if (existingVote != null && existingVote.getTargetUuid().equals(targetUuid)) {
            // Add vote to existing
            castKickVote(initiator, island);
            return;
        }

        // Create new vote
        KickVote vote = new KickVote(
            island.getId(),
            targetUuid,
            System.currentTimeMillis() + (kickVoteExpireMinutes * 60 * 1000L)
        );
        vote.addVote(initiator.getUniqueId());

        activeKickVotes.put(island.getId(), vote);

        // Check if vote passes immediately (only 2 members = instant kick)
        if (checkKickVote(island, vote)) {
            return;
        }

        // Notify members
        initiator.sendMessage("§aKick vote started against §e" + target.getName());
        target.sendMessage("§c§lWarning: §cA kick vote has been started against you!");

        for (UUID memberUuid : island.getMembers().keySet()) {
            if (!memberUuid.equals(initiator.getUniqueId()) && !memberUuid.equals(targetUuid)) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null) {
                    member.sendMessage("§eA kick vote has been started against §c" + target.getName());
                    member.sendMessage("§7Use §6/coopkick " + target.getName() + " §7to vote.");
                }
            }
        }
    }

    /**
     * Cast a vote to kick a member.
     */
    public void castKickVote(Player voter, Island island) {
        KickVote vote = activeKickVotes.get(island.getId());
        if (vote == null) {
            voter.sendMessage("§cThere is no active kick vote!");
            return;
        }

        if (vote.isExpired()) {
            activeKickVotes.remove(island.getId());
            voter.sendMessage("§cThe kick vote has expired!");
            return;
        }

        if (vote.hasVoted(voter.getUniqueId())) {
            voter.sendMessage("§cYou have already voted!");
            return;
        }

        if (voter.getUniqueId().equals(vote.getTargetUuid())) {
            voter.sendMessage("§cYou can't vote on your own kick!");
            return;
        }

        vote.addVote(voter.getUniqueId());
        voter.sendMessage("§aVote cast!");

        checkKickVote(island, vote);
    }

    /**
     * Check if a kick vote passes.
     */
    private boolean checkKickVote(Island island, KickVote vote) {
        // Get eligible voters (all members except target)
        int eligibleVoters = island.getMemberCount() - 1;

        // Majority needed
        int votesNeeded = (eligibleVoters / 2) + 1;

        if (vote.getVoteCount() >= votesNeeded) {
            // Vote passed - kick the player
            executeKick(island, vote.getTargetUuid());
            activeKickVotes.remove(island.getId());
            return true;
        }

        return false;
    }

    /**
     * Execute the kick of a member.
     */
    private void executeKick(Island island, UUID targetUuid) {
        island.removeMember(targetUuid);
        plugin.getIslandManager().saveIsland(island);

        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null) {
            target.sendMessage("§c§lYou have been kicked from the island!");

            // Teleport to hub if on island
            if (target.getWorld().getName().equals(island.getWorldName())) {
                plugin.getWorldManager().teleportToHub(target);
            }
        }

        // Notify remaining members
        for (UUID memberUuid : island.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null) {
                String targetName = target != null ? target.getName() : targetUuid.toString();
                member.sendMessage("§e" + targetName + " §7has been kicked from the island.");
            }
        }

        // TODO: Implement salvage system for kicked member's items
    }

    /**
     * Leave an island voluntarily.
     */
    public void leaveIsland(Player player, Island island) {
        if (island.isOwner(player.getUniqueId())) {
            player.sendMessage("§cYou can't leave an island you own! Transfer ownership or delete the island.");
            return;
        }

        if (!island.isMember(player.getUniqueId())) {
            player.sendMessage("§cYou're not a member of this island!");
            return;
        }

        island.removeMember(player.getUniqueId());
        plugin.getIslandManager().saveIsland(island);

        player.sendMessage("§cYou have left the island.");

        // Teleport to hub if on island
        if (player.getWorld().getName().equals(island.getWorldName())) {
            plugin.getWorldManager().teleportToHub(player);
        }

        // Notify members
        for (UUID memberUuid : island.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null) {
                member.sendMessage("§e" + player.getName() + " §7has left the island.");
            }
        }
    }

    /**
     * Get pending invite for a player.
     */
    public CoopInvite getPendingInvite(UUID playerUuid) {
        return pendingInvites.get(playerUuid);
    }

    /**
     * Check if player has pending invite.
     */
    public boolean hasPendingInvite(UUID playerUuid) {
        CoopInvite invite = pendingInvites.get(playerUuid);
        return invite != null && !invite.isExpired();
    }

    // Database operations
    private void saveInvite(CoopInvite invite) {
        plugin.getDatabaseManager().executeUpdateAsync(conn -> {
            String sql = """
                INSERT INTO coop_invites (island_id, inviter_uuid, invitee_uuid, invited_at, expires_at)
                VALUES (?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, invite.getIslandId().toString());
                stmt.setString(2, invite.getInviterUuid().toString());
                stmt.setString(3, invite.getInviteeUuid().toString());
                stmt.setLong(4, invite.getInvitedAt());
                stmt.setLong(5, invite.getExpiresAt());
                stmt.executeUpdate();
            }
        });
    }

    private void deleteInvite(CoopInvite invite) {
        plugin.getDatabaseManager().executeUpdateAsync(conn -> {
            String sql = "DELETE FROM coop_invites WHERE island_id = ? AND invitee_uuid = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, invite.getIslandId().toString());
                stmt.setString(2, invite.getInviteeUuid().toString());
                stmt.executeUpdate();
            }
        });
    }

    private int getProfileIdForIsland(UUID islandId) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "SELECT profile_id FROM islands WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, islandId.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("profile_id");
                    }
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.WARNING, "Failed to get profile ID for island: " + e.getMessage());
        }
        return -1;
    }

    private void startCleanupTask() {
        // Clean up expired invites and votes every minute
        Bukkit.getScheduler().runTaskTimerAsync(plugin, () -> {
            long now = System.currentTimeMillis();

            // Clean expired invites
            pendingInvites.entrySet().removeIf(entry -> {
                if (entry.getValue().isExpired()) {
                    deleteInvite(entry.getValue());
                    return true;
                }
                return false;
            });

            // Clean expired votes
            activeKickVotes.entrySet().removeIf(entry -> entry.getValue().isExpired());

        }, 20 * 60, 20 * 60); // Every minute
    }

    public void shutdown() {
        // Clean up pending invites
        for (CoopInvite invite : pendingInvites.values()) {
            deleteInvite(invite);
        }
    }
}
