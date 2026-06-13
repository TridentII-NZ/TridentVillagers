# TridentVillagers v2.0 Rewrite Design

**Date:** 2026-06-13

---

## Goal

Strip the plugin back to two stable features — auto-restock and bribe system — removing everything that touches villager AI passively. Nothing in the plugin should interfere with vanilla villager breeding or pathfinding.

---

## What's Removed

| Item | Reason |
|------|--------|
| Villager leads | Out of scope for v2 |
| `BedHighlightService` | Out of scope for v2 |
| Sneak branch in `onVillagerInteract` | No longer needed |
| `leashVillager()` method | Out of scope for v2 |
| `onVillagerFoodPickup` handler | Removed in hotfix; never re-added |
| `features.bed-highlight` config key | Out of scope |
| `features.always-willing` config key | Out of scope |
| `features.villager-leads` config key | Out of scope |
| `bedHighlight()` / `alwaysWilling()` / `villagerLeads()` accessors | Out of scope |

---

## Feature 1: Auto-Restock

**Behaviour:** A repeating sync task fires every `restock.interval-ticks` (default 1200) and calls `restock()` on every loaded villager in every world. On trade, restock also fires immediately if `features.always-restock` is true.

**`restock()` does:**
- `villager.setRestocksToday(0)` + `villager.restock()`
- If `restock.reset-uses: true` → set all recipe uses to 0
- If `restock.reset-demand: true` → set all recipe demand to 0

**Config keys kept:** `features.always-restock`, `restock.interval-ticks`, `restock.reset-uses`, `restock.reset-demand`

---

## Feature 2: Bribe System

**Behaviour:** Right-clicking a villager opens a 27-slot chest GUI. Slot 12 = trade button (opens vanilla merchant screen after restocking). Slot 16 = bribe button (opens bribe input GUI). Bribe GUI accepts configured items; confirming deducts the item and applies a cure-style reputation + price discount to that player's trades with that villager.

**Config keys kept:** All `menus.*`, `bribes.*`

---

## Architecture

### Files Modified

| File | Change |
|------|--------|
| `VillagerMenuListener.java` | Remove sneak branch, lead handler, `leashVillager()` method; revert to 5-arg constructor |
| `TridentVillagers.java` | Remove `BedHighlightService` instantiation and import; revert to 5-arg `VillagerMenuListener` call |
| `PluginConfig.java` | Remove `bedHighlight`, `alwaysWilling`, `villagerLeads` fields + accessors + reload lines |
| `config.yml` | Remove `features.bed-highlight`, `features.always-willing`, `features.villager-leads` keys and associated messages |

### Files Deleted

| File | Reason |
|------|--------|
| `villager/BedHighlightService.java` | Feature removed |

### Files Unchanged

`RestockService.java`, `VillagerTradeService.java`, `BribeStorage.java`, `VillagerMenuService.java`, `VillagerMenuHolder.java`, `TridentVillagersCommand.java`, `MessageService.java`, `TextFormatter.java`, `Gradient.java`, `ChatColour.java`, `plugin.yml`

---

## Config After v2

```yaml
prefix: "..."

messages:
  startup / shutdown / reload-success / no-permission / player-only
  unknown-command / villager-missing / bribe-accepted / bribe-rejected
  # removed: villager-leashed, bed-highlighted, bed-no-bed

features:
  always-restock: true
  # removed: villager-leads, bed-highlight, always-willing

restock:
  interval-ticks: 1200
  reset-uses: true
  reset-demand: true

menus: (unchanged)

bribes: (unchanged)
```

---

## Version

`gradle.properties`: `2.0` (drop the `-B` suffix for a clean v2 release label)

devlog entry: `## Update 2.0`

---

## Constraints

- Paper API 1.21.11 only. No deprecated Bukkit methods.
- No passive listeners that fire on entity AI ticks or item pickup.
- Plugin only acts on explicit player interaction (right-click villager, inventory click).
