# Island System Guide

The Island System is the core feature of SkyBlock. Every player gets their own private island where they can build, farm, and progress through the game.

## Overview

In SkyBlock, "everything revolves around islands." Your private island is your home base where you:
- Build structures
- Farm resources
- Store items
- Invite friends (co-op)
- Receive visitors

## Creating Your Island

### First Time Setup
When you first join, use `/island` or `/is` to create your island. You'll be teleported to a floating island with:
- Small starting platform (dirt/grass)
- One tree
- One chest with starter items

### Island Size
- **Default Size**: 160x160 blocks
- **Maximum Size**: 240x240 blocks (after upgrades)

## Island Commands

| Command | Description |
|---------|-------------|
| `/island` | Teleport to your island |
| `/is` | Alias for /island |
| `/island create` | Create island (if deleted) |
| `/island sethome` | Set spawn point |
| `/island reset` | Reset island (DESTRUCTIVE!) |
| `/island settings` | Open settings GUI |
| `/island invite <player>` | Invite to co-op |
| `/island kick <player>` | Start kick vote |
| `/island ban <player>` | Ban from island |
| `/island unban <player>` | Unban player |
| `/island visitors` | View visit stats |
| `/island public` | Toggle public status |
| `/island pvp` | Toggle PvP |
| `/island help` | Show all commands |

## Island Settings

Access via `/island settings` or Shift+Click the Island button in the SkyBlock menu.

### Public/Private Toggle
- **Private** (default): Only invited players and co-op members can visit
- **Public**: Anyone can visit your island using `/visit`

### PvP Toggle
- **Disabled** (default): Players cannot fight on your island
- **Enabled**: PvP is allowed

### Mob Spawning
- **Enabled** (default): Hostile mobs spawn naturally
- **Disabled**: No hostile mob spawning

### Animal Spawning
- **Enabled** (default): Animals spawn naturally
- **Disabled**: No animal spawning

### Visitor Container Access
- **Denied** (default): Visitors cannot open chests/containers
- **Allowed**: Visitors can access containers

## Island Protection

Your island is protected from griefing:

### Non-Members Cannot:
- Break blocks
- Place blocks
- Open containers (unless enabled)
- Interact with certain blocks
- Attack you (if PvP disabled)

### Members Can:
- Full building permissions
- Container access
- All interactions

## Co-op System

Share your island with friends!

### Inviting Players
```
/coopadd <player>
or
/island invite <player>
```

The invited player has 5 minutes to accept with `/is accept`.

### Member Limits
- Maximum 5 co-op members (configurable)
- Owner + 4 additional members

### Kick Voting
To remove a member:
```
/coopkick <player>
```

This starts a vote. Other members can vote, and a majority is required.

### Leaving Co-op
```
/coopleave
```

Members can leave at any time. The owner cannot leave - they must salvage the co-op.

### Salvaging Co-op
Only the owner can salvage:
```
/coopsalvage
```

This removes all members and converts back to a solo island.

## Visitor System

### Visiting Others
```
/visit <player>
```

Requirements:
- Target island must be public, OR
- You must be invited/member

### Being Visited
When players visit your island:
- You earn Social Skill XP (cooldown applies)
- Visit is recorded in your statistics
- Visitors can see your builds

### Banning Visitors
```
/island ban <player>
/island unban <player>
```

Banned players cannot visit your island.

## Island Travel

### To Your Island
- `/island` or `/is`
- Click "Your Island" in SkyBlock menu

### To Hub
- `/hub`
- Click hub portal (if configured)

### To Others
- `/visit <player>`

## Tips for New Players

1. **Don't fall!** The void below will kill you
2. **Expand carefully** - cobblestone generators help
3. **Plant trees** for wood
4. **Set your spawn** with `/island sethome`
5. **Make it public** once you're proud of your build
6. **Invite friends** to progress faster

## Technical Details

### World Management
Each island exists in its own world (or region), providing:
- Isolation from other players
- Better performance
- Easier backups

### Data Storage
Island data is stored in MySQL/MariaDB:
- `islands` table: Core island data
- `island_members`: Co-op members
- `island_settings`: Configuration
- `island_visitors`: Visit history
- `island_bans`: Banned players

### Caching
Island data is cached using Caffeine for fast access:
- 1000 islands cached
- 30-minute expiration
- Automatic refresh on access

## Troubleshooting

### Can't Create Island
- Check if you already have one
- Verify database connection
- Check console for errors

### Can't Invite Players
- Ensure you're the owner
- Check member limit
- Verify player is online

### Reset Not Working
- Must Shift+Click to confirm
- Check if island exists

### Visitors Can't Visit
- Ensure island is public
- Or send an invite first
