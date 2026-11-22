# Bosses' Rise Respawner

Rematch the Bosses' Rise bosses. Each boss has a sigil; use it at that boss's home
structure to summon it again. Each structure allows one rematch per day.

Unofficial add-on for Bosses' Rise (`block_factorys_bosses`), which is required.
NeoForge 1.21.1, Java 21, NeoForge 21.1.265.

## Sigils

- bossesrise_respawner:sirok_sigil — Sandworm Nest, nest center.
- bossesrise_respawner:skor_sigil — Yeti hideout, arena center.
- bossesrise_respawner:helvar_sigil — Underworld arena, past the door.
- bossesrise_respawner:ashlord_sigil — Dragon Tower, above the player.
- bossesrise_respawner:nerakyss_sigil — Kraken Ship, nearby deep water.

## Crafting

Every sigil is a diamond surrounded by four corner and four side items:

| Sigil | Corners | Sides |
|---|---|---|
| Sirok's Sigil | Chiseled Sandstone | Fermented Spider Eye |
| Skor's Sigil | Blue Ice | Snow Block |
| Helvar's Sigil | Crying Obsidian | Soul Sand |
| Ashlord's Sigil | Dragon Bone | Blaze Powder |
| Nerakyss's Sigil | Kraken Tooth | Ink Sac |

A recipe unlocks in the recipe book when the player kills that boss or picks up
its signature drop.

## Behaviour

Each sigil stacks to 16 and glows. A successful summon consumes one (except in creative).
Wrong location, an existing living boss, missing water, failed entity spawning,
or a cooldown refuses without consumption. Cooldowns last 24,000 game ticks per
boss, dimension and structure start, persisted in overworld SavedData. Time is
measured by game time, so sleeping does not skip the wait.

## Development

Open this folder as a Gradle project in IntelliJ. Use JDK 21 for the project SDK
and Gradle JVM, then reload Gradle. ModDevGradle generates client and server run
configurations.

- Build: ./gradlew build
- Jar: build/libs/bossesrise_respawner-0.1.0.jar
- Launch development client: ./gradlew runClient

Development copies of Bosses' Rise 2.1.2 and GeckoLib 4.9.3 go in libs/. They are
ignored by Git and are not included in the output jar. On a new machine, add
compatible NeoForge 1.21.1 versions of both jars to libs/ before launching.

## Extending

- BossSigils.java: definition list and registration.
- BossSigilItem.java: validation, feedback and placement.
- SigilCooldowns.java: persistent cooldown storage.
- assets/bossesrise_respawner: item models and English names.
- data/bossesrise_respawner: recipes and recipe-book unlock advancements.

Add future systems as separate classes, registering mod-bus listeners in
BossesRiseRespawner and gameplay listeners on NeoForge.EVENT_BUS when needed.

## Gameplay acceptance checks

In a disposable development world, test all five structures, wrong-piece
refusals, live-boss detection, creative use, item consumption, independent
structure cooldowns, cooldown persistence across restart and simultaneous
multiplayer use. Check Helvar beyond the arena door, Nerakyss near water and
client models/tooltips. Structure-piece matching needs validation against future
Bosses' Rise updates.
