package com.skyblock.gui.menus;

import com.skyblock.SkyblockPlugin;
import com.skyblock.gui.AbstractGUI;
import com.skyblock.gui.utils.ItemBuilder;
import com.skyblock.pets.Pet;
import com.skyblock.pets.PetManager;
import com.skyblock.pets.PetType;
import com.skyblock.utils.ColorUtils;
import com.skyblock.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pet menu GUI showing all player pets.
 * Allows summoning/unsummoning pets and viewing detailed information.
 */
public class PetMenu extends AbstractGUI {

    private final int page;
    private static final int PETS_PER_PAGE = 45; // 5 rows of 9

    public PetMenu(SkyblockPlugin plugin) {
        this(plugin, 0);
    }

    public PetMenu(SkyblockPlugin plugin, int page) {
        super(plugin, "&8Your Pets", 6);
        this.page = page;
    }

    @Override
    protected void build(Player player) {
        fillBorder(createFiller());

        PetManager petManager = plugin.getPetManager();
        List<Pet> allPets = petManager.getPets(player.getUniqueId());
        Pet activePet = petManager.getActivePet(player.getUniqueId());

        // Sort pets by rarity (highest first), then by level (highest first)
        allPets.sort(Comparator
                .comparing((Pet p) -> p.getRarity().ordinal())
                .reversed()
                .thenComparing((Pet p) -> p.getLevel())
                .reversed()
        );

        // Calculate pagination
        int totalPages = (int) Math.ceil(allPets.size() / (double) PETS_PER_PAGE);
        int startIndex = page * PETS_PER_PAGE;
        int endIndex = Math.min(startIndex + PETS_PER_PAGE, allPets.size());

        // Display pets for current page
        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            Pet pet = allPets.get(i);

            // Skip border slots
            if ((slot % 9 == 0) || (slot % 9 == 8)) {
                slot++;
            }
            if (slot >= 45) break;

            boolean isActive = activePet != null && activePet.getId() == pet.getId();
            setItem(slot, createPetItem(pet, isActive), event -> {
                if (isActive) {
                    // Unsummon pet
                    petManager.unsummonPet(player.getUniqueId());
                    player.sendMessage(ColorUtils.colorize("&cUnsummoned " + pet.getRarity().getColor() +
                            pet.getType().getDisplayName()));
                } else {
                    // Summon pet
                    petManager.summonPet(player.getUniqueId(), pet);
                    player.sendMessage(ColorUtils.colorize("&aSummoned " + pet.getRarity().getColor() +
                            pet.getType().getDisplayName()));
                }
                // Refresh menu
                new PetMenu(plugin, page).open(player);
            });

            slot++;
        }

        // Info item
        setItem(49, new ItemBuilder(Material.BOOK)
                .name("&6&lPet Information")
                .lore(
                        "",
                        "&7Total Pets: &e" + allPets.size(),
                        "&7Active Pet: " + (activePet != null ?
                                activePet.getRarity().getColor() + activePet.getType().getDisplayName() :
                                "&cNone"),
                        "",
                        "&7Pets gain XP when you train",
                        "&7skills that match their type.",
                        "",
                        "&7Higher rarity pets provide",
                        "&7stronger stat bonuses!",
                        "",
                        "&eClick a pet to summon/unsummon!"
                )
                .build());

        // Previous page button
        if (page > 0) {
            setItem(45, new ItemBuilder(Material.ARROW)
                    .name("&aPrevious Page")
                    .lore("&7Page " + page + "/" + totalPages)
                    .build(), event -> {
                new PetMenu(plugin, page - 1).open(player);
            });
        }

        // Next page button
        if (page < totalPages - 1) {
            setItem(53, new ItemBuilder(Material.ARROW)
                    .name("&aNext Page")
                    .lore("&7Page " + (page + 2) + "/" + totalPages)
                    .build(), event -> {
                new PetMenu(plugin, page + 1).open(player);
            });
        }

        // Back button
        setItem(48, createBackButton(), event -> {
            plugin.getGuiManager().openGUI(player, new SkyblockMenu(plugin));
        });

        // Close button
        setItem(50, createCloseButton(), event -> {
            player.closeInventory();
        });
    }

    /**
     * Create an ItemStack representing a pet.
     */
    private org.bukkit.inventory.ItemStack createPetItem(Pet pet, boolean isActive) {
        PetType type = pet.getType();
        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.add(pet.getRarity().getColor() + "&l" + pet.getRarity().getDisplayName());
        lore.add("");

        // Level and XP
        int maxLevel = type.getMaxLevel();
        lore.add("&7Level: &e" + pet.getLevel() + "&7/&a" + maxLevel);

        if (pet.getLevel() < maxLevel) {
            double currentLevelXp = com.skyblock.pets.PetXPCalculator.getTotalXPForLevel(pet.getLevel(), pet.getRarity());
            double nextLevelXp = com.skyblock.pets.PetXPCalculator.getTotalXPForLevel(pet.getLevel() + 1, pet.getRarity());
            double progress = ((pet.getXp() - currentLevelXp) / (nextLevelXp - currentLevelXp)) * 100;

            lore.add("");
            lore.add("&7Progress to Level " + (pet.getLevel() + 1) + ":");
            lore.add(createProgressBar(progress));
            lore.add("&7 " + NumberUtils.formatAbbreviated(pet.getXp() - currentLevelXp) + "&7/&a" +
                    NumberUtils.formatAbbreviated(nextLevelXp - currentLevelXp) +
                    " &8(" + String.format("%.1f", progress) + "%)");
        } else {
            lore.add("");
            lore.add("&6&lMAX LEVEL!");
        }

        lore.add("");
        lore.add("&7Total XP: &b" + NumberUtils.formatAbbreviated(pet.getXp()));

        // Held item
        if (pet.getHeldItem() != null) {
            lore.add("");
            lore.add("&7Held Item: &6" + pet.getHeldItem());
        }

        // Candy used
        if (pet.getCandyUsed() > 0) {
            lore.add("");
            lore.add("&7Candy Used: &d" + pet.getCandyUsed());
        }

        // Status
        lore.add("");
        if (isActive) {
            lore.add("&a&l✓ ACTIVE");
            lore.add("");
            lore.add("&eClick to unsummon!");
        } else {
            lore.add("&c✗ Inactive");
            lore.add("");
            lore.add("&eClick to summon!");
        }

        ItemBuilder builder = new ItemBuilder(type.getIcon())
                .name(pet.getRarity().getColor() + type.getDisplayName())
                .lore(lore)
                .hideFlags();

        if (isActive) {
            builder.glow();
        }

        return builder.build();
    }

    /**
     * Create a progress bar for XP visualization.
     */
    private String createProgressBar(double percentage) {
        int length = 20;
        int filled = (int) Math.round(percentage / 100 * length);

        StringBuilder bar = new StringBuilder("&a");
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("-");
            } else {
                bar.append("&7-");
            }
        }

        return ColorUtils.colorize(bar.toString());
    }
}
