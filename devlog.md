# Devlog

## Update 1.4-B

- Fixed bribe discounts not showing in villager trades by applying saved cure counts directly to merchant recipe pricing.
- Reordered trade opening so restocking happens before saved cure discounts are applied.
- Added configurable cure-style price discount controls.

## Update 1.3-B

- Added an internal text formatter for MiniMessage tags, gradients, hex colors, and legacy `&` chat color codes.
- Updated configurable chat messages and GUI titles/items/lore to render through the same color formatter.
- Reworked bribes to save per-player cure counts on each villager instead of applying temporary recipe special prices.
- Bribes now reapply cure-style villager reputation when the player interacts with the villager or opens trades.

## Update 1.2-B

- Refactored Java sources into purpose-specific packages: command, config, menu, message, and villager.
- Cached hot-path feature and restock settings in the plugin config wrapper so event/task code avoids repeated YAML lookups.
- Removed an unused menu service plugin dependency and tightened command tab completion locale handling.

## Update 1.1-B

- Added configurable startup, shutdown, reload, bribe, lead, and villager-missing messages with configurable prefix support.
- Added `/tridentvillagers reload` with tab completion and permissions.
- Added villager interaction menu with configurable trade and bribe buttons.
- Added bribe menu with configurable accepted items, reputation gain, and trade discount behavior.
- Added villager lead support using the modern Paper/Bukkit entity API.
- Added recurring loaded-villager restocking with configurable interval and recipe use/demand resets.
