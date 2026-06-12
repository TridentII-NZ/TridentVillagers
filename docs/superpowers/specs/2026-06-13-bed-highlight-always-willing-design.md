# TridentVillagers — Bed Highlight & Always Willing Design

**Date:** 2026-06-13
**Features:** Shift+right-click bed highlight, food always triggers willingness

---

## Overview

Two independent additions to TridentVillagers:

1. **Bed Highlight** — shift+right-clicking a villager spawns `HAPPY_VILLAGER` particles around its assigned bed for ~5 seconds, visible only to the clicking player.
2. **Always Willing** — when a villager picks up breeding food (bread, carrot, potato, beetroot), it is immediately set willing, bypassing the vanilla willingness check. Vanilla population cap and bed requirements still apply.

---

## Architecture

### New File: `villager/BedHighlightService.java`

- Constructor: `(JavaPlugin plugin, PluginConfig config, MessageService messages)`
- Method: `highlight(Player player, Villager villager)`
  - Reads `villager.getMemory(MemoryKey.HOME)` to get bed location
  - If null: sends `messages.bed-no-bed` to player, returns
  - If present: schedules a `BukkitRunnable` repeating every 4 ticks, 25 iterations (~5s)
    - Each tick: `player.spawnParticle(Particle.HAPPY_VILLAGER, bedCenter, 3, 0.3, 0.3, 0.3, 0)`
    - Auto-cancels after 25 iterations
  - Sends `messages.bed-highlighted` to player on start
  - Feature-gated by `config.bedHighlight()`

### Modified: `villager/VillagerMenuListener.java`

**Constructor** — add `BedHighlightService bedHighlight` parameter.

**`onVillagerInteract`** — add sneak branch before the lead check:
```
if (player.isSneaking()) {
    event.setCancelled(true);
    bedHighlight.highlight(player, villager);
    return;
}
```

**New `@EventHandler onVillagerFoodPickup(EntityPickupItemEvent)`:**
- Guard: `config.alwaysWilling()` is true
- Guard: entity is `Villager`
- Guard: item material is one of `BREAD`, `CARROT`, `POTATO`, `BEETROOT`
- Action: `villager.setWilling(true)`

### Modified: `config/PluginConfig.java`

Two new boolean fields and accessors:
- `bedHighlight` — read from `features.bed-highlight`
- `alwaysWilling` — read from `features.always-willing`

### Modified: `TridentVillagers.java`

- Instantiate `BedHighlightService` in `loadServices()`
- Pass to `VillagerMenuListener` constructor

---

## Config Changes (`config.yml`)

```yaml
features:
  bed-highlight: true
  always-willing: true

messages:
  bed-highlighted: "%prefix% &fVillager's bed is highlighted."
  bed-no-bed: "%prefix% &cThis villager has no assigned bed."
```

---

## Constraints

- Particles are player-local (`player.spawnParticle`) — other players do not see them.
- Bed location via `MemoryKey.HOME` — Paper API only, no deprecated Bukkit methods.
- Breeding: only willingness is forced. Population cap and bed count remain vanilla.
- All new messages are configurable. No hardcoded strings.
- Both features individually togglable via config.

---

## Out of Scope

- Cancelling an in-progress highlight (e.g. if player moves away) — auto-expires after 5s.
- Visual bed-distance indicator or waypoint — particles only.
- Bypassing population cap or bed count for breeding.
