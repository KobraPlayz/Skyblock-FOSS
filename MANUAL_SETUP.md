# SkyblockFOSS - Setup Manual

## Prerequisites

- Java 17 or higher
- Minecraft Server running Spigot/Paper 1.20.4
- MySQL 8.0+ (recommended for production) or SQLite (development)
- Maven 3.6+ (for building)

## Building the Plugin

1. Clone the repository:
```bash
git clone https://github.com/your-repo/Skyblock-FOSS.git
cd Skyblock-FOSS
```

2. Build with Maven:
```bash
mvn clean package
```

3. The compiled JAR will be in `target/SkyblockFOSS-1.0.0-SNAPSHOT.jar`

## Installation

1. Copy the JAR file to your server's `plugins/` directory

2. Start the server once to generate default configuration files

3. Stop the server and configure the plugin (see Configuration section)

4. Start the server again

## Configuration

### Database Setup

#### MySQL (Recommended for Production)

1. Create a MySQL database:
```sql
CREATE DATABASE skyblock;
CREATE USER 'skyblock'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON skyblock.* TO 'skyblock'@'localhost';
FLUSH PRIVILEGES;
```

2. Edit `plugins/SkyblockFOSS/config.yml`:
```yaml
database:
  type: mysql
  mysql:
    host: localhost
    port: 3306
    database: skyblock
    username: skyblock
    password: your_secure_password
    useSSL: false
    poolSize: 10
```

#### SQLite (Development)

No additional setup required. Default configuration uses SQLite:
```yaml
database:
  type: sqlite
  sqlite:
    filename: data.db
```

### Cache Settings

Adjust for your server's player count:
```yaml
cache:
  player-data-size: 500        # Maximum cached players
  player-data-expire: 30       # Minutes before expiration
```

For servers with 200+ concurrent players, consider:
```yaml
cache:
  player-data-size: 1000
  player-data-expire: 60
```

### Module Configuration

Enable/disable features in `modules.yml`:
```yaml
modules:
  skills:
    enabled: true
  collections:
    enabled: true
  profiles:
    enabled: true
  economy:
    enabled: true
```

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/skyblock` | `skyblock.menu` | Open the main SkyBlock menu |
| `/skills` | `skyblock.skills` | View your skills |
| `/collections` | `skyblock.collections` | View your collections |
| `/profile` | `skyblock.profile` | Manage profiles |
| `/coins` | `skyblock.coins` | Check coin balance |
| `/shop` | `skyblock.shop` | Open the shop |
| `/sbadmin` | `skyblock.admin` | Admin commands |

### Admin Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/sbadmin reload` | `skyblock.admin.reload` | Reload configuration |
| `/sbadmin coins <player> <give/take/set> <amount>` | `skyblock.admin.economy` | Manage player coins |
| `/sbadmin skill <player> <skill> <level>` | `skyblock.admin.players` | Set skill level |
| `/sbadmin collection <player> <collection> <amount>` | `skyblock.admin.players` | Set collection amount |
| `/sbadmin give <player> <item> [amount]` | `skyblock.admin.items` | Give custom items |

## Permissions

### Player Permissions
- `skyblock.menu` - Access SkyBlock menu
- `skyblock.skills` - View skills
- `skyblock.collections` - View collections
- `skyblock.profile` - Manage profiles
- `skyblock.coins` - Check coin balance
- `skyblock.shop` - Access shop

### Admin Permissions
- `skyblock.admin` - Access admin menu
- `skyblock.admin.reload` - Reload configuration
- `skyblock.admin.economy` - Manage economy
- `skyblock.admin.players` - Manage players
- `skyblock.admin.items` - Give items
- `skyblock.admin.*` - All admin permissions

## Optional Dependencies

### Vault (Economy Integration)
If you want to integrate with other economy plugins:
1. Install Vault on your server
2. The plugin will automatically detect and use Vault

### PlaceholderAPI
For placeholder support in other plugins:
1. Install PlaceholderAPI
2. The plugin automatically registers placeholders

Available placeholders:
- `%skyblock_coins%` - Player's coin balance
- `%skyblock_skill_<skill>%` - Skill level
- `%skyblock_skill_<skill>_xp%` - Skill XP
- `%skyblock_collection_<collection>%` - Collection amount
- `%skyblock_profile%` - Active profile name

## Troubleshooting

### Database Connection Issues
- Verify MySQL is running and accessible
- Check username/password in config.yml
- Ensure the database exists
- Check firewall settings

### Plugin Not Loading
- Verify Java 17+ is installed
- Check server is running Spigot/Paper 1.20.4
- Review console for error messages

### Performance Issues
- Increase database pool size for high player counts
- Enable query logging to identify slow queries
- Consider MySQL over SQLite for production

## Support

For issues and feature requests, please open an issue on GitHub.
