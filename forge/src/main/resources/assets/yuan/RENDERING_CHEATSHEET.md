# Yuan Mod 渲染速查卡 — 1.20.1 Forge

> 每次做渲染特效前扫一眼这张卡。完整文档见 `RENDERING_REFERENCE.md`

---

## 极速启动：5 种最常用的炫酷效果

| 效果 | 一行方案 | 完整方案 |
|---|---|---|
| 物品流光 | `RenderType.energySwirl(tex, tick*0.01f, tick*0.005f)` | 见 REF §9.4 |
| 盔甲发光 | `RenderType.energySwirl(...)` 在 ArmorLayer 中叠加 | 见 REF §10.4 |
| 实体轮廓 | `entity.setGlowingTag(true)` | 见 REF §12.3 |
| 世界刀光 | `RenderLevelStageEvent.AFTER_TRANSLUCENT_BLOCKS` + GL_QUADS | 已有代码在 `YuanSwordHandheldOverlay` |
| 粒子特效 | `level.addParticle(type, x,y,z, vx,vy,vz)` | 见 REF §13 |

---

## 关键 API 一句话

```java
// === Mod Bus（初始化注册，@Mod.EventBusSubscriber(bus = Bus.MOD)） ===
EntityRenderersEvent.RegisterRenderers        // 注册实体渲染器
EntityRenderersEvent.RegisterLayerDefinitions  // 注册模型层
RegisterParticleProvidersEvent                 // 注册粒子工厂
RegisterNamedRenderTypesEvent                  // 注册命名渲染类型
RegisterShadersEvent                           // 注册自定义着色器
ColorHandlerEvent.Block / ColorHandlerEvent.Item // 注册颜色处理器

// === Forge Bus（游戏运行时，@Mod.EventBusSubscriber(bus = Bus.FORGE)） ===
RenderLevelStageEvent  // Stage.AFTER_TRANSLUCENT_BLOCKS → 世界特效
RenderHandEvent        // 第一人称手持渲染
RenderLivingEvent.Pre/Post  // 每个实体渲染前后
DrawSelectionEvent     // 方块/实体选择框

// 物品自定义渲染
BlockEntityWithoutLevelRenderer  // BEWLR 基类
IClientItemExtensions.builder().setBEWLR(ctx -> INSTANCE).build();

// 自定义 RenderType
RenderType.energySwirl(tex, uSpeed, vSpeed)   // 流光
RenderType.glint(tex, uSpeed, vSpeed)          // 附魔光效
RenderType.create(name, format, bufSize, state) // 完全自定义

// 混合模式
RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);           // 加性(770,1)
RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // 标准(770,771)

// 粒子
level.addParticle(type, x,y,z, vx,vy,vz);
DeferredRegister<ParticleType<?>>  // 注册
RegisterParticleProvidersEvent     // 工厂

// 游戏时间
int tick = (int) level.getGameTime();
float gameTime = tick / 24000.0f;  // 0~1 循环
float pulse = (float) Math.sin(tick * 0.1f);  // 正弦波
```

---

## 着色器文件位置

```
assets/yuan/shaders/core/    ← 你的自定义着色器
assets/minecraft/shaders/core/ ← vanilla 可被覆盖（资源包方式）
assets/minecraft/shaders/include/ ← 包含着色器（9个）
```

## 最常用的包含着色器

```glsl
#moj_import <minecraft:dynamictransforms.glsl>  // ModelViewMat, ModelOffset, TextureMat
#moj_import <minecraft:globals.glsl>             // GameTime, GlintAlpha, ScreenSize
#moj_import <minecraft:fog.glsl>                 // apply_fog(), Fog uniform
#moj_import <minecraft:light.glsl>               // minecraft_mix_light()
```

## 关键 Uniform（1.20.1 直接声明，不用 block）

```glsl
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec4 ColorModulator;
uniform vec3 ModelOffset;
uniform mat4 TextureMat;      // ← 流光动画核心
uniform float GameTime;       // ← 0~1 循环
uniform float GlintAlpha;     // ← 附魔强度
uniform vec2 ScreenSize;
```

---

## 核心着色器 #define 指令速查

| 指令 | 作用 | 常用值 |
|---|---|---|
| `ALPHA_CUTOUT` | 透明度裁剪阈值 | 0.5 (cutout), 0.1 (translucent) |
| `EMISSIVE` | 禁用光照（自发光） | 实体定义中设置 |
| `APPLY_TEXTURE_MATRIX` | UV 受 TextureMat 变换 | 能量漩涡需要 |
| `NO_CARDINAL_LIGHTING` | 禁用朝向光照差异 | |
| `DISSOLVE` | 启用溶解效果（alpha 遮罩） | 末影龙死亡 |

---

## 后处理管线速查

```
assets/minecraft/shaders/post/*.json  ← 后处理管线配置
assets/minecraft/shaders/post/*.fsh   ← 后处理片段着色器
```

**内置渲染目标**：main, translucent, item_entity, particles, weather, clouds, entity_outline

**SamplerInfo uniform**：`OutSize`(vec2), `<Name>Size`(vec2)

---

## 坑点（踩过一次就记住）

1. **solid 忽略 alpha** → 要用 cutout
2. **UV2 是 ivec2** → 亮度坐标，不是归一化的
3. **Position 在实体上是观察空间** → 需要 ModelOffset 偏移
4. **#moj_import 不能嵌套**
5. **GameTime 精度只有 1/24000** → 高精度用 Java 端算好传 uniform
6. **BEWLR 单例** → 每个 mod 只一个
7. **能量漩涡需要可平铺纹理** → 否则 UV 接缝处会跳
8. **addParticle 只在客户端调用** → 服务端调了也不报错但没粒子
9. **RenderType.create 需要提前注册 ShaderProgram** → 在 RegisterShadersEvent 中做
10. **solid 渲染类型 + alpha 纹理 = 透明部分也渲染** → 必须用 cutout
11. **transparency 后处理管线失败 = 游戏崩溃** → 覆盖时必须完全兼容
12. **GUI 物品预渲染** → 动画纹理/附魔光效/数据改变时自动重绘
13. **blit_screen 着色器预加载** → 覆盖会导致崩溃

---

## 颜色格式速查（本项目用 ARGB int）

```java
int color = 0xAARRGGBB;
int a = (color >> 24) & 0xFF;
int r = (color >> 16) & 0xFF;
int g = (color >> 8) & 0xFF;
int b = color & 0xFF;

// OpenGL 用 RGBA byte
GL11.glColor4ub((byte)r, (byte)g, (byte)b, (byte)a);

// lerp 两个颜色
int lerpColor(int c1, int c2, float t) {
    int a1=(c1>>24)&0xff, r1=(c1>>16)&0xff, g1=(c1>>8)&0xff, b1=c1&0xff;
    int a2=(c2>>24)&0xff, r2=(c2>>16)&0xff, g2=(c2>>8)&0xff, b2=c2&0xff;
    return ((int)(a1+(a2-a1)*t)<<24)|((int)(r1+(r2-r1)*t)<<16)
         | ((int)(g1+(g2-g1)*t)<<8)|(int)(b1+(b2-b1)*t);
}
```

---

## 渲染类型 → 何时使用

| 想要的效果 | 用这个 RenderType |
|---|---|
| 物品基础层 | `entity_cutout_no_cull` |
| 物品半透明 | `entity_translucent` |
| 盔甲 | `armor_cutout_no_cull` |
| 盔甲纹饰(decal) | `armor_decal_cutout_no_cull` |
| 流光覆盖 | `energy_swirl`（Java 创建）或自定义 RenderType |
| 附魔光效 | `glint` / `armor_entity_glint` |
| 发光/火焰 | `entity_translucent_emissive` 模式或自定义 |
| 眼睛 | `eyes`（加性混合） |
| 信标光柱 | `beacon_beam` |
| 闪电 | `lightning` |
| 实体轮廓 | `outline` |

---

## 盔甲模型速查

```
LayerDefinition.create(MeshDefinition, texWidth, texHeight)
├─ root.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(x,y).addBox(...), PartPose)
├─ root.addOrReplaceChild("body", ...)
├─ root.addOrReplaceChild("right_arm", ..., PartPose.offset(-5, 2, 0))
├─ root.addOrReplaceChild("left_arm", ..., PartPose.offset(5, 2, 0))
├─ root.addOrReplaceChild("right_leg", ..., PartPose.offset(-1.9, 12, 0))
└─ root.addOrReplaceChild("left_leg", ..., PartPose.offset(1.9, 12, 0))

纹理路径: assets/yuan/textures/models/armor/<name>_layer_<1|2>.png
```

---

## 实体渲染层顺序

```
EntityRenderer.render()
├─ model.setupAnim()      ← 设置骨骼动画
├─ model.renderToBuffer() ← 渲染模型主体
├─ RenderLayer[0]         ← 发光层
├─ RenderLayer[1]         ← 自定义层
├─ ArmorLayer (inner)     ← 盔甲内层
├─ ArmorLayer (outer)     ← 盔甲外层
├─ ArmorOverlayLayer      ← 附魔光效
└─ ElytraLayer            ← 鞘翅
```

---

## VertexConsumer 速查

```java
// 获取顶点消费者
VertexConsumer c = buffer.getBuffer(renderType);

// 发射顶点（ENTITY 格式：position + color + uv + overlay + light + normal）
c.vertex(matrix, x, y, z).color(r,g,b,a).uv(u,v)
  .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
  .normal(normalMatrix, nx, ny, nz).endVertex();

// 简单格式（POSITION_COLOR_TEX）
c.vertex(matrix, x, y, z).color(r,g,b,a).uv(u,v).endVertex();
```

---

## PoseStack 速查

```java
push/pop → 保存/恢复矩阵
translate(x,y,z) → 平移（1.0 = 一个方块）
mulPose(Axis.YP.rotationDegrees(deg)) → 旋转
scale(x,y,z) → 缩放
last().pose() → 获取 4x4 矩阵（给 VertexConsumer）
last().normal() → 获取 3x3 法线矩阵
```

---

## 混合模式速查

| 效果 | blendFunc |
|---|---|
| 不透明 | 禁用混合 |
| 加性发光 | `SRC_ALPHA, ONE` (770, 1) |
| 标准半透明 | `SRC_ALPHA, ONE_MINUS_SRC_ALPHA` (770, 771) |
| 附魔光效 | `SRC_COLOR, ONE` |

---

> 完整文档 → `RENDERING_REFERENCE.md` | 项目 → `Yuan/forge/src/main/java/com/yuan/`
