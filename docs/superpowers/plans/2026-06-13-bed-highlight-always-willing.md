# Bed Highlight & Always Willing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add shift+right-click bed highlighting with HAPPY_VILLAGER particles and force villager willingness whenever breeding food is picked up.

**Architecture:** `BedHighlightService` owns the per-player particle task lifecycle; `VillagerMenuListener` gains a sneak branch delegating to that service and a new `EntityPickupItemEvent` handler that sets willing on food pickup. Both features are individually toggled in config.

**Tech Stack:** Paper API 1.21.11, Java 21, Gradle

---

## File Map

| Action | File |
|--------|------|
| Modify | `src/main/resources/config.yml` |
| Modify | `src/main/java/TridentII/config/PluginConfig.java` |
| Create | `src/main/java/TridentII/villager/BedHighlightService.java` |
| Modify | `src/main/java/TridentII/TridentVillagers.java` |
| Modify | `src/main/java/TridentII/villager/VillagerMenuListener.java` |
| Modify | `gradle.properties` |
| Modify | `devlog.md` |

---

## Task 1: Add config keys and messages

**Files:**
- Modify: `src/main/resources/config.yml`
- Modify: `src/main/java/TridentII/config/PluginConfig.java`

- [ ] **Step 1: Add feature flags and messages to config.yml**

In `config.yml`, under the existing `features:` block add the two new flags:

```yaml
features:
  always-restock: true
  villager-leads: true
  bribes: true
  bed-highlight: true
  always-willing: true
```

Under the existing `messages:` block add the two new messages (after `villager-missing`):

```yaml
  bed-highlighted: "%prefix% &fVillager's bed is highlighted."
  bed-no-bed: "%prefix% &cThis villager has no assigned bed."
```

- [ ] **Step 2: Add fields and accessors to PluginConfig**

Add two private fields near the other boolean fields:

```java
private boolean bedHighlight;
private boolean alwaysWilling;
```

In the `reload()` method, after the existing `bribesEnabled` line, add:

```java
bedHighlight = config.getBoolean("features.bed-highlight");
alwaysWilling = config.getBoolean("features.always-willing");
```

Add two accessor methods after the existing `bribesEnabled()` method:

```java
public boolean bedHighlight() {
    return bedHighlight;
}

public boolean alwaysWilling() {
    return alwaysWilling;
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/config.yml src/main/java/TridentII/config/PluginConfig.java
git commit -m "feat: add bed-highlight and always-willing config keys"
```

---

## Task 2: Create BedHighlightService

**Files:**
- Create: `src/main/java/TridentII/villager/BedHighlightService.java`

- [ ] **Step 1: Create the file**

```java
package TridentII.villager;

import TridentII.config.PluginConfig;
import TridentII.message.MessageService;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class BedHighlightService {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final MessageService messages;

    public BedHighlightService(JavaPlugin plugin, PluginConfig config, MessageService messages) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
    }

    public void highlight(Player player, Villager villager) {
        if (!config.bedHighlight()) {
            return;
        }

        Location bed = villager.getMemory(MemoryKey.HOME);
        if (bed == null) {
            messages.send(player, "messages.bed-no-bed");
            return;
        }

        messages.send(player, "messages.bed-highlighted");
        Location center = bed.clone().add(0.5, 0.5, 0.5);

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count++ >= 25) {
                    cancel();
                    return;
                }
                player.spawnParticle(Particle.HAPPY_VILLAGER, center, 5, 0.4, 0.3, 0.4, 0);
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }
}
```

- [ ] **Step 2: Verify it compiles**

```bash
cd "D:/Development/Trident Dev/Java/Minecraft/TridentVillagers"
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL` with no errors.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/TridentII/villager/BedHighlightService.java
git commit -m "feat: add BedHighlightService for per-player bed particle highlight"
```

---

## Task 3: Wire BedHighlightService into TridentVillagers and VillagerMenuListener

**Files:**
- Modify: `src/main/java/TridentII/TridentVillagers.java`
- Modify: `src/main/java/TridentII/villager/VillagerMenuListener.java`

- [ ] **Step 1: Instantiate BedHighlightService in TridentVillagers**

Add the import at the top of `TridentVillagers.java`:

```java
import TridentII.villager.BedHighlightService;
```

In `loadServices()`, after the `messageService` line, add:

```java
BedHighlightService bedHighlightService = new BedHighlightService(this, pluginConfig, messageService);
```

Update the `VillagerMenuListener` constructor call (currently the last line of `loadServices()`) to pass `bedHighlightService`:

```java
menuListener = new VillagerMenuListener(this, pluginConfig, messageService, menuService, tradeService, bedHighlightService);
```

- [ ] **Step 2: Update VillagerMenuListener constructor**

Add the import at the top of `VillagerMenuListener.java`:

```java
import TridentII.villager.BedHighlightService;
```

Add a new field after the existing `trades` field:

```java
private final BedHighlightService bedHighlight;
```

Update the constructor signature and body to accept and assign the new field:

```java
public VillagerMenuListener(
    JavaPlugin plugin,
    PluginConfig config,
    MessageService messages,
    VillagerMenuService menus,
    VillagerTradeService trades,
    BedHighlightService bedHighlight
) {
    this.plugin = plugin;
    this.config = config;
    this.messages = messages;
    this.menus = menus;
    this.trades = trades;
    this.bedHighlight = bedHighlight;
}
```

- [ ] **Step 3: Verify it compiles**

```bash
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL` with no errors.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/TridentII/TridentVillagers.java src/main/java/TridentII/villager/VillagerMenuListener.java
git commit -m "feat: wire BedHighlightService into plugin and listener"
```

---

## Task 4: Add sneak branch to onVillagerInteract

**Files:**
- Modify: `src/main/java/TridentII/villager/VillagerMenuListener.java`

- [ ] **Step 1: Add the sneak check**

In `onVillagerInteract`, after the `Player player = event.getPlayer();` line and BEFORE the `trades.applyStoredCures` call, insert:

```java
if (player.isSneaking()) {
    event.setCancelled(true);
    bedHighlight.highlight(player, villager);
    return;
}
```

The full method should now read:

```java
@EventHandler
public void onVillagerInteract(PlayerInteractEntityEvent event) {
    if (event.getHand() != EquipmentSlot.HAND || !(event.getRightClicked() instanceof Villager villager)) {
        return;
    }

    Player player = event.getPlayer();

    if (player.isSneaking()) {
        event.setCancelled(true);
        bedHighlight.highlight(player, villager);
        return;
    }

    trades.applyStoredCures(player, villager);
    if (config.villagerLeads() && player.getInventory().getItemInMainHand().getType() == Material.LEAD) {
        leashVillager(event, player, villager);
        return;
    }

    event.setCancelled(true);
    player.openInventory(menus.createTradeMenu(villager));
}
```

- [ ] **Step 2: Verify it compiles**

```bash
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL` with no errors.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/TridentII/villager/VillagerMenuListener.java
git commit -m "feat: add shift+right-click bed highlight to villager interact"
```

---

## Task 5: Add always-willing food pickup handler

**Files:**
- Modify: `src/main/java/TridentII/villager/VillagerMenuListener.java`

- [ ] **Step 1: Add the import**

At the top of `VillagerMenuListener.java`, add:

```java
import org.bukkit.event.entity.EntityPickupItemEvent;
```

- [ ] **Step 2: Add the event handler**

Add this method after the existing `onPlayerTrade` handler:

```java
@EventHandler
public void onVillagerFoodPickup(EntityPickupItemEvent event) {
    if (!config.alwaysWilling() || !(event.getEntity() instanceof Villager villager)) {
        return;
    }

    Material type = event.getItem().getItemStack().getType();
    if (type == Material.BREAD || type == Material.CARROT
            || type == Material.POTATO || type == Material.BEETROOT) {
        villager.setWilling(true);
    }
}
```

- [ ] **Step 3: Verify it compiles**

```bash
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL` with no errors.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/TridentII/villager/VillagerMenuListener.java
git commit -m "feat: force villager willing on breeding food pickup"
```

---

## Task 6: Bump version, update devlog, build

**Files:**
- Modify: `gradle.properties`
- Modify: `devlog.md`

- [ ] **Step 1: Bump version in gradle.properties**

Change `version=1.5-B` to:

```properties
version=1.6-B
```

- [ ] **Step 2: Add devlog entry**

Prepend a new entry at the top of `devlog.md` (above the existing `## Update 1.5-B` block):

```markdown
## Update 1.6-B

- Added shift+right-click on a villager to highlight its assigned bed with HAPPY_VILLAGER particles for ~5 seconds, visible only to the clicking player. Sends a configurable message if the villager has no bed assigned.
- Added always-willing mode: when a villager picks up bread, carrot, potato, or beetroot it is immediately set willing, bypassing the vanilla willingness check.
- Both features are individually togglable via `features.bed-highlight` and `features.always-willing` in config.yml.

```

- [ ] **Step 3: Build the JAR**

```bash
./gradlew jar
```

Expected: `BUILD SUCCESSFUL`. JAR output at `build/libs/TridentVillagers-1.6-B.jar`.

- [ ] **Step 4: Stage to test server**

Copy the built JAR to `D:\Minecraft\My Plugins\TridentVillagers-1.6-B.jar`.

- [ ] **Step 5: In-game test — bed highlight**

1. Place a villager with an assigned bed in a test world.
2. Shift+right-click the villager.
3. Confirm HAPPY_VILLAGER particles appear around the bed block and a "bed highlighted" message is received.
4. Right-click a villager with no bed assigned — confirm the "no bed" message appears.
5. Normal right-click (no shift) — confirm trade menu still opens as before.

- [ ] **Step 6: In-game test — always willing**

1. Throw bread onto a group of villagers.
2. Confirm villagers pick it up and enter love mode / begin breeding.
3. Repeat in a fully populated village — confirm breeding still triggers on food despite population.

- [ ] **Step 7: Commit and push**

```bash
git add gradle.properties devlog.md
git commit -m "chore: bump to 1.6-B, update devlog"
git pull --rebase
git push
```
