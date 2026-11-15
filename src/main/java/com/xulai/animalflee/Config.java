package com.xulai.animalflee;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        FLEE_TRIGGER_DISTANCE = BUILDER
                .comment(
                        "触发逃跑距离（格）",
                        "Flee trigger distance (blocks)",
                        "玩家靠近到这个距离时，被动/水生生物开始逃跑。",
                        "Passive and aquatic mobs start fleeing when the player gets within this distance.",
                        "Default: 10.0 / 默认：10.0",
                        "Range: 1.0 ~ 64.0"
                )
                .defineInRange("fleeTriggerDistance", 10.0, 1.0, 64.0);

        BUILDER.comment(" ");

        PASSIVE_MOB_FLEE = BUILDER
                .comment(
                        "被动生物是否逃跑",
                        "Whether passive mobs flee",
                        "被动生物包括猪、牛、羊、鸡、兔、蝙蝠等。",
                        "Passive mobs include pigs, cows, sheep, chickens, rabbits, bats, etc.",
                        "Default: true / 默认：true"
                )
                .define("passiveMobFlee", true);

        BUILDER.comment(" ");

        AQUATIC_MOB_FLEE = BUILDER
                .comment(
                        "水生生物是否逃跑",
                        "Whether aquatic mobs flee",
                        "水生生物包括鱼、鱿鱼、海豚、蝾螈等。",
                        "Aquatic mobs include fish, squid, dolphins, axolotls, etc.",
                        "Default: true / 默认：true"
                )
                .define("aquaticMobFlee", true);

        BUILDER.comment(" ");

        FLOCK_FLEE = BUILDER
                .comment(
                        "逃跑时是否启用群体行为（Boids 群聚算法）",
                        "Whether fleeing mobs use flocking behavior (Boids algorithm)",
                        "开启后，逃跑中的同类生物会互相分离、对齐、聚合，",
                        "受惊的鱼群会成群结队地逃窜，而不是各自盲目乱跑。",
                        "When enabled, fleeing mobs of the same species separate, align and cohere,",
                        "so a startled school scatters together instead of each one running blindly.",
                        "逃跑方向始终优先：群聚力只作为转向扰动叠加，",
                        "若群聚方向与逃跑方向相反，其影响会被大幅削弱，绝不会把生物拉回玩家身边。",
                        "The flee direction always takes priority. Flocking is only a steering nudge,",
                        "and any flock force opposing the flee direction is heavily damped,",
                        "so it can never pull a mob back toward the player.",
                        "Default: true / 默认：true"
                )
                .define("flockFlee", true);

        BUILDER.comment(" ");

        PANIC_SPREAD_RADIUS = BUILDER
                .comment(
                        "受惊扩散半径（格）",
                        "Panic spread radius (blocks)",
                        "动物开始逃跑的一瞬间，会让水平方向该半径内、",
                        "同样会逃跑的其它动物（不分物种）一起受惊逃跑。",
                        "When an animal starts fleeing, every other animal that would flee",
                        "(of any species) within this horizontal radius is startled into fleeing too.",
                        "扩散只发生一次：被波及的动物不会继续向更远处传播。",
                        "The spread fires only once: mobs pulled in do not spread it further.",
                        "设为 0 表示关闭该功能。",
                        "Set to 0 to disable this feature.",
                        "Default: 10.0 / 默认：10.0",
                        "Range: 0.0 ~ 64.0"
                )
                .defineInRange("panicSpreadRadius", 10.0, 0.0, 64.0);

        BUILDER.comment(" ");

        NEVER_FLEE_CHANCE = BUILDER
                .comment(
                        "个体永久不逃跑的概率",
                        "Chance for an individual to never flee from players",
                        "每个会逃跑的生物在首次逃跑判定时掷一次骰子，",
                        "命中该概率的个体会永久不再逃跑（隐身、跳跃暴露等触发同样不逃），",
                        "掷骰结果随个体永久保存，重载配置只影响尚未掷骰的生物。",
                        "设为 0 表示关闭该功能：所有会逃跑的生物都正常逃跑。",
                        "Each mob that would flee rolls once at its first flee check.",
                        "Individuals that hit this chance will never flee from players again",
                        "(invisibility and jump-exposure triggers included).",
                        "The result is stored per individual, so reloading the config",
                        "only affects mobs that have not rolled yet.",
                        "Set to 0 to disable the feature: every mob flees normally.",
                        "Default: 0.1 / 默认：0.1",
                        "Range: 0.0 ~ 1.0"
                )
                .defineInRange("neverFleeChance", 0.1, 0.0, 1.0);

        BUILDER.comment(" ");

        FISHING_REQUIRES_NATURAL_WATER = BUILDER
                .comment(
                        "钓鱼是否要求自然水域",
                        "Whether fishing requires natural water bodies",
                        "开启后，只有在河流、海洋、海滩生物群系，",
                        "或者连通水面达到一定大小的自然湖泊/池塘中才能钓到鱼。",
                        "When enabled, fish can only be caught in river, ocean or beach biomes,",
                        "or in natural lakes/ponds whose connected water surface is large enough.",
                        "玩家自己挖的小坑、人造水池无论等多久都钓不到鱼。",
                        "Small pits or artificial pools dug by players will never yield fish.",
                        "车万女仆的钓鱼任务同样遵守这条限制。",
                        "Maid fishing from Touhou Little Maid follows this restriction as well.",
                        "Default: true / 默认：true"
                )
                .define("fishingRequiresNaturalWater", true);

        BUILDER.comment(" ");

        FISHING_MIN_WATER_VOLUME = BUILDER
                .comment(
                        "自然湖泊最小连通水方块数量（格）",
                        "Minimum connected water blocks for a natural lake",
                        "当不在河流/海洋/海滩生物群系时，钩子周围连通的源水方块达到该数量才视为自然湖泊。",
                        "Outside river/ocean/beach biomes, the connected source water blocks around the hook",
                        "must reach this count to be treated as a natural lake.",
                        "Default: 64 / 默认：64",
                        "Range: 16 ~ 1024"
                )
                .defineInRange("fishingMinWaterVolume", 64, 16, 1024);

        BUILDER.comment(" ");

        FISH_FISH_SCAN_RADIUS = BUILDER
                .comment(
                        "钓鱼时扫描鱼实体的半径（格）",
                        "Radius to scan for fish entities when fishing",
                        "鱼漂周围这个范围内没有真实鱼实体时，这一杆不会咬钩（而不是咬钩后空手）；",
                        "垃圾与宝藏不受这条限制，随时可能上钩。",
                        "每钓上一条鱼，范围内就会减少一条鱼实体，直到钓完。",
                        "女仆的渔获取自这条鱼本身，钓上来的就是水里那条鱼的掉落物。",
                        "No real fish within this radius of the bobber means that cast never bites",
                        "(instead of biting with an empty hook); junk and treasure are unaffected.",
                        "Each caught fish removes one fish entity from the area until none remain.",
                        "Maid catches yield that same fish's own drops, so the species matches the water.",
                        "Default: 16 / 默认：16",
                        "Range: 4 ~ 64"
                )
                .defineInRange("fishScanRadius", 16, 4, 64);

        BUILDER.comment(" ");

        BAIT_BLACKLIST = BUILDER
                .comment(
                        "鱼饵黑名单（物品ID列表）",
                        "Fish bait blacklist (item ID list)",
                        "列入的物品即使本身可食用，丢进水里也不会成为鱼饵、不会生成鱼群。",
                        "Items in this list never work as fish bait, even if they are edible;",
                        "dropping them into water will not spawn fish.",
                        "用于防止珍贵食物被误丢；未列入的食物、种子与作物不受影响。",
                        "Use this to keep valuable food from being thrown in by mistake;",
                        "food, seeds and crops not listed here are unaffected.",
                        "格式：每个ID用双引号包裹，逗号分隔。",
                        "Format: comma-separated quoted IDs.",
                        "Default: [] / 默认：[]"
                )
                .defineListAllowEmpty("baitBlacklist", List.of(), () -> "", obj -> obj instanceof String);

        BUILDER.comment(" ");

        BAIT_WHITELIST = BUILDER
                .comment(
                        "鱼饵白名单（物品ID列表）",
                        "Fish bait whitelist (item ID list)",
                        "列入的物品一律可以作为鱼饵，即使它本身不能吃（例如木棍、骨头、线）。",
                        "Items in this list always work as fish bait, even if they are not edible",
                        "(sticks, bones, string and the like).",
                        "与鱼饵黑名单冲突时，黑名单优先。",
                        "If an item appears in both lists, the blacklist wins.",
                        "格式：每个ID用双引号包裹，逗号分隔。",
                        "Format: comma-separated quoted IDs.",
                        "Default: [] / 默认：[]"
                )
                .defineListAllowEmpty("baitWhitelist", List.of(), () -> "", obj -> obj instanceof String);

        BUILDER.comment(" ");

        HUNGER_ENABLED = BUILDER
                .comment(
                        "是否启用动物饥饿系统",
                        "Whether the animal hunger system is enabled",
                        "开启后，被玩家喂养过的家养动物会随时间消耗饥饿值。",
                        "When enabled, domesticated animals (fed once by a player) lose hunger over time.",
                        "Default: true / 默认：true"
                )
                .define("hungerEnabled", true);

        BUILDER.comment(" ");

        BREED_MIN_HUNGER_PERCENT = BUILDER
                .comment(
                        "动物可繁殖所需的最低饥饿百分比",
                        "Minimum hunger percentage for animals to breed",
                        "动物需要达到该百分比（占其最大饥饿值）才能参与繁殖/交配。",
                        "Animals must be at or above this percentage of their max hunger to breed.",
                        "Default: 80 / 默认：80",
                        "Range: 0 ~ 100"
                )
                .defineInRange("breedMinHungerPercent", 80, 0, 100);

        BUILDER.comment(" ");

        HUNGER_DAILY_DRAIN_PERCENT = BUILDER
                .comment(
                        "家养动物每天消耗的饥饿值百分比",
                        "Percent of a domesticated animal's hunger consumed per day",
                        "每只动物每天（1 MC 天）按最大饥饿值的该百分比消耗。",
                        "Each animal consumes this percentage of its max hunger per in-game day.",
                        "每只动物还有独立的代谢系数（0.8~1.2 随机浮动），实际消耗在此基础上上下浮动。",
                        "Each animal also has a fixed metabolism factor (0.8~1.2) that varies the actual drain.",
                        "Default: 50 / 默认：50",
                        "Range: 1 ~ 100"
                )
                .defineInRange("dailyHungerDrainPercent", 50, 1, 100);

        BUILDER.comment(" ");

        HUNGER_STARVE_DAYS = BUILDER
                .comment(
                        "饥饿值归零后，还能存活的天数",
                        "Days a domesticated animal survives after hunger reaches zero",
                        "归零后动物不会立刻死亡，而是进入挨饿状态缓慢掉血，",
                        "在这段时间内喂食可以救回来。",
                        "After hunger reaches zero the animal does not die instantly;",
                        "it enters a starving state and slowly loses health,",
                        "and can be saved by feeding it during this window.",
                        "Default: 3 / 默认：3",
                        "Range: 1 ~ 30"
                )
                .defineInRange("hungerStarveDays", 3, 1, 30);

        BUILDER.comment(" ");

        COW_PREGNANCY_MINUTES = BUILDER
                .comment(
                        "牛基准孕期（分钟）",
                        "Base cow pregnancy duration in minutes",
                        "其它生物按体型等比例缩放。",
                        "Other animals scale this by body size.",
                        "Default: 120 / 默认：120",
                        "Range: 5 ~ 1440"
                )
                .defineInRange("cowPregnancyMinutes", 120, 5, 1440);

        BUILDER.comment(" ");

        PREGNANCY_FLOAT_MINUTES = BUILDER
                .comment(
                        "孕期浮动（分钟）",
                        "Pregnancy duration random float in minutes",
                        "实际孕期在基准值上额外加上 0~浮动值 之间的随机分钟。",
                        "Actual duration adds a random 0~float minutes on top of the base.",
                        "Default: 20 / 默认：20",
                        "Range: 0 ~ 720"
                )
                .defineInRange("pregnancyFloatMinutes", 20, 0, 720);

        BUILDER.comment(" ");

        MISCARRIAGE_CHANCE = BUILDER
                .comment(
                        "怀孕期间受伤导致流产的概率",
                        "Chance of miscarriage when a pregnant animal is hurt",
                        "0.0 = 永不流产，1.0 = 必然流产。",
                        "0.0 = never, 1.0 = always.",
                        "Default: 0.2 / 默认：0.2",
                        "Range: 0.0 ~ 1.0"
                )
                .defineInRange("miscarriageChance", 0.2, 0.0, 1.0);

        BUILDER.comment(" ");

        COOLDOWN_MINUTES = BUILDER
                .comment(
                        "产后冷却基准（分钟）",
                        "Post-birth/post-miscarriage cooldown in minutes",
                        "生产或流产后，动物在该时间内无法再次怀孕。",
                        "After giving birth or miscarrying, the animal cannot become pregnant again for this long.",
                        "Default: 120 / 默认：120",
                        "Range: 1 ~ 1440"
                )
                .defineInRange("cooldownMinutes", 120, 1, 1440);

        BUILDER.comment(" ");

        COW_GROWTH_DAYS = BUILDER
                .comment(
                        "牛幼崽基准成长天数（MC 天）",
                        "Base calf growth duration in MC days",
                        "幼崽长成成年所需的时间基准，其它生物按体型等比例缩放。",
                        "Other animals scale this by body size.",
                        "实际成长时间在基准上额外上下浮动 growthFloatDays 天。",
                        "Actual duration floats by plus/minus growthFloatDays days.",
                        "Default: 7 / 默认：7",
                        "Range: 1 ~ 60"
                )
                .defineInRange("cowGrowthDays", 7, 1, 60);

        BUILDER.comment(" ");

        GROWTH_FLOAT_DAYS = BUILDER
                .comment(
                        "幼崽成长天数浮动（天）",
                        "Growth duration random float in days",
                        "实际成长天数在基准值上下浮动该天数，例如基准 7、浮动 2 则为 5~9 天。",
                        "Actual growth days float plus/minus this value, e.g. base 7 with float 2 gives 5~9 days.",
                        "Default: 2 / 默认：2",
                        "Range: 0 ~ 10"
                )
                .defineInRange("growthFloatDays", 2, 0, 10);

        BUILDER.comment(" ");

        GROWTH_MIN_HUNGER_PERCENT = BUILDER
                .comment(
                        "幼崽成长所需的最低饥饿百分比",
                        "Minimum hunger percentage for a baby to grow",
                        "幼崽饥饿达到该百分比（占其最大饥饿值）才会累积成长时间，",
                        "饥饿不足时成长停滞，需要喂食后才会继续成长。",
                        "A baby only accumulates growth time while at or above this percentage of its max hunger;",
                        "growth pauses when hungry and resumes after feeding.",
                        "Default: 50 / 默认：50",
                        "Range: 0 ~ 100"
                )
                .defineInRange("growthMinHungerPercent", 50, 0, 100);

        BUILDER.comment(" ");

        OVIPAROUS_MOBS = BUILDER
                .comment(
                        "卵生动物列表（条目格式：生物ID,蛋物品ID）",
                        "List of oviparous mobs (entry format: mobID,eggItemID)",
                        "列入的动物不会怀孕，而是像鸡一样按周期产蛋。",
                        "Mobs in this list do not get pregnant; they lay eggs on a cycle like chickens.",
                        "每个条目由生物实体ID和它产出的蛋物品ID组成，用逗号分隔。",
                        "Each entry pairs a mob entity ID with the egg item ID that mob lays, separated by a comma.",
                        "Default: [\"minecraft:chicken,minecraft:egg\"]",
                        "示例：[\"minecraft:chicken,minecraft:egg\", \"examplemod:dino,examplemod:dino_egg\"]",
                        "格式：每个条目用双引号包裹，条目之间用逗号分隔。",
                        "Format: comma-separated quoted entries."
                )
                .defineListAllowEmpty("oviparousMobs", List.of(
                        "minecraft:chicken,minecraft:egg"
                ), () -> "", obj -> obj instanceof String);

        BUILDER.comment(" ");

        BLOCK_NORMAL_EGG_HATCHING = BUILDER
                .comment(
                        "是否阻止普通蛋扔出后孵化",
                        "Whether to block normal eggs from hatching when thrown",
                        "开启后，普通蛋扔出不再随机孵出幼崽；受精蛋无论是否开启扔出都不孵化。",
                        "When enabled, normal eggs no longer randomly hatch when thrown;",
                        "fertilized eggs never hatch when thrown regardless of this setting.",
                        "Default: true / 默认：true"
                )
                .define("blockNormalEggHatching", true);

        BUILDER.comment(" ");

        EGG_INCUBATION_MINUTES = BUILDER
                .comment(
                        "受精蛋孵化所需的游戏内时间（分钟）",
                        "In-game minutes required for a fertilized egg to hatch",
                        "受精蛋必须放在「不能见天空的干草块」上才会孵化；",
                        "离开干草块或重见天空会重置进度。",
                        "1 MC 天 = 20 分钟。",
                        "A fertilized egg only hatches when placed on a hay block that cannot see the sky;",
                        "moving it off the hay block or exposing it to the sky resets the progress.",
                        "1 MC day = 20 minutes.",
                        "Default: 20 (1 MC day) / 默认：20（1 MC 天）",
                        "Range: 1 ~ 1440"
                )
                .defineInRange("eggIncubationMinutes", 20, 1, 1440);

        BUILDER.comment(" ");

        BLACKLISTED_MOBS = BUILDER
                .comment(
                        "不会逃跑的生物黑名单（实体ID列表）",
                        "Blacklist of mob IDs that will never flee.",
                        "这些生物即使属于被动/水生类别也不会逃跑。",
                        "Mobs in this list will not flee even if they are passive/aquatic.",
                        "默认包含原版 14 种，以及 Alex 的生物中会攻击玩家、不该掉头逃跑的掠食者。",
                        "Defaults cover 14 vanilla mobs plus Alex's Mobs predators that fight instead of fleeing.",
                        "Default: [\"minecraft:wolf\", \"minecraft:cat\", \"minecraft:guardian\", \"minecraft:elder_guardian\", \"minecraft:sniffer\", \"minecraft:skeleton_horse\", \"minecraft:zombie_horse\", \"minecraft:trader_llama\", \"minecraft:dolphin\", \"minecraft:polar_bear\", \"minecraft:bee\", \"minecraft:axolotl\", \"minecraft:iron_golem\", \"minecraft:snow_golem\",",
                        "           \"alexsmobs:alligator_snapping_turtle\", \"alexsmobs:anaconda\", \"alexsmobs:bald_eagle\", \"alexsmobs:frilled_shark\", \"alexsmobs:froststalker\", \"alexsmobs:giant_squid\", \"alexsmobs:grizzly_bear\", \"alexsmobs:hammerhead_shark\", \"alexsmobs:komodo_dragon\", \"alexsmobs:orca\", \"alexsmobs:rattlesnake\", \"alexsmobs:sea_bear\", \"alexsmobs:snow_leopard\", \"alexsmobs:tasmanian_devil\", \"alexsmobs:tiger\"]",
                        "格式：每个ID用双引号包裹，逗号分隔。",
                        "Format: comma-separated quoted IDs."
                )
                .defineListAllowEmpty("blacklistedMobs", List.of(
                        "minecraft:wolf",
                        "minecraft:cat",
                        "minecraft:guardian",
                        "minecraft:elder_guardian",
                        "minecraft:sniffer",
                        "minecraft:skeleton_horse",
                        "minecraft:zombie_horse",
                        "minecraft:trader_llama",
                        "minecraft:dolphin",
                        "minecraft:polar_bear",
                        "minecraft:bee",
                        "minecraft:axolotl",
                        "minecraft:iron_golem",
                        "minecraft:snow_golem",
                        "alexsmobs:alligator_snapping_turtle",
                        "alexsmobs:anaconda",
                        "alexsmobs:bald_eagle",
                        "alexsmobs:frilled_shark",
                        "alexsmobs:froststalker",
                        "alexsmobs:giant_squid",
                        "alexsmobs:grizzly_bear",
                        "alexsmobs:hammerhead_shark",
                        "alexsmobs:komodo_dragon",
                        "alexsmobs:orca",
                        "alexsmobs:rattlesnake",
                        "alexsmobs:sea_bear",
                        "alexsmobs:snow_leopard",
                        "alexsmobs:tasmanian_devil",
                        "alexsmobs:tiger"
                ), () -> "", obj -> obj instanceof String);

        BUILDER.comment(" ");

        EXCLUDED_MOBS = BUILDER
                .comment(
                        "模组排外生物黑名单（实体ID列表）",
                        "Blacklist of mob IDs excluded from this mod.",
                        "列入的生物会被模组完全忽略，只遵循原版行为（不逃跑、无饥饿/性别/怀孕/成长、Jade 不显示模组信息）。",
                        "Mobs in this list are completely ignored by this mod and follow vanilla behavior only",
                        "(no fleeing, no hunger/gender/pregnancy/growth, no mod Jade info).",
                        "默认值分两组：原版三项，以及 Alex 的生物中产蛋、产卵、变态发育或多部件的巨兽。",
                        "Defaults come in two groups: three vanilla mobs, plus Alex's Mobs species that lay eggs,",
                        "metamorphose or use multi-part bodies.",
                        "海龟与青蛙的繁殖产出是卵/蝌蚪而非幼崽，嗅探兽掉的是嗅探兽蛋，",
                        "Alex 的上述生物同样不走通用幼崽路径，接进怀孕系统只会产出错误结果。",
                        "Turtles lay eggs, frogs produce frogspawn and sniffers drop sniffer eggs instead of a cub;",
                        "the listed Alex's Mobs species follow the same non-cub path, so pregnancy cannot replace them.",
                        "Default: [\"minecraft:turtle\", \"minecraft:frog\", \"minecraft:sniffer\", \"alexsmobs:caiman\", \"alexsmobs:crocodile\", \"alexsmobs:platypus\", \"alexsmobs:terrapin\", \"alexsmobs:triops\", \"alexsmobs:emu\", \"alexsmobs:cockroach\", \"alexsmobs:mungus\", \"alexsmobs:stradpole\", \"alexsmobs:cachalot_whale\", \"alexsmobs:laviathan\"]",
                        "格式：每个ID用双引号包裹，逗号分隔。",
                        "Format: comma-separated quoted IDs."
                )
                .defineListAllowEmpty("excludedMobs", List.of(
                        "minecraft:turtle",
                        "minecraft:frog",
                        "minecraft:sniffer",
                        "alexsmobs:caiman",
                        "alexsmobs:crocodile",
                        "alexsmobs:platypus",
                        "alexsmobs:terrapin",
                        "alexsmobs:triops",
                        "alexsmobs:emu",
                        "alexsmobs:cockroach",
                        "alexsmobs:mungus",
                        "alexsmobs:stradpole",
                        "alexsmobs:cachalot_whale",
                        "alexsmobs:laviathan"
                ), () -> "", obj -> obj instanceof String);

        BUILDER.comment(" ");

        CUSTOM_ANIMAL_FOODS = BUILDER
                .comment(
                        "自定义动物饲料列表（物品ID:饱食度值）",
                        "Custom animal food list (itemID:saturation)",
                        "玩家可指定任意物品作为动物饲料，并为其设定饱食度值。",
                        "Players can designate any item as animal food and set its saturation value.",
                        "格式：每个条目用双引号包裹，逗号分隔。两种写法：",
                        "Format: each entry wrapped in double quotes, comma-separated. Two forms:",
                        "  1. 只写物品ID（无值）：仅当该物品本身有饱食度（原版可吃）时生效，使用原版营养值。",
                        "     1. Item ID only (no value): only works if the item has vanilla saturation;",
                        "        the vanilla nutrition value is used.",
                        "  2. 物品ID:值（带值）：强制指定饱食度值，会覆盖原版值。",
                        "     2. Item ID:value (with value): forces the saturation value, overriding vanilla.",
                        "示例 Examples:",
                        "  [\"minecraft:apple\"]                       -> 苹果，用原版 4 点",
                        "  [\"minecraft:stick:1.5\"]                  -> 木棍，强制 1.5 点",
                        "  [\"minecraft:apple:6\", \"minecraft:stone:2\"] -> 苹果覆盖为 6 点，石头 2 点",
                        "注意：只写 ID 且该物品没有原版饱食度时，无法作为饲料（会被忽略）。",
                        "Note: an ID-only entry whose item has no vanilla saturation is ignored",
                        "(cannot be used as food).",
                        "Default: [] / 默认：[]"
                )
                .defineListAllowEmpty("customAnimalFoods", List.of(), () -> "", obj -> obj instanceof String);

        BUILDER.comment(" ");

        HERBIVORE_MOBS = BUILDER
                .comment(
                        "草食动物列表（实体ID列表）",
                        "Herbivore mob list (entity ID list)",
                        "草食动物只能吃素食（非肉非鱼）：作物、种子、干草块、苹果、糖等。",
                        "Herbivores can only eat vegetarian food (no meat or fish): crops, seeds, hay, apples, sugar, etc.",
                        "未出现在草食或肉食列表中的动物默认为杂食。",
                        "Animals in neither list default to omnivore.",
                        "默认包含原版 14 种，以及 Alex 的生物中以植物、果实、花蜜为主食的物种。",
                        "Defaults cover 14 vanilla mobs plus Alex's Mobs species that feed on plants, fruit and nectar.",
                        "格式：每个ID用双引号包裹，逗号分隔。",
                        "Format: comma-separated quoted IDs."
                )
                .defineListAllowEmpty("herbivoreMobs", List.of(
                        "minecraft:cow",
                        "minecraft:sheep",
                        "minecraft:goat",
                        "minecraft:rabbit",
                        "minecraft:horse",
                        "minecraft:donkey",
                        "minecraft:mule",
                        "minecraft:llama",
                        "minecraft:trader_llama",
                        "minecraft:camel",
                        "minecraft:panda",
                        "minecraft:parrot",
                        "minecraft:bee",
                        "minecraft:sniffer",
                        "alexsmobs:banana_slug",
                        "alexsmobs:bison",
                        "alexsmobs:elephant",
                        "alexsmobs:endergrade",
                        "alexsmobs:flutter",
                        "alexsmobs:gazelle",
                        "alexsmobs:gelada_monkey",
                        "alexsmobs:gorilla",
                        "alexsmobs:hummingbird",
                        "alexsmobs:kangaroo",
                        "alexsmobs:leafcutter_ant",
                        "alexsmobs:moose",
                        "alexsmobs:rhinoceros",
                        "alexsmobs:sugar_glider",
                        "alexsmobs:sunbird",
                        "alexsmobs:toucan",
                        "alexsmobs:tusklin"
                ), () -> "", obj -> obj instanceof String);

        BUILDER.comment(" ");

        CARNIVORE_MOBS = BUILDER
                .comment(
                        "肉食动物列表（实体ID列表）",
                        "Carnivore mob list (entity ID list)",
                        "肉食动物只能吃肉类和鱼类（生/熟肉、鱼、腐肉等）。",
                        "Carnivores can only eat meat and fish (raw/cooked meat, fish, rotten flesh, etc.).",
                        "未出现在草食或肉食列表中的动物默认为杂食。",
                        "Animals in neither list default to omnivore.",
                        "默认包含原版 5 种，以及 Alex 的生物中的捕食者、食虫与食鱼物种。",
                        "Defaults cover 5 vanilla mobs plus Alex's Mobs predators, insectivores and piscivores.",
                        "格式：每个ID用双引号包裹，逗号分隔。",
                        "Format: comma-separated quoted IDs."
                )
                .defineListAllowEmpty("carnivoreMobs", List.of(
                        "minecraft:cat",
                        "minecraft:wolf",
                        "minecraft:ocelot",
                        "minecraft:polar_bear",
                        "minecraft:axolotl",
                        "alexsmobs:alligator_snapping_turtle",
                        "alexsmobs:anaconda",
                        "alexsmobs:anteater",
                        "alexsmobs:bald_eagle",
                        "alexsmobs:cosmaw",
                        "alexsmobs:froststalker",
                        "alexsmobs:komodo_dragon",
                        "alexsmobs:maned_wolf",
                        "alexsmobs:mantis_shrimp",
                        "alexsmobs:mimic_octopus",
                        "alexsmobs:mudskipper",
                        "alexsmobs:orca",
                        "alexsmobs:potoo",
                        "alexsmobs:rain_frog",
                        "alexsmobs:rattlesnake",
                        "alexsmobs:roadrunner",
                        "alexsmobs:seagull",
                        "alexsmobs:seal",
                        "alexsmobs:shoebill",
                        "alexsmobs:snow_leopard",
                        "alexsmobs:tasmanian_devil",
                        "alexsmobs:tiger",
                        "alexsmobs:warped_toad"
                ), () -> "", obj -> obj instanceof String);

        BUILDER.comment(" ");

        FALL_DAMAGE_LOOT = BUILDER
                .comment(
                        "动物摔死是否掉落掉落物",
                        "Whether animals drop loot when killed by fall damage",
                        "开启后，动物因摔落伤害死亡时会掉落与玩家亲手击杀完全一致的掉落物",
                        "（包括需要 killed_by_player 条件的特殊物品，如兔子脚）。",
                        "When enabled, animals killed by fall damage drop the same loot as if killed by a player",
                        "(including special items that require the killed_by_player condition, such as rabbit feet).",
                        "摔死不会掉落经验。",
                        "Fall deaths do not drop experience.",
                        "Default: true / 默认：true"
                )
                .define("fallDamageLoot", true);
    }

    public static final ModConfigSpec.DoubleValue FLEE_TRIGGER_DISTANCE;
    public static final ModConfigSpec.BooleanValue PASSIVE_MOB_FLEE;
    public static final ModConfigSpec.BooleanValue AQUATIC_MOB_FLEE;
    public static final ModConfigSpec.BooleanValue FLOCK_FLEE;
    public static final ModConfigSpec.DoubleValue PANIC_SPREAD_RADIUS;
    public static final ModConfigSpec.DoubleValue NEVER_FLEE_CHANCE;
    public static final ModConfigSpec.BooleanValue FISHING_REQUIRES_NATURAL_WATER;
    public static final ModConfigSpec.IntValue FISHING_MIN_WATER_VOLUME;
    public static final ModConfigSpec.IntValue FISH_FISH_SCAN_RADIUS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BAIT_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BAIT_WHITELIST;
    public static final ModConfigSpec.BooleanValue HUNGER_ENABLED;
    public static final ModConfigSpec.IntValue BREED_MIN_HUNGER_PERCENT;
    public static final ModConfigSpec.IntValue HUNGER_DAILY_DRAIN_PERCENT;
    public static final ModConfigSpec.IntValue HUNGER_STARVE_DAYS;
    public static final ModConfigSpec.IntValue COW_PREGNANCY_MINUTES;
    public static final ModConfigSpec.IntValue PREGNANCY_FLOAT_MINUTES;
    public static final ModConfigSpec.DoubleValue MISCARRIAGE_CHANCE;
    public static final ModConfigSpec.IntValue COOLDOWN_MINUTES;
    public static final ModConfigSpec.IntValue COW_GROWTH_DAYS;
    public static final ModConfigSpec.IntValue GROWTH_FLOAT_DAYS;
    public static final ModConfigSpec.IntValue GROWTH_MIN_HUNGER_PERCENT;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> OVIPAROUS_MOBS;
    public static final ModConfigSpec.BooleanValue BLOCK_NORMAL_EGG_HATCHING;
    public static final ModConfigSpec.IntValue EGG_INCUBATION_MINUTES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_MOBS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_MOBS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CUSTOM_ANIMAL_FOODS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> HERBIVORE_MOBS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CARNIVORE_MOBS;
    public static final ModConfigSpec.BooleanValue FALL_DAMAGE_LOOT;

    public static final ModConfigSpec SPEC = BUILDER.build();
}