package com.skyblock.pets;

import com.skyblock.SkyblockPlugin;
import com.skyblock.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Data Access Object for Pet persistence.
 * Handles all database operations for pets using async patterns.
 *
 * @since Phase 2.0
 */
public class PetDAO {

    private final SkyblockPlugin plugin;
    private final DatabaseManager databaseManager;

    public PetDAO(SkyblockPlugin plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    // ==================== LOAD OPERATIONS ====================

    /**
     * Load all pets for a player asynchronously (PREFERRED METHOD).
     *
     * @param playerUuid The player's UUID
     * @return CompletableFuture with list of pets
     */
    public CompletableFuture<List<Pet>> loadPetsAsync(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> loadPetsSync(playerUuid));
    }

    /**
     * Load all pets for a player synchronously.
     * Used internally and for critical paths.
     *
     * @param playerUuid The player's UUID
     * @return List of pets, or empty list on error
     */
    public List<Pet> loadPetsSync(UUID playerUuid) {
        String sql = """
            SELECT p.id, p.profile_id, p.pet_type, p.rarity, p.level, p.xp,
                   p.candy_used, p.held_item, p.is_active
            FROM pets p
            INNER JOIN profiles pr ON p.profile_id = pr.id
            WHERE pr.player_uuid = ? AND pr.active = 1
            ORDER BY p.is_active DESC, p.level DESC
            """;

        List<Pet> pets = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Pet pet = parsePet(rs);
                    if (pet != null) {
                        pets.add(pet);
                    }
                }
            }

            plugin.debug("Loaded " + pets.size() + " pets for player " + playerUuid);
            return pets;

        } catch (SQLException e) {
            plugin.log(Level.WARNING, "Failed to load pets for player " + playerUuid + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Load a single pet by ID asynchronously.
     *
     * @param petId The pet ID
     * @return CompletableFuture with pet, or null if not found
     */
    public CompletableFuture<Pet> loadPetAsync(int petId) {
        return CompletableFuture.supplyAsync(() -> loadPetSync(petId));
    }

    /**
     * Load a single pet by ID synchronously.
     *
     * @param petId The pet ID
     * @return The pet, or null if not found
     */
    public Pet loadPetSync(int petId) {
        String sql = """
            SELECT id, profile_id, pet_type, rarity, level, xp,
                   candy_used, held_item, is_active
            FROM pets
            WHERE id = ?
            """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, petId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return parsePet(rs);
                }
            }

        } catch (SQLException e) {
            plugin.log(Level.WARNING, "Failed to load pet " + petId + ": " + e.getMessage());
        }

        return null;
    }

    // ==================== SAVE OPERATIONS ====================

    /**
     * Save a pet asynchronously (PREFERRED METHOD).
     * Creates new pet if id = 0, otherwise updates existing.
     *
     * @param pet The pet to save
     * @return CompletableFuture that completes when save is done
     */
    public CompletableFuture<Void> savePetAsync(Pet pet) {
        return CompletableFuture.runAsync(() -> savePetSync(pet));
    }

    /**
     * Save a pet synchronously.
     * Creates new pet if id = 0, otherwise updates existing.
     *
     * @param pet The pet to save
     */
    public void savePetSync(Pet pet) {
        if (pet.getId() == 0) {
            insertPet(pet);
        } else {
            updatePet(pet);
        }
    }

    /**
     * Insert a new pet into the database.
     */
    private void insertPet(Pet pet) {
        String sql = """
            INSERT INTO pets (profile_id, pet_type, rarity, level, xp, candy_used, held_item, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pet.getProfileId());
            stmt.setString(2, pet.getType().name());
            stmt.setString(3, pet.getRarity().name());
            stmt.setInt(4, pet.getLevel());
            stmt.setDouble(5, pet.getXp());
            stmt.setInt(6, pet.getCandyUsed());
            stmt.setString(7, pet.getHeldItem());
            stmt.setBoolean(8, pet.isActive());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                plugin.debug("Inserted new pet: " + pet.getType() + " for profile " + pet.getProfileId());
            } else {
                plugin.log(Level.WARNING, "Failed to insert pet - no rows affected");
            }

        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to insert pet: " + e.getMessage());
        }
    }

    /**
     * Update an existing pet in the database.
     */
    private void updatePet(Pet pet) {
        String sql = """
            UPDATE pets
            SET pet_type = ?, rarity = ?, level = ?, xp = ?,
                candy_used = ?, held_item = ?, is_active = ?
            WHERE id = ?
            """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pet.getType().name());
            stmt.setString(2, pet.getRarity().name());
            stmt.setInt(3, pet.getLevel());
            stmt.setDouble(4, pet.getXp());
            stmt.setInt(5, pet.getCandyUsed());
            stmt.setString(6, pet.getHeldItem());
            stmt.setBoolean(7, pet.isActive());
            stmt.setInt(8, pet.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                plugin.debug("Updated pet " + pet.getId() + ": " + pet.getType());
            } else {
                plugin.log(Level.WARNING, "Failed to update pet " + pet.getId() + " - no rows affected");
            }

        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update pet " + pet.getId() + ": " + e.getMessage());
        }
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Delete a pet asynchronously.
     *
     * @param petId The pet ID to delete
     * @return CompletableFuture that completes when deletion is done
     */
    public CompletableFuture<Void> deletePetAsync(int petId) {
        return CompletableFuture.runAsync(() -> deletePetSync(petId));
    }

    /**
     * Delete a pet synchronously.
     *
     * @param petId The pet ID to delete
     */
    public void deletePetSync(int petId) {
        String sql = "DELETE FROM pets WHERE id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, petId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                plugin.debug("Deleted pet " + petId);
            } else {
                plugin.log(Level.WARNING, "Failed to delete pet " + petId + " - no rows affected");
            }

        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete pet " + petId + ": " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Deactivate all pets for a profile.
     * Used when summoning a new pet to ensure only one is active.
     *
     * @param profileId The profile ID
     */
    public void deactivateAllPets(int profileId) {
        String sql = "UPDATE pets SET is_active = 0 WHERE profile_id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profileId);
            stmt.executeUpdate();

            plugin.debug("Deactivated all pets for profile " + profileId);

        } catch (SQLException e) {
            plugin.log(Level.WARNING, "Failed to deactivate pets for profile " + profileId + ": " + e.getMessage());
        }
    }

    /**
     * Parse a pet from a ResultSet.
     *
     * @param rs The ResultSet positioned at a pet row
     * @return The parsed pet, or null if parsing failed
     */
    private Pet parsePet(ResultSet rs) {
        try {
            int id = rs.getInt("id");
            int profileId = rs.getInt("profile_id");
            String petTypeStr = rs.getString("pet_type");
            String rarityStr = rs.getString("rarity");
            int level = rs.getInt("level");
            double xp = rs.getDouble("xp");
            int candyUsed = rs.getInt("candy_used");
            String heldItem = rs.getString("held_item");
            boolean active = rs.getBoolean("is_active");

            // Parse enums
            PetType type;
            try {
                type = PetType.valueOf(petTypeStr);
            } catch (IllegalArgumentException e) {
                plugin.log(Level.WARNING, "Invalid pet type in database: " + petTypeStr);
                return null;
            }

            PetRarity rarity;
            try {
                rarity = PetRarity.valueOf(rarityStr);
            } catch (IllegalArgumentException e) {
                plugin.log(Level.WARNING, "Invalid pet rarity in database: " + rarityStr);
                return null;
            }

            return new Pet(id, profileId, type, rarity, level, xp, candyUsed, heldItem, active);

        } catch (SQLException e) {
            plugin.log(Level.WARNING, "Failed to parse pet from ResultSet: " + e.getMessage());
            return null;
        }
    }
}
