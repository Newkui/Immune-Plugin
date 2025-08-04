# MyImmunePlugin

A Minecraft plugin that gives players temporary immunity (customizable effects & visuals) after respawn.  
Supports multiple effects like Invisibility, Resistance, Speed, Regeneration, plus glowing and particle indicators.

---

## Features

- Custom immunity duration
- Multiple potion effects (toggleable via `config.yml`)
- Particle and glowing visuals during immunity
- Multi-world support (all worlds or whitelist specific)
- Configurable start/end messages

---

## Commands

`/immunecheck` — View immunity duration and enabled effects

---

## Installation

1. Place the plugin `.jar` file into your server's `plugins/` folder.
2. Start the server to generate `config.yml`.
3. Edit `config.yml` to your preference and reload/restart the server.

---

## Configuration Example

```yaml
immunity-seconds: 15
effects:
  invisibility: true
  resistance: true
  speed: false
  regeneration: false
visual:
  glowing: true
  particles: true
  particle-type: ENCHANTMENT_TABLE
messages:
  start: "&aYou are immune for %time% seconds!"
  end: "&cYour immunity has ended!"
settings:
  all-worlds: true
  worlds:
    - world
    - world_nether
