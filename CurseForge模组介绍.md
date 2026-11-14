# 🐄 Pasture Tales

**Vanilla farming, rebuilt as an actual ranch.**

In vanilla, two animals and a handful of wheat are all it takes to produce a baby. Pasture Tales replaces that shortcut with a full husbandry loop: animals that flee from you, have genders and diets, get hungry, court and conceive, carry a pregnancy to term, and raise young that take real in-game days to grow. Fishing gets the same treatment — a one-block pond in your base is no longer a cod dispenser.

Every rule below is configurable, and the entire system is documented in an in-game handbook.

**At a glance**

- Wild animals flee, and panic spreads through the herd
- Males and females, with clearly different builds
- Hunger, three diet types, and permanent domestication
- Courtship, pregnancy, birth, and growth measured in days
- Fertilized eggs, 3×3 hay-block hatcheries, moulting chickens
- Fishing that requires natural water and real fish
- Full loot from animals that die falling

## 🏃 They Run From You

Come within about ten blocks of a wild animal and it bolts. Sneaking does not help. An invisibility potion and a slow, quiet approach get you much closer, but they may still catch your scent — and jumping gives you away instantly. Fleeing animals steer around obstacles and climb one-block ledges instead of pinning themselves against the first fence they meet.

Roughly one animal in ten is born fearless and never runs. That is decided once per animal, so it holds for life.

Panic is contagious. The instant one animal is startled, every animal of every species within ten blocks runs with it. It spreads exactly one wave, so a single startled cow cannot set off an entire biome.

Animals you have domesticated, tamed pets, and anything you are riding — or just dismounted from — ignore you completely.

## ⚖️ Gender and Build

Each animal is assigned male or female as it enters the world, an even split. Males are noticeably larger, females slightly smaller, so the makeup of a herd is visible at a glance. Both are needed to produce offspring.

## 🌾 Hunger, Diet, Domestication

Feed an animal once and it becomes domesticated — permanently. From then on, it is your responsibility.

- **Diet decides the menu.** Herbivores eat crops, seeds, hay and fruit; carnivores eat meat and fish; omnivores eat anything. Meat and fish are recognised by standard item tags, so food from other mods counts too.
- **Hunger drains daily.** The default is half of an animal's maximum per in-game day, and pregnancy burns it faster.
- **Starving is lethal but reversible.** At zero hunger an animal loses health over three days and may lose a pregnancy. Feed it in time and it recovers.
- **Well-fed animals heal.** Above 80% hunger, an injured animal slowly regenerates.
- **Wild animals look after themselves.** Animals you have never fed — including wild newborns — never burn hunger and never starve. Keeping livestock is something you opt into by taming them.

## 💞 Courtship, Pregnancy, Birth

Domesticated animals breed on their own, but not by vanilla rules.

- Both parents must be domesticated and well fed, and the female must be past her recovery period.
- The female seeks out a suitable male and mates with him herself; a wild male brought to the pen will not do.
- Pregnancy length scales with body size — roughly two hours for a cow — and her belly swells as it progresses.
- Damage taken during pregnancy can cause a miscarriage.
- Birth produces a domesticated newborn, and the mother needs time to recover before the next one. Males never need a cooldown, so one good bull serves the whole herd.
- Horses and donkeys still pair up to produce mules.

## 🐣 Eggs, Chicks, Feathers

- Only hens lay eggs, about one per in-game day.
- An egg is **fertilized** only if the hen is domesticated and has mated recently. Wild hens and unmated hens lay ordinary eggs.
- A fertilized egg names its species in the tooltip. Place up to **nine** on the top of a hay block: they settle into a tidy 3×3 grid, each with its own timer.
- The hay block must be **sheltered from the sky**. One in-game day — twenty minutes — later, the chick hatches, already domesticated.
- Eggs can be taken back by right-clicking the hay block, and incubating eggs cannot be pushed by water or scooped up by accident.
- Fertilized eggs never hatch when thrown, and ordinary eggs are blocked from hatching that way by default. Hatching is a ranch activity, not a projectile one.
- Chickens of both sexes moult, dropping one or two feathers about once a day.

## 🐟 Fishing and Fish Farming

Fishing now depends on where you cast and on what is actually swimming there.

- **Natural water only.** Rivers, oceans and beaches always qualify; other water must be open to the sky with at least 64 connected source blocks. A decorative pond produces nothing.
- **Real fish must be present.** With a fish nearby, a bite draws that fish to your feet — kill it for the drops. With no fish around, you will not get a fish bite at all: the bobber only twitches for junk and treasure. A bite never leaves you empty-handed.
- **Stock your own water.** Drop almost any edible item into natural water as bait; half a day to a day later, two to five fish appear. Dig a lake, seed it, and fish it indefinitely.
- Luck and open water still affect the haul exactly as they always have.

## 🩸 Fair Fall Loot

Animals that die from fall damage drop exactly what a player kill would give — rabbit's foot included — provided a player is within 32 blocks. No experience is granted.

## 📖 An In-Game Handbook

Your first join delivers the **Animal Husbandry Guide**, a Patchouli book that walks through every system with timings, numbers and practical advice. Patchouli is needed for the book to appear; everything else works without it.

## 🔌 Mod Integration

- **Jade** — diet (colour-coded), gender, domestication, pregnancy progress, recovery cooldown, next egg and next moult countdowns, growth progress, hunger and starving state, plus incubation progress on eggs. Jade's own "time until grown" figure is hidden, because it no longer matches reality.
- **Touhou Little Maid** — maids feed only domesticated animals that are actually hungry, only with food the animal's diet permits, and keep going until it is full. Their fishing follows the same water and live-fish rules.
- **Breeding modifiers** — births and hatches announce themselves the standard way, so mods that add twins, birth bonuses or inherited stats apply to offspring produced here.

## ⚙️ Configuration

Flee distance, panic radius, the chance of a fearless animal, hunger drain, pregnancy and growth length, miscarriage chance, minimum water size, fish scan radius and more are adjustable in the config file or in-game, and take effect immediately. Species lists — herbivore, carnivore, egg-laying, flee blacklist and fully excluded — are editable as well. The excluded list exists for creatures whose breeding cannot be safely replaced; they are left entirely untouched by this mod.

## ✅ Safe for Existing Worlds

Nothing is regenerated or replaced. Animals already in your world are adopted into the system as they load, and creatures from other mods can be brought in through the species lists. If one behaves oddly, exclude it and it returns to vanilla behaviour.

---

# 🐄 Pasture Tales 牧业

**把原版的养殖，重做成一套真正的牧业循环。**

原版里，两只动物加一把小麦就能变出一只幼崽。本模组取消了这条捷径，换成完整的养殖链路：动物会躲人，有性别与食性，会饿，会自己求偶受孕、怀胎生产，幼崽要按游戏天数慢慢长大。钓鱼也一样——基地里挖一格水池，再也换不来鳕鱼。

下文所有规则均可配置，整套机制也都写进了游戏内手册。

**一览**

- 野生动物会逃跑，受惊还会传染整群
- 分雌雄，体型差异一眼可辨
- 饥饿、三种食性、喂食即永久家养
- 求偶、怀孕、生产，成长以天计
- 受精蛋、干草块 3×3 孵化、鸡会换羽
- 钓鱼只认自然水域与真实鱼群
- 摔死的动物也掉完整战利品

## 🏃 它们会躲你

走近野生动物约十格，它就会掉头就跑。按潜行没有用。喝下隐身药水、放慢脚步不疾跑也不跳跃，可以靠得更近，但贴得太近仍会被嗅到；一旦跳跃，立刻暴露。逃跑中的动物会绕开障碍、翻上一格高的台阶，不会一头顶在围栏上原地打转。

大约每十只里有一只是天生不怕人的，一辈子都不会逃。这个特性每只动物只判定一次。

受惊会传染：一只动物开始逃跑的瞬间，十格内不分物种的其它动物会一起跑。扩散只有一波，一头受惊的牛不会点燃整片草原。

已家养的动物、有主的宠物，以及你正骑着或刚下的坐骑，完全不会躲你。

## ⚖️ 性别与体型

动物进入世界时随机分配雌雄，各占一半。雄性明显更大，雌性略小，一群动物的构成一眼可见。繁衍后代需要一雄一雌。

## 🌾 饥饿、食性与家养

喂食一次，动物即永久家养——从此它归你照料。

- **食性决定菜单。**草食动物吃作物、种子、干草与果实；肉食动物只吃肉和鱼；杂食动物什么都吃。肉与鱼按标准物品标签识别，其它模组的食物同样通用。
- **饥饿按天消耗。**默认每个游戏日消耗最大饥饿的一半，怀孕期间消耗更快。
- **挨饿会死，但救得回来。**饥饿归零后，动物在三天内持续掉血，还可能流产；及时喂食即可恢复。
- **吃饱会回血。**饥饿高于八成时，受伤的动物会缓慢恢复生命。
- **野生动物自给自足。**你从未喂过的动物——包括野外出生的幼崽——不消耗饥饿，也不会饿死。养牲畜是你选择驯服之后才要承担的事。

## 💞 求偶、怀孕与生产

家养动物会自己繁殖，但不按原版规则。

- 父母双方都必须已家养且吃饱，雌性还要过了产后恢复期。
- 雌性会自己寻找合适的雄性完成交配；从野外牵一头公牛回来是没用的。
- 孕期随体型缩放，牛约两小时，肚子会随孕期推进而隆起。
- 孕期受伤有概率流产。
- 生产得到的是天生家养的幼崽，母畜需要时间恢复才能怀下一胎。雄性没有冷却，一头好公牛可以配整群。
- 马与驴仍可配对，生下骡子。

## 🐣 鸡蛋、雏鸡与羽毛

- 只有母鸡下蛋，约每个游戏日一枚。
- 只有**已家养且近期交配过**的母鸡，产下的蛋才是受精蛋；野生母鸡与未交配的母鸡只下普通蛋。
- 受精蛋的提示会写明物种。手持受精蛋右键干草块顶面，一次最多放**九枚**，自动排成 3×3，每枚单独计时。
- 干草块必须**不见天日**。一个游戏日（二十分钟）后小鸡破壳，出生即家养。
- 右键干草块即可取回蛋；孵化中的蛋不会被水流推动，也不会被误捡。
- 受精蛋扔出永远不会孵化，普通蛋默认也被禁止扔出孵化。孵化是牧场里的事，不是投掷物的事。
- 公母鸡都会换羽，约每天掉落一到两根羽毛。

## 🐟 钓鱼与鱼群

钓鱼现在取决于你下竿的地方，以及水里真正有什么。

- **只限自然水域。**河流、海洋与海滩始终合格；其它水域必须露天，且连通水源至少 64 格。基地里的装饰水池什么也钓不到。
- **附近必须有真鱼。**有鱼时，咬钩会把那条鱼拉到脚边，你得自己动手才有掉落物。附近没有鱼，就根本不会有鱼来咬钩——浮漂只会为垃圾与宝藏而动。只要咬钩，就不会空手。
- **自己养鱼。**把几乎任何可食用物品丢进自然水域当鱼饵；半天到一天后，附近会出现二到五条鱼。挖一片湖、投几次饵，就能长期取用。
- 运气与开阔水域依旧照常影响收获。

## 🩸 摔死也有完整掉落

动物因摔落死亡时，掉落与玩家亲手击杀完全一致——包括兔子脚——前提是 32 格内有玩家。不掉经验。

## 📖 游戏内手册

首次进入游戏会获得一本《牧业指南》，基于 Patchouli，逐条讲解每套机制的时间、数值与实操建议。手册的显示需要安装 Patchouli，其余功能不受影响。

## 🔌 模组联动

- **Jade**——看向动物即可看到食性（带颜色）、性别、家养状态、怀孕进度、产后冷却、下次产蛋与下次掉毛倒计时、成长进度、饥饿与挨饿状态；看向受精蛋还能看到孵化进度。Jade 自带的「还有多久长大」会被隐藏，因为它已不再符合实际规则。
- **东方 Little Maid（车万女仆）**——女仆只喂确实饿了的家养动物，只用该动物食性允许的食物，并且一直喂到吃饱；她们的钓鱼遵守同样的水域与真鱼规则。
- **繁殖类模组**——出生与孵化按标准方式广播，因此添加双胞胎、出生奖励或属性继承的模组，对这里出生的后代同样生效。

## ⚙️ 配置

逃跑距离、受惊半径、天生不怕人的比例、饥饿消耗、孕期与成长时长、流产概率、最小水域、找鱼半径等等，都可以在配置文件中或游戏内调整，改完立即生效。物种名单——草食、肉食、卵生、逃跑黑名单、完全排除——同样可以编辑。完全排除名单留给那些繁殖方式无法安全替换的生物，它们不受本模组任何影响。

## ✅ 对已有存档安全

不会重新生成或替换任何地形与方块。已有动物在载入时纳入系统，其它模组的生物也能通过物种名单接入。若某种生物表现异常，把它排除即可恢复原版行为。
