HuskClaims supports toggling Operation Groups within a claim to determine whether the plugin should prevent certain operations from occurring in a claim.

## Toggling Operation Groups
Operation Groups can be configured in the plugin [[config]] and managed by any user who has the `MANAGE_OPERATION_GROUPS` [[trust]] privilege in a claim. Effectively, Operation Groups provide a way of letting players fine tune the flag settings of your claim. 

By default, HuskClaims provides the operation groups below. Each one has a toggle command, `/<command> [on|off]`, and a `%huskclaims_group_<id>%` placeholder that says whether it is on in the claim the player is standing in (`true`, `false`, or `no_claim`), which is what a settings menu needs.

| Operation Group | Toggle Command | Placeholder | Allows | Default |
|---|---|---|---|:-:|
| `Claim Explosions` | `/claimexplosions` | `%huskclaims_group_explosions%` | Explosion block damage, from blocks and mobs (`explosion_damage_terrain, monster_damage_terrain`) | ❌ |
| `Claim PvP` | `/claimpvp` | `%huskclaims_group_pvp%` | Players fighting each other (`player_damage_player`) | ✅ |
| `Monster Spawning` | `/claimmonsters` | `%huskclaims_group_monsters%` | Hostile mobs spawning naturally (`monster_spawn`) | ✅ |
| `Animal Spawning` | `/claimanimals` | `%huskclaims_group_animals%` | Passive mobs spawning naturally (`passive_mob_spawn`) | ✅ |
| `Fire Spread` | `/claimfire` | `%huskclaims_group_fire%` | Fire spreading and burning blocks (`fire_spread, fire_burn`) | ❌ |
| `Ender Pearls` | `/claimpearls` | `%huskclaims_group_pearls%` | Ender pearl teleports into the claim (`ender_pearl_teleport`) | ❌ |
| `Raids` | `/claimraids` | `%huskclaims_group_raids%` | Raids starting inside the claim (`start_raid`) | ❌ |
| `Spawn Eggs` | `/claimeggs` | `%huskclaims_group_spawn_eggs%` | Spawn eggs being used in the claim (`use_spawn_egg`) | ❌ |

"Default" follows the `default_flags` list in the config. These groups are only written to `config.yml` when it is created; an existing config keeps the groups it already has, so copy the entries below into `operation_groups` to get the new ones.

## Claim flags menu
`/claimflags` opens a menu for the claim the player is standing in, with one item per operation group that shows whether it is on. Clicking an item runs the group's own toggle command, so the trust privilege check and the messages are the same as typing the command. Standing outside a claim shows a "no claim" item instead.

The menu is `plugins/HuskClaims/claim_flags_menu.yml`, written on first start and reloaded with `/huskclaims reload`. Every entry in `flags` is one operation group, by its `id` from `operation_groups`: add an entry to show a group of your own, remove one to hide it, and change its slot, materials, names and lore freely. `enabled` is the item shown while the group is on in the claim, `disabled` the item shown while it is off. The title, rows, filler material and the info, no-claim and close items are set the same way. Names and lore take MiniMessage and `&` codes, and `%group_name%` and `%group_description%` in them are filled in from the group.

A DeluxeMenus version that uses the toggle commands and placeholders is in [`examples/claimflags-menu.yml`](https://github.com/cozy-survival-setup-forks/HuskClaims/blob/master/examples/claimflags-menu.yml).

## Wilderness Redstone Restrictions
HuskClaims includes a `redstone_actuate` operation type that controls whether redstone mechanisms (pistons, dispensers, etc.) can function. This provides similar functionality to GriefPrevention's redstone restrictions and is **enabled by default** in wilderness areas.

This means:
- **In Claims**: Redstone works normally (controlled by existing `redstone_interact` permissions)  
- **Outside Claims**: Redstone mechanisms work by default (pistons, dispensers, etc. function normally)

Server administrators can disable redstone in wilderness by using `/claimflags set redstone_actuate false` while standing outside any claim, or by modifying the `wilderness_rules` in the config file.

## Customizing Operation Groups
Operation groups can be customised in the plugin config as follows:

<details>
<summary>Operation Groups (config.yml)</summary>

```yaml
# Groups of operations that can be toggled on/off in claims
operation_groups:
- id: explosions
  name: Claim Explosions
  description: Toggle whether explosions can damage terrain in claims
  toggle_command_aliases:
  - claimexplosions
  allowed_operations:
  - explosion_damage_terrain
  - monster_damage_terrain
- id: pvp
  name: Claim PvP
  description: Toggle whether players can fight each other in claims
  toggle_command_aliases:
  - claimpvp
  allowed_operations:
  - player_damage_player
- id: monsters
  name: Monster Spawning
  description: Toggle whether hostile mobs can spawn naturally in claims
  toggle_command_aliases:
  - claimmonsters
  allowed_operations:
  - monster_spawn
- id: animals
  name: Animal Spawning
  description: Toggle whether passive mobs can spawn naturally in claims
  toggle_command_aliases:
  - claimanimals
  allowed_operations:
  - passive_mob_spawn
- id: fire
  name: Fire Spread
  description: Toggle whether fire can spread and burn blocks in claims
  toggle_command_aliases:
  - claimfire
  allowed_operations:
  - fire_spread
  - fire_burn
- id: pearls
  name: Ender Pearls
  description: Toggle whether ender pearls can teleport players into claims
  toggle_command_aliases:
  - claimpearls
  allowed_operations:
  - ender_pearl_teleport
- id: raids
  name: Raids
  description: Toggle whether raids can start inside claims
  toggle_command_aliases:
  - claimraids
  allowed_operations:
  - start_raid
- id: spawn_eggs
  name: Spawn Eggs
  description: Toggle whether spawn eggs can be used in claims
  toggle_command_aliases:
  - claimeggs
  allowed_operations:
  - use_spawn_egg
```
</details>

Whether an Operation Group is the default in a claim depends on whether the `allowed_operations` of the group are also present in the `default_flags` list in the config.

## Fine-Grained Flag Management
> **Note:** `/claimflags set` changes single operation types and needs the matching `huskclaims.flag.<type>` permission, which only operators have by default.

The `/claimflags` command allows you to fine-tune the allowed operation group settings within a claim. This is a powerful command, and can cause users confusion if they accidentally mess with the wrong flags. We therefore recommend creating operation group commands for end-user needs, and restricting this command for moderator actions.

`/claimflags` opens the flags menu below. `/claimflags set` requires the user to have the `MANAGE_OPERATION_GROUPS` privilege in the claim they are stood in, or the `huskclaims.command.claimflags.other` bypass permission.

Use `/claimflags set [operation_type] <true/false>` to change one operation type.

### Adjusting the flags outside of claims
You can also can adjust the value of flags outside of claims (the "Wilderness") by using `/claimflags set` while standing outside a claim. This requires the `huskclaims.command.claimflags.world` permission.