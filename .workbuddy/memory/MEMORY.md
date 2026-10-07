# 项目长期记忆（牧业模组 / Pasture Tales）

## 跨模组约定：DNF 凯莉猪（dnfenhance）

- DNF 模组（`D:\Games\MOD\DNF\DNF-MDK-1.21.1-NeoGradle-main`）召唤的凯莉猪带标签 `dnfenhance_kai_li`。
- 在牧业模组中通过 `mob.getTags().contains("dnfenhance_kai_li")` 识别（纯字符串判断，无编译期依赖）。
- 凯莉猪需要：**不逃跑**、**固定雌性**。已分别在 `MobFleeLogic`（shouldFlee/processFlee/tickFlee 三处拦截）和 `GenderLogic.initGender`（强制 female）实现。
- 这些处理只记录在记忆里，**不要写进任何手册/文档/README**。

## 逃跑系统：受惊扩散（panic spread）

- 需求原文：逃跑的动物让附近 10 格的动物也逃跑。
- 定稿设计（2026-10-07）：**单波扩散**——只有玩家直接触发的动物在「开始逃跑」瞬间向外扩散一次，被波及者不再传播（`spreadPanic` 只从 `processFlee` 调用，`beginFlee` 不调用）。跨物种、XZ 水平圆柱、不判视线、参照原触发玩家。
- 实现落点：`MobFleeLogic.processFlee` 拆为 `processFlee`（校验）+ `beginFlee`（写状态）+ `spreadPanic`（扩散）。
- 配置：`Config.PANIC_SPREAD_RADIUS`（`panicSpreadRadius`，默认 10.0，0 = 关闭）。
- 不要把它改成链式/持续扩散，除非用户明确要求（会失控）。

## 家养（domesticated）判定规则

- `af_domesticated` 只来自两条路：**玩家喂食**（`HungerSystem.feed`）、**家养后代继承**（`PregnancySystem.giveBirth` 新生幼崽；受精蛋走 NBT `af_offspring_domesticated`，孵化时读回）。
- **自然生成（含刷怪蛋）的幼崽必须是野生**。曾经 `GrowthSystem.initGrowth` 无条件 `setDomesticated` 导致所有幼崽变家养，已删除——不要再加回去。
- 野生幼崽不消耗饥饿（`HungerSystem.tick` 里 `!isDomesticated` 直接返回），靠 `GrowthSystem` 自行长大，不会被饿死。
- 注意：**野生母鸡只要附近有公鸡就会下受精蛋**（`canLayFertilizedEgg` 不要求家养），而 `OviparousLayHandler` 的非鸡卵生动物要求家养。所以受精蛋的「后代是否家养」必须由蛋 NBT 携带，不能假设。

## 文档同步约定

- 代码改动后需同步两处：`开发文档.md`（开发者用，按章节对应代码）与 `assets/animalflee/patchouli_books/animal_husbandry_guide/{zh_cn,en_us}/`（玩家用，中英必须**一一对应**）。
- 手册 JSON 改完用 `python -c "json.load(...)"` 校验，并核对 zh_cn / en_us 条目文件名集合相等。
- **DNF 凯莉猪相关的实现细节不得写入 `开发文档.md` 或任何手册/README**（见上）。
- 提交 Git 前按 `开发规范.md` §5 清单逐项核对（Config 分隔行数 = 项数-1、注释块内无 `""`、无代码注释、无临时文件）。