package com.xulai.animalflee;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue FLEE_TRIGGER_DISTANCE = BUILDER
            .comment(
                    "触发逃跑距离（格）",
                    "Flee trigger distance (blocks)",
                    "",
                    "玩家靠近到这个距离时，被动/水生生物开始逃跑。",
                    "Passive and aquatic mobs start fleeing when the player gets within this distance.",
                    "",
                    "Default: 10.0 / 默认：10.0",
                    "Range: 1.0 ~ 64.0"
            )
            .defineInRange("fleeTriggerDistance", 10.0, 1.0, 64.0);

    public static final ModConfigSpec.BooleanValue PASSIVE_MOB_FLEE = BUILDER
            .comment(
                    "被动生物是否逃跑",
                    "Whether passive mobs flee",
                    "",
                    "被动生物包括猪、牛、羊、鸡、兔、蝙蝠等。",
                    "Passive mobs include pigs, cows, sheep, chickens, rabbits, bats, etc.",
                    "",
                    "Default: true / 默认：true"
            )
            .define("passiveMobFlee", true);

    public static final ModConfigSpec.BooleanValue AQUATIC_MOB_FLEE = BUILDER
            .comment(
                    "水生生物是否逃跑",
                    "Whether aquatic mobs flee",
                    "",
                    "水生生物包括鱼、鱿鱼、海豚、蝾螈等。",
                    "Aquatic mobs include fish, squid, dolphins, axolotls, etc.",
                    "",
                    "Default: true / 默认：true"
            )
            .define("aquaticMobFlee", true);

    public static final ModConfigSpec.BooleanValue FLOCK_FLEE = BUILDER
            .comment(
                    "逃跑时是否启用群体行为（Boids 群聚算法）",
                    "Whether fleeing mobs use flocking behavior (Boids algorithm)",
                    "",
                    "开启后，逃跑中的同类生物会互相分离、对齐、聚合，",
                    "受惊的鱼群会成群结队地逃窜，而不是各自盲目乱跑。",
                    "When enabled, fleeing mobs of the same species separate, align and cohere,",
                    "so a startled school scatters together instead of each one running blindly.",
                    "",
                    "逃跑方向始终优先：群聚力只作为转向扰动叠加，",
                    "若群聚方向与逃跑方向相反，其影响会被大幅削弱，绝不会把生物拉回玩家身边。",
                    "The flee direction always takes priority. Flocking is only a steering nudge,",
                    "and any flock force opposing the flee direction is heavily damped,",
                    "so it can never pull a mob back toward the player.",
                    "",
                    "Default: true / 默认：true"
            )
            .define("flockFlee", true);

    public static final ModConfigSpec.BooleanValue FISHING_REQUIRES_NATURAL_WATER = BUILDER
            .comment(
                    "钓鱼是否要求自然水域",
                    "Whether fishing requires natural water bodies",
                    "",
                    "开启后，只有在河流、海洋、海滩生物群系，",
                    "或者连通水面达到一定大小的自然湖泊/池塘中才能钓到鱼。",
                    "When enabled, fish can only be caught in river, ocean or beach biomes,",
                    "or in natural lakes/ponds whose connected water surface is large enough.",
                    "",
                    "玩家自己挖的小坑、人造水池无论等多久都钓不到鱼。",
                    "Small pits or artificial pools dug by players will never yield fish.",
                    "",
                    "Default: true / 默认：true"
            )
            .define("fishingRequiresNaturalWater", true);

    public static final ModConfigSpec.IntValue FISHING_MIN_WATER_VOLUME = BUILDER
            .comment(
                    "自然湖泊最小连通水方块数量（格）",
                    "Minimum connected water blocks for a natural lake",
                    "",
                    "当不在河流/海洋/海滩生物群系时，钩子周围连通的源水方块达到该数量才视为自然湖泊。",
                    "Outside river/ocean/beach biomes, the connected source water blocks around the hook",
                    "must reach this count to be treated as a natural lake.",
                    "",
                    "Default: 64 / 默认：64",
                    "Range: 16 ~ 1024"
            )
            .defineInRange("fishingMinWaterVolume", 64, 16, 1024);

    public static final ModConfigSpec.IntValue FISH_FISH_SCAN_RADIUS = BUILDER
            .comment(
                    "钓鱼时扫描鱼实体的半径（格）",
                    "Radius to scan for fish entities when fishing",
                    "",
                    "鱼漂周围这个范围内必须有真实的鱼实体才能钓到鱼，",
                    "每钓上一条鱼，范围内就会减少一条鱼实体，直到钓完。",
                    "Fishing only works when real fish entities exist within this radius around the hook;",
                    "each caught fish removes one fish entity from the area until none remain.",
                    "",
                    "Default: 16 / 默认：16",
                    "Range: 4 ~ 64"
            )
            .defineInRange("fishScanRadius", 16, 4, 64);

    public static final ModConfigSpec.BooleanValue HUNGER_ENABLED = BUILDER
            .comment(
                    "是否启用动物饥饿系统",
                    "Whether the animal hunger system is enabled",
                    "",
                    "开启后，被玩家喂养过的家养动物会随时间消耗饥饿值。",
                    "When enabled, domesticated animals (fed once by a player) lose hunger over time.",
                    "",
                    "Default: true / 默认：true"
            )
            .define("hungerEnabled", true);

    public static final ModConfigSpec.IntValue HUNGER_DURATION_MINUTES = BUILDER
            .comment(
                    "家养动物饥饿值从满降到 0 所需时间（分钟）",
                    "Minutes for a domesticated animal's hunger to drop from full to zero",
                    "",
                    "按游戏内时间计算，受时间加速模组影响。",
                    "Measured in in-game minutes, so time-acceleration mods affect it.",
                    "",
                    "Default: 60 / 默认：60",
                    "Range: 5 ~ 1440"
            )
            .defineInRange("hungerMinutes", 60, 5, 1440);

    public static final ModConfigSpec.IntValue HUNGER_STARVE_DAYS = BUILDER
            .comment(
                    "饥饿值归零后，还能存活的天数",
                    "Days a domesticated animal survives after hunger reaches zero",
                    "",
                    "归零后动物不会立刻死亡，而是进入挨饿状态缓慢掉血，",
                    "在这段时间内喂食可以救回来。",
                    "After hunger reaches zero the animal does not die instantly;",
                    "it enters a starving state and slowly loses health,",
                    "and can be saved by feeding it during this window.",
                    "",
                    "Default: 3 / 默认：3",
                    "Range: 1 ~ 30"
            )
            .defineInRange("hungerStarveDays", 3, 1, 30);

    public static final ModConfigSpec.IntValue COW_PREGNANCY_MINUTES = BUILDER
            .comment(
                    "牛基准孕期（分钟）",
                    "Base cow pregnancy duration in minutes",
                    "",
                    "其它生物按体型等比例缩放。",
                    "Other animals scale this by body size.",
                    "",
                    "Default: 120 / 默认：120",
                    "Range: 5 ~ 1440"
            )
            .defineInRange("cowPregnancyMinutes", 120, 5, 1440);

    public static final ModConfigSpec.IntValue PREGNANCY_FLOAT_MINUTES = BUILDER
            .comment(
                    "孕期浮动（分钟）",
                    "Pregnancy duration random float in minutes",
                    "",
                    "实际孕期在基准值上额外加上 0~浮动值 之间的随机分钟。",
                    "Actual duration adds a random 0~float minutes on top of the base.",
                    "",
                    "Default: 20 / 默认：20",
                    "Range: 0 ~ 720"
            )
            .defineInRange("pregnancyFloatMinutes", 20, 0, 720);

    public static final ModConfigSpec.DoubleValue MISCARRIAGE_CHANCE = BUILDER
            .comment(
                    "怀孕期间受伤导致流产的概率",
                    "Chance of miscarriage when a pregnant animal is hurt",
                    "",
                    "0.0 = 永不流产，1.0 = 必然流产。",
                    "0.0 = never, 1.0 = always.",
                    "",
                    "Default: 0.2 / 默认：0.2",
                    "Range: 0.0 ~ 1.0"
            )
            .defineInRange("miscarriageChance", 0.2, 0.0, 1.0);

    public static final ModConfigSpec.IntValue COOLDOWN_MINUTES = BUILDER
            .comment(
                    "产后冷却基准（分钟）",
                    "Post-birth/post-miscarriage cooldown in minutes",
                    "",
                    "生产或流产后，动物在该时间内无法再次怀孕。",
                    "After giving birth or miscarrying, the animal cannot become pregnant again for this long.",
                    "",
                    "Default: 120 / 默认：120",
                    "Range: 1 ~ 1440"
            )
            .defineInRange("cooldownMinutes", 120, 1, 1440);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> OVIPAROUS_MOBS = BUILDER
            .comment(
                    "卵生动物列表（实体ID列表）",
                    "List of oviparous mob IDs",
                    "",
                    "这些动物生产时生成受精蛋物品而不是直接生成幼年实体。",
                    "These animals lay fertilized eggs instead of giving live birth.",
                    "",
                    "Default: [\"minecraft:chicken\"]",
                    "格式：每个ID用双引号包裹，逗号分隔。",
                    "Format: comma-separated quoted IDs."
            )
            .defineListAllowEmpty("oviparousMobs", List.of(
                    "minecraft:chicken"
            ), () -> "", obj -> obj instanceof String);

    public static final ModConfigSpec.BooleanValue BLOCK_NORMAL_EGG_HATCHING = BUILDER
            .comment(
                    "是否阻止普通鸡蛋扔出后孵化小鸡",
                    "Whether to block normal eggs from hatching when thrown",
                    "",
                    "开启后，普通鸡蛋扔出不再随机孵出小鸡；",
                    "只有由怀孕卵生动物产下的受精蛋才能孵出小鸡。",
                    "When enabled, normal eggs no longer randomly hatch when thrown;",
                    "only fertilized eggs laid by pregnant oviparous animals can hatch.",
                    "",
                    "Default: true / 默认：true"
            )
            .define("blockNormalEggHatching", true);

    public static final ModConfigSpec.IntValue EGG_INCUBATION_MINUTES = BUILDER
            .comment(
                    "受精蛋孵化所需的游戏内时间（分钟）",
                    "In-game minutes required for a fertilized egg to hatch",
                    "",
                    "受精蛋必须放在「不能见天空的干草块」上才会孵化；",
                    "离开干草块或重见天空会重置进度。",
                    "1 MC 天 = 20 分钟。",
                    "A fertilized egg only hatches when placed on a hay block that cannot see the sky;",
                    "moving it off the hay block or exposing it to the sky resets the progress.",
                    "1 MC day = 20 minutes.",
                    "",
                    "Default: 20 (1 MC day) / 默认：20（1 MC 天）",
                    "Range: 1 ~ 1440"
            )
            .defineInRange("eggIncubationMinutes", 20, 1, 1440);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_MOBS = BUILDER
            .comment(
                    "不会逃跑的生物黑名单（实体ID列表）",
                    "Blacklist of mob IDs that will never flee.",
                    "",
                    "这些生物即使属于被动/水生类别也不会逃跑。",
                    "Mobs in this list will not flee even if they are passive/aquatic.",
                    "",
                    "Default: [\"minecraft:wolf\", \"minecraft:cat\", \"minecraft:parrot\", \"minecraft:horse\", ...]",
                    "格式：每个ID用双引号包裹，逗号分隔。",
                    "Format: comma-separated quoted IDs."
            )
            .defineListAllowEmpty("blacklistedMobs", List.of(
                    "minecraft:wolf",
                    "minecraft:parrot",
                    "minecraft:donkey",
                    "minecraft:mule",
                    "minecraft:skeleton_horse",
                    "minecraft:zombie_horse",
                    "minecraft:llama",
                    "minecraft:trader_llama",
                    "minecraft:ocelot",
                    "minecraft:dolphin",
                    "minecraft:polar_bear",
                    "minecraft:bee",
                    "minecraft:axolotl",
                    "minecraft:panda",
                    "minecraft:iron_golem",
                    "minecraft:snow_golem"
            ), () -> "", obj -> obj instanceof String);

    public static final ModConfigSpec SPEC = BUILDER.build();
}