<div align="center">

# ⚔️ Yuan · 原

**Minecraft Forge 1.20.1** 武器 / 防具 / 渲染模组
神器之刃的宇宙星空渲染、世界时停、空间斩与时间回溯

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-FF9900)](https://www.minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.4.20-00A7E1)](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html)
[![Java](https://img.shields.io/badge/Java-17-5382A1)]()
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)
[![Platform](https://img.shields.io/badge/平台-Windows-lightgrey)]()
[![Version](https://img.shields.io/badge/Mod-1.0.0-blue)]()

</div>

Yuan 是一个面向 Minecraft **Forge 1.20.1** 的武器/防具模组,核心是一把「原·神器之刃」——
它拥有 ArcaneVortex(秘法涡流)风格的**宇宙星空渲染**、长按既停的**世界时停**、
命中即斩的 **3D 空间斩**,以及可回放的**时间回溯**。所有效果都通过 **G 键专属配置界面**
(Compose 绘制)逐剑保存在 NBT 中,完全自定义。

> 📌 **基本停更**:本项目目前已基本停止更新(维护模式)——不再添加大型新功能,仅处理严重缺陷与兼容性问题。Issues / PR 依然欢迎。

---

## ✨ 特性

### ⚔️ 原·神器之刃 `yuan:yuan_god_sword`

- **多套渲染风格**:宇宙星空(cosmic)/ 丝绸(silk)/ 原版 / 关闭,逐剑 NBT 保存,随时切换
- **星空渲染**:ArcaneVortex(秘法涡流)风格 cosmic 星云与星点,基于掩膜(mask)的动态几何烘焙;
  **Oculus / Iris 光影下同样正常显示**(延迟渲染管线)
- **专属配置界面**:主手持剑按 **G** 打开,三页导航「时停 / 武器渲染 / 斩击」+ 子页 Tab,
  含真实贴图预览、搜索过滤、预设槽与逐页恢复默认

### 🗡️ 原剑 `yuan:yuan_sword`

- 高额伤害的近战剑,老派的"一刀流"手感
- **Tooltip(悬停信息)样式参考秘法涡流(ArcaneVortex)的「狐狸剑」**:动态狐狸纹背景 +
  彩虹光晕环 + 液态玻璃质感的文字层(悬停时可看到)
- 老式交互:Shift+右击触发时停;G 打开传统配置界面;G+Ctrl 一键全预设、G+Shift 循环切换预设

### ⏸️ 世界时停

- **触发**:右键(可改为 Shift+右键 或 自定义键 R),进入时停世界;玩家自身保持可动且无敌
- **完整冻结链**:实体 / 方块 / 流体 / 粒子 / 声音 / 天气 / 世界时间 / Boss AI / 光照,全部停住
- **局域网同步**:时停状态、冻结配置与施法者位置广播给其他玩家
- **高度自定义**(全部在 G 界面配置):
  - 画面:黑白 / 复古 / 暗角 三种滤镜、灰度强度、动画
  - 光球:颜色(8 预设 + 自定义 RGB)、大小、数量、透明度、旋转
  - 音效:开关、音量、循环
  - 冻结:范围(0–128 格或全场)、实体 / 方块 / 流体 / Boss AI 分类开关、是否冻结自己
  - 时长:手动或 1–60 秒自动结束;冷却 0–100 tick;触发方式与弹提示
- **与 mega时停 等其它时停模组共存**(MixinExtras 兼容)

### ⚡ 空间斩

- 神剑**左键命中**目标时,在命中点生成一道 **3D 黑白斩击**(黑芯 + 白边发光),约 0.26s 淡出
- 刀光跟随目标、相机面朝向、命中点精确;Oculus/Iris 光影下走实体渲染同样可见
- 完整参数面板:黑芯 / 白边 / 发光颜色(RGB)、长度、宽度、厚度、扫开与淡出动画、深浅测试等

### ⏪ 时间回溯

- **触发**:背包中持有神剑时按 **H**(播放中再按 H 取消)
- **动画式回放**:方块逐个倒放、实体平滑回退、世界时间回滚,而非瞬间瞬移
- **健壮性**:防物品/容器复制、跨维度换算(下界/主世界、传送门对端)、距离半径过滤、
  死亡回溯避熔岩/虚空等危险点
- 记录窗口 1–600 秒可配置,播放秒数可调

### 🗡️ 装备套件

| 物品 | ID | 说明 |
|---|---|---|
| 原·神器之刃 | `yuan:yuan_god_sword` | 星空渲染 + 时停 + 空间斩 + 时间回溯 |
| 原剑 | `yuan:yuan_sword` | 高额伤害;狐狸剑风格 Tooltip;老式 Shift+右击时停、G+Ctrl/G+Shift 预设 |
| 原·初始之刃 | `yuan:yuan_origin_blade` | 原银金剑造型 |
| 斧 / 镐 / 弓 | `yuan:yuan_axe` / `yuan:yuan_pickaxe` / `yuan:yuan_bow` | YUAN 工具与远程武器 |
| 防具四件 | `yuan:yuan_helmet` … `yuan:yuan_boots` | YUAN 材料装甲 |

---

## 🚀 安装

1. 准备好 **Forge 47.4.20**(Forge 1.20.1,要求 Java 17)
2. 下载 `yuan-forge-1.0.0-all.jar`(**请用 `-all` 包**:内含 MixinExtras 运行时,JarJar 打包)
3. 放入游戏实例的 `mods/` 目录后启动即可

> 可选装 Oculus / Embeddium 以获得光影;本模组对两者已做适配。

## ⌨️ 按键

| 按键 | 功能 |
|---|---|
| **G** | 神剑:专属配置界面;原剑:传统配置界面 |
| **右键** | 神剑:触发时停(可配置为 Shift+右键 或 自定义键) |
| **R** | 时停自定义触发键(在 G 界面中选择「自定义键」后生效) |
| **H** | 背包持神剑时:触发时间回溯;播放中再按:取消 |
| **Shift + 右键** | 原剑:触发时停(老式系统) |

所有按键可在 游戏内「选项 → 控制 → Yuan」中改绑。

---

## 🛠️ 构建

```powershell
# 必须使用 JDK 17(ForgeGradle 6 在更新 JDK 上 reobf 会失败)
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat :forge:build --console=plain
```

构建产物:`forge/build/libs/yuan-forge-1.0.0-all.jar`(部署用 `-all` 包)。

---

## 📦 目录结构

```
forge/src/main/java/com/yuan/
├── client/          # 客户端:cosmic 渲染、Compose 配置界面、粒子、渲染器
├── command/         # 命令
├── common/          # 通用组件(DataComponent)
├── event/           # 事件:武器事件、掉落保护、防御状态
├── item/            # 物品:神剑、工具、防具、NBT 配置
├── mixin/           # Mixin 注入(时停冻结链、cosmic 接管、回溯等)
├── network/         # 网络包
├── registry/        # 物品注册
├── space_slash/     # 空间斩特效(shader + 渲染 + 实体)
├── timerewind/      # 时间回溯(记录器/还原器/回放)
└── timestop/        # 世界时停(状态/着色器/粒子/渲染)
```

---

## 💬 兼容性与已知限制

- **必要**:Forge `[47,48)`、Java 17、单人 / 局域网
- **可选**:Iris / Oculus 光影、Embeddium
- 与 mega时停 等其它插件可共存
- 已知限制:时间回溯的**动画回放**在局域网内其它玩家视角暂不可见(进行中);液体贴图动画在时停中暂未冻结

---

## 📖 文档

- 开发交接 / 完整变更记录:`docs/YUANMOD_HANDOFF_CURRENT.md`
- 渲染与回溯专项记录:`docs/YUAN_COSMIC_RENDER_HANDOFF_20260808.md`
- 研究文档:`docs/research/`

---

## 📜 许可与致谢

- 代码:**MIT**,见 [LICENSE](LICENSE)
- 第三方内容(ArcaneVortex cosmic 贴图/shader、megatimestop 音效、Silk shader、原版 Minecraft shader)**不适用 MIT** —— 详见 [THIRD_PARTY_NOTICES](THIRD_PARTY_NOTICES.md)
- 特别致谢:
  - **ArcaneVortex(秘法涡流)** —— 哔哩哔哩 UP主「洛谔谔」(UID 3546888156481679):cosmic 渲染的贴图与 shader 来源;**原剑的 Tooltip 悬停样式参考其武器「狐狸剑」**
  - **超级时停(megatimestop)** —— 时停实现参考
  - **Silk WebGL shader** —— 丝绸渲染移植来源

---

<div align="center">⭐ 如果喜欢,欢迎 Star —— 也欢迎在 Issues 提出建议</div>