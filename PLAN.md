# 动物模组功能开发计划

> 当前进度：性别系统 + JADE 显示已完成。其余功能按此清单逐步实现。

## 已完成 ✅

- [x] 动物性别系统（50/50 随机分配，雄性体型 110%~120%，雌性 90%~100%）
- [x] JADE 显示（性别 / 怀孕 / 饥饿状态，通过 NBT 传输到客户端 tooltip）
- [x] 修复 JADE 运行时依赖：`compileOnly` + `localRuntime`，确保 dev 环境运行时 JADE 在 classpath 并被作为 mod 加载
- [x] 修复 JADE 插件注册失败（性别一直显示「未知」的真正原因）
- [x] 逃跑系统对创造 / 旁观模式玩家不生效（`MobFleeLogic.shouldIgnorePlayer`，硬编码，无配置项）
- [x] 融合 boids-1.21 鱼群算法到逃跑系统（Fabric → NeoForge 移植，`logic.boid` 包 + 逃跑转向叠加）
- [x] 钓鱼限制：仅自然水域（露天 + 河流/海洋/海滩群系 + 连通水面≥64）+ 必须有鱼实体
- [x] 鱼群消耗与投喂：钓上一条鱼减少一条实体；向水中投喂食物，0.5~1 MC 天后随机生成 2~5 条鱼（SavedData 持久化）
- [x] 真实钓鱼：钓上来的鱼是实体，拉到玩家脚边，亲手杀鱼才得到物品（参考 Fishing-Real-master）
- [x] 鱼饵通用识别：其他模组的种子无需数据包，`BlockItem` + `CropBlock`/`NetherWartBlock` 自动识别为鱼饵
- [x] 饥饿系统：玩家喂食一次即永久家养（`af_domesticated`）；家养动物随时间掉饥饿（`hungerMinutes` 默认 60 分钟从满到 0）；归零后进入挨饿状态，3 个 MC 天内持续掉血（`hungerStarveDays`，可喂食抢救）；野生动物不消耗饥饿
- [x] 阻止原版繁殖（喂食路径）：Mixin 注入 `Animal.mobInteract`，喂食不再调用 `setInLove`，改为恢复饥饿 + 消耗物品；`BreedGoal` 因无发情状态而永不触发
- [x] 食物判定：所有农作物（除毒马铃薯）+ 原版 `isFood` 标签；农作物通过 `CropBlock`/`NetherWartBlock`/`CocoaBlock`/`StemBlock`/`SweetBerryBushBlock`/`MushroomBlock`/`CaveVines*` + 显式物品表判定，天然兼容其它模组作物
- [x] 怀孕系统（自动交配，**仅家养**）：公母双方都需家养，且母体饥饿 ≥80% + 非产后冷却 → 8 格内每 5 秒 5% 概率怀孕；孕期按体型缩放（牛 120±20 分钟）；孕期饥饿 1.5 倍速；后期肚子渐变变大；受伤 20% 概率流产；生产/流产后冷却 120 分钟（按体型缩放）；野生动物不再自动繁殖
- [x] 马/驴/骡/骆驼走怀孕机制（`AbstractHorseFeedMixin` 拦截 `fedFood`：喂食即家养 + 恢复饥饿 + 保留原版回血/驯服度，且不再触发原版 `setInLove`）
- [x] 生产：胎生调用原版 `getBreedOffspring` 生成幼年实体（保留羊毛颜色等）；卵生（鸡）掉落带 NBT 标记的受精蛋
- [x] 卵生（鸡）：**只有雌性鸡会下蛋**；每只母鸡产蛋间隔为 **1 MC 天 ± 随机 1~3 小时**（21000~27000 tick，即约 0.875~1.125 天，`af_next_egg_daytime` 记录下一次可产蛋时间）；产蛋时若附近有合格的公鸡（家养 + 饥饿≥80% + 成年 + 非冷却）→ 下**受精蛋**，否则下**普通鸡蛋**；受精蛋放在干草块上不见天孵 1 MC 天出小鸡；普通蛋/受精蛋扔出都不孵化

> 注：JADE 显示已接入 性别 / 家养 / 怀孕进度 / 饥饿，全部通过 NBT 传输到客户端 tooltip

## 已解决的坑（务必记住）

### JADE 插件注册抛 `Duplicate config key` → 服务端数据提供者静默丢失

- 现象：tooltip 能显示条目（因为组件提供者已回放成功），但服务端 NBT 永远拿不到，值恒为「未知」。
- 根因：`registerClient` 里手动调了 `registration.addConfig(UID, true)`，而 Jade 在
  `ClientRegistrationSession.end()` 回放 `registerEntityComponent` 时，会通过
  `WailaClientRegistration.tryAddConfig(provider)` **自动**为组件提供者建好同名配置条目。
  两处相加 → `PluginConfig.addConfig` 的 `Preconditions` 抛 `IllegalArgumentException`。
- 致命后果：`client.endSession()` 抛异常后，`CommonProxy.loadComplete` 里紧随其后的
  **`common.endSession()` 永远不会执行**，服务端的 `entityDataProviders` 因此从未回放注册，
  provider 不在客户端请求列表中，服务端 `appendServerData` 永不执行。
- 修复：**不要手动 `addConfig`**，Jade 会自动处理（默认值取 `IToggleableProvider.enabledByDefault()`）。
- 排查要点：Jade 只打一行 `Error loading plugin at <class>` + 堆栈，游戏照常运行，非常隐蔽。
  日志里搜 `Error loading plugin` 是排查 Jade 集成问题的第一动作。

## 待实现 🚧

### 怀孕系统 ✅
- [x] 阻止原版繁殖：喂食路径用 Mixin 拦截 `Animal.mobInteract` 的 `setInLove`；繁殖改为「公母靠近自动概率怀孕」（不依赖 BreedGoal）
- [x] 孕期计算：牛为基准 120 分钟 ± 20 分钟浮动，其它生物按实体尺寸（宽×高）开方缩放
- [x] 生产：胎生直接生成幼年实体；卵生（鸡）生成受精蛋
- [x] 流产：怀孕期间受到任何伤害有概率流产（默认 20%）
- [x] 产后冷却：生产/流产后长时间无法再次怀孕（时长按体型缩放）
- [x] 怀孕肚子变大：怀孕后期（进度 ≥40%）把 SCALE 按进度逐渐调大，最多 +15%

### 卵生（鸡专属）✅
- [x] 受精蛋：复用原版鸡蛋 `minecraft:egg` + `CustomData` 组件标记 `af_fertilized=true`
- [x] 孵化条件：受精蛋必须**放在干草块（`hay_block`）上**且**该位置不能见天空**（`!level.canSeeSky`），累积 **1 MC 天**（默认 20 分钟，`eggIncubationMinutes`）后孵出小鸡；离开干草块或重见天空立即重置进度（`EggIncubationHandler`）
- [x] 右键干草块顶面可直接把受精蛋放上去（`EggItemUseMixin`）；鸡在干草块鸡舍里直接下蛋也会落上去
- [x] 扔出的鸡蛋不再孵化：普通蛋、受精蛋扔出去都不出小鸡（`ThrownEggHatchMixin`，普通蛋受 `blockNormalEggHatching` 控制）
- [x] 鸡蛋 tooltip：受精蛋提示「放在不见天日的干草块上约 1 天孵化」；普通蛋提示「无法孵化」
- [x] 孵蛋期间蛋物品实体设 `setUnlimitedLifetime()`（不消失、不合并）
- [x] 只有雌性下蛋：`ChickenEggLayMixin` 拦截 `spawnAtLocation(Items.EGG)`，雄性鸡直接重置产蛋计时（不再下蛋）
- [x] 产蛋频率限制：每只母鸡产蛋间隔 = 1 MC 天 ± 随机 1~3 小时（用 `af_next_egg_daytime` 存下一次可产蛋的 `getDayTime` 目标值，避免固定节奏）
- [x] 受精判定：产蛋时检查附近是否有合格公鸡（家养+饥饿≥80%+成年+非冷却），有则下受精蛋，否则下普通蛋

### 饥饿系统 ✅
- [x] 家养判定：玩家喂过一次即永久家养（`af_domesticated=true`）
- [x] 家养动物饥饿计时：随时间掉饥饿值，饿到 0 后持续掉血（可抢救）
- [x] 野生动物不消耗饥饿，不饿死

### 配置项 ✅
- [x] 牛基准孕期（分钟）cowPregnancyMinutes = 120
- [x] 孕期浮动（分钟）pregnancyFloatMinutes = 20
- [x] 受伤流产概率 miscarriageChance = 0.2
- [x] 产后冷却基准（分钟）cooldownMinutes = 120
- [x] 家养动物饥饿时长（分钟）hungerMinutes = 60
- [x] 卵生动物列表 oviparousMobs = ["minecraft:chicken"]
- [x] 普通鸡蛋是否禁止孵化 blockNormalEggHatching = true
- [x] 受精蛋孵化时长（分钟）eggIncubationMinutes = 20（=1 MC 天）
- [x] 饥饿总开关 hungerEnabled = true
- [x] 饥饿归零宽限天数 hungerStarveDays = 3

### 待确认 / 后续
- [x] 怀孕触发限定「家养」：母体与父体都必须 `af_domesticated`（玩家喂过），野生动物不再自动繁殖
- [x] 马/驴/骡/骆驼走怀孕机制：喂食走 `AbstractHorseFeedMixin`，可家养、有饥饿、能怀孕；原版繁殖被阻止
- 备注：马/驴仍可跨种配对——`isCompatibleMate` 允许「马×驴」「驴×马」双向配对，生出的后代由原版 `getBreedOffspring` 决定（→ 骡子）；父本若在出生时已卸载，则按怀孕时记录的父本类型重建一个临时实例来判定，保证仍生骡子；骡子在本模组中也可生育（→ 骡子）