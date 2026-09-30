# RLCombat Hits

为 Minecraft 近战攻击添加自定义**命中**与**暴击**音效的 Forge 模组。

| 项目 | 信息 |
|------|------|
| 适用版本 | Minecraft 1.20.1 / Forge 47.4.10 |
| Mod ID | `rlcombat_hits` |
| 版本 | 1.0.0-1.20.1 |
| 许可 | LGPL |

## 功能特性

- 攻击**命中**与**暴击**时播放自定义音效（`swordslash` / `criticalstrike`）
- 服务端权威判定，通过网络包下发到客户端，避免双端重复播放
- 支持**主手**（左键）与**副手**（右键，兼容副手攻击类模组）
- 武器判定三策略（优先级从高到低）：
  1. 物品标签（可配置，适配任意第三方模组武器）
  2. 继承关系（`SwordItem` / `TridentItem` / `DiggerItem`）
  3. 攻击伤害属性兜底（覆盖完全自定义的近战武器）
- 正/负双缓存：武器与非武器都缓存，避免重复判断，性能开销极低

## 使用

将构建出的 jar 放入游戏 `mods` 文件夹即可，无需其他前置模组。

## 配置

首次启动后会在 `config/` 下生成 `rlcombat_hits-common.toml`，可通过 `weaponTags` 数组配置用于武器判定的物品标签，从而适配任意第三方模组的武器（无需对方适配本模组）。

默认标签：

```toml
weaponTags = [
    "minecraft:swords", "minecraft:axes", "minecraft:pickaxes",
    "minecraft:shovels", "minecraft:hoes",
    "forge:tools/swords", "forge:tools/axes", "forge:tools/pickaxes",
    "forge:tools/shovels", "forge:tools/hoes"
]
```

## 指令

需要 OP 权限（等级 2）：

| 指令 | 作用 |
|------|------|
| `/rlcombat_hits list` | 查看已缓存的武器列表及数量统计 |
| `/rlcombat_hits clear` | 清空命中缓存 |

## 构建

要求 **JDK 17**：

```bash
gradlew build
```

产物位于 `build/libs/`。

## 本地测试第三方模组

将第三方模组 jar 放入项目根目录的 `./libs`，在 `build.gradle` 中通过 `flatDir` + `fg.deobf` 声明依赖即可在开发环境（`runClient`）一起加载测试。依赖 mixin 的模组已通过 `mixin.env.remapRefMap` 正确处理 refmap 重映射。

## 架构概览

```
com.animator70.rlcombat_hits
├── RLCombatHits           入口：注册音效 / 网络 / 配置
├── ModSounds              音效注册
├── client/SoundEffects    客户端音效播放
├── combat/CombatEvents    事件监听（服务端权威判定）
├── combat/CombatJudge     纯逻辑判断（标签 → 继承 → 属性）
├── network/Network        网络通道与分发
├── network/CombatFeedbackPacket  服务端→客户端反馈包
├── config/RLCombatConfig  配置（内部类隔离）
└── command/ModCommands   指令注册与实现
```

## 许可

本项目使用 [LGPL](LICENSE.txt) 许可。
