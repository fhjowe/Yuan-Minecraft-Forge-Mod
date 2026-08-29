# Yuan

A Minecraft Forge 1.20.1 weapons and armor mod.

## Features

- Yuan God Sword：cosmic 星空 / 丝绸 / 隧道 / 晶格（Voronoi）多套武器渲染，Compose 配置界面（G 键），逐剑 NBT 保存
- 神剑时停：右键触发，冻结实体/方块/流体/粒子/声音/天气等，玩家保持可动，局域网同步
- 空间斩：左键命中敌人播放 3D 黑白斩击特效
- YUAN 工具与防具套件

## Building

```powershell
# Java 17 toolchain is required (ForgeGradle 6 fails on newer JDKs when reobfuscating)
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat :forge:build --console=plain
```

Build output: `forge/build/libs/yuan-forge-1.0.0-all.jar` (the `-all` jar bundles
the MixinExtras runtime via JarJar and is the deployable artifact).

## License

MIT - see [LICENSE](LICENSE).

## Third-party content

This mod reuses a few third-party textures, shaders and sounds (ArcaneVortex
cosmic assets, megatimestop sounds, Silk WebGL shader, vanilla Minecraft
shaders). Those items are NOT covered by the MIT license — see
[THIRD_PARTY_NOTICES](THIRD_PARTY_NOTICES.md).
