package com.xulai.animalflee.logic;

import com.xulai.animalflee.AnimalFleeMod;
import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.boid.BoidsSimulation;
import com.xulai.animalflee.logic.boid.NearbyMobTracker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = AnimalFleeMod.MODID)
public class MobFleeLogic {

    private static final String NBT_FLEE_TARGET_X = "AF_FleeTargetX";
    private static final String NBT_FLEE_TARGET_Z = "AF_FleeTargetZ";
    private static final String NBT_FLEE_SOURCE_UUID = "AF_FleeSourceUUID";
    private static final String NBT_FLEE_TICKS = "AF_FleeTicks";
    private static final String NBT_FLEE_STUCK_TICKS = "AF_FleeStuckTicks";
    private static final String NBT_FLEE_REPATH_FAILS = "AF_FleeRepathFails";
    private static final String NBT_FLOCK_X = "AF_FlockX";
    private static final String NBT_FLOCK_Z = "AF_FlockZ";
    private static final String NBT_FLOCK_TICK = "AF_FlockTick";
    private static final String NBT_FLEE_LAST_X = "AF_FleeLastX";
    private static final String NBT_FLEE_LAST_Z = "AF_FleeLastZ";
    private static final String NBT_FLEE_ACTIVE = "AF_FleeActive";
    private static final String NBT_ORIGINAL_NO_AI = "AF_OriginalNoAi";
    private static final String STEP_MODIFIER_ID = "animalflee:flee_step_up";
    private static final double STEP_UP_BONUS = 0.4;
    private static final int MAX_FLEE_TICKS = 1200;
    private static final int STUCK_REPATH_TICKS = 8;
    private static final int MAX_REPATH_FAILS = 5;
    private static final double FLEE_SAFE_DISTANCE = 32.0;
    private static final double FLEE_PATH_CHECK_DIST = 3.0;
    private static final int FLEE_MIN_DIST = 24;
    private static final int FLEE_MAX_DIST = 40;
    private static final int ENCLOSE_CHECK_RANGE = 4;
    private static final int ENCLOSE_BLOCKED_DIRS = 4;
    private static final double PANIC_SPEED_FAR = 0.34;
    private static final double PANIC_SPEED_NEAR = 0.48;
    private static final double SWIM_UP_SPEED = 0.18;
    private static final double SWIM_SPEED_FACTOR = 0.6;
    private static final double STUCK_THRESHOLD_LAND = 0.05;
    private static final double STUCK_THRESHOLD_WATER = 0.02;
    private static final int FLOCK_RECALC_INTERVAL = 10;
    private static final double FLOCK_SEARCH_RANGE = 6.0;
    private static final double FLOCK_INFLUENCE = 0.45;
    private static final double FLOCK_CONTRARY_FACTOR = 0.25;
    private static final BoidsSimulation PANIC_FLOCK = new BoidsSimulation(
            0.8f,
            2.5f,
            BoidsSimulation.degrees(70f),
            0.6f,
            BoidsSimulation.degrees(100f),
            0.15f,
            BoidsSimulation.degrees(70f),
            0.02f
    );

    private static Set<String> cachedBlacklist = null;

    private static Set<String> getBlacklist() {
        if (cachedBlacklist == null) {
            cachedBlacklist = new HashSet<>(Config.BLACKLISTED_MOBS.get());
        }
        return cachedBlacklist;
    }

    public static void resetBlacklistCache() {
        cachedBlacklist = null;
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (entity instanceof ServerPlayer player) {
            if (shouldIgnorePlayer(player)) return;
            if (player.tickCount % 10 != 0) return;
            double triggerDist = Config.FLEE_TRIGGER_DISTANCE.get();
            AABB box = player.getBoundingBox().inflate(triggerDist + 4.0);
            List<Mob> mobs = player.level().getEntitiesOfClass(Mob.class, box, Mob::isAlive);
            for (Mob mob : mobs) {
                if (shouldFlee(mob)) {
                    processFlee(mob, player, triggerDist);
                }
            }
            return;
        }

        if (entity instanceof Mob mob) {
            tickFlee(mob);
        }
    }

    private static boolean isBlacklisted(Mob mob) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (key == null) return false;
        return getBlacklist().contains(key.toString());
    }

    private static boolean shouldIgnorePlayer(Player player) {
        return player.isCreative() || player.isSpectator();
    }

    private static boolean shouldFlee(Mob mob) {
        if (HungerSystem.isDomesticated(mob)) return false;
        if (isBlacklisted(mob)) return false;
        if (isEnclosed(mob)) return false;
        if (isSubmerged(mob)) return false;
        boolean flee = false;
        if (mob instanceof Animal || mob instanceof AmbientCreature) {
            flee |= Config.PASSIVE_MOB_FLEE.get();
        }
        if (isAquatic(mob)) {
            flee |= Config.AQUATIC_MOB_FLEE.get();
        }
        return flee;
    }

    private static boolean isEnclosed(Mob mob) {
        int blockedDirs = 0;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : dirs) {
            if (!canEscapeDir(mob, dir[0], dir[1])) {
                blockedDirs++;
            }
        }
        return blockedDirs >= ENCLOSE_BLOCKED_DIRS;
    }

    private static boolean canEscapeDir(Mob mob, int dx, int dz) {
        for (int step = 1; step <= ENCLOSE_CHECK_RANGE; step++) {
            int bx = mob.getBlockX() + dx * step;
            int bz = mob.getBlockZ() + dz * step;
            int by = mob.getBlockY();
            BlockPos feet = new BlockPos(bx, by, bz);
            BlockPos head = feet.above();
            BlockState feetState = mob.level().getBlockState(feet);
            BlockState headState = mob.level().getBlockState(head);
            if (!headState.getCollisionShape(mob.level(), head).isEmpty()) {
                return false;
            }
            if (!feetState.getCollisionShape(mob.level(), feet).isEmpty()
                    && !isOneBlockStep(mob, feet, feetState)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isOneBlockStep(LivingEntity entity, BlockPos feet, BlockState feetState) {
        VoxelShape shape = feetState.getCollisionShape(entity.level(), feet);
        if (shape.isEmpty()) return false;
        double stepTop = shape.max(Direction.Axis.Y) + feet.getY();
        return stepTop - entity.getY() <= 1.0 + 1.0E-3;
    }

    private static boolean isStrandedInWater(Mob mob) {
        return mob.isInWater() && !isAquatic(mob);
    }

    private static boolean isSubmerged(Mob mob) {
        return isStrandedInWater(mob) && isHeadUnderWater(mob);
    }

    private static boolean isHeadUnderWater(LivingEntity entity) {
        return entity.level().getFluidState(BlockPos.containing(entity.getEyePosition())).is(FluidTags.WATER);
    }

    private static boolean isAquatic(Mob mob) {
        if (mob instanceof WaterAnimal) return true;
        MobCategory category = mob.getType().getCategory();
        return category == MobCategory.AXOLOTLS
                || category == MobCategory.UNDERGROUND_WATER_CREATURE
                || category == MobCategory.WATER_CREATURE;
    }

    public static void processFlee(Mob mob, Player player, double triggerDist) {
        CompoundTag data = mob.getPersistentData();
        if (data.getBoolean(NBT_FLEE_ACTIVE)) return;
        if (shouldIgnorePlayer(player)) return;
        if (isEnclosed(mob)) return;
        if (isSubmerged(mob)) return;
        if (distanceXZ(mob, player) > triggerDist) return;

        double[] path = pickFleeTarget(mob, player);

        data.putDouble(NBT_FLEE_TARGET_X, path[0]);
        data.putDouble(NBT_FLEE_TARGET_Z, path[1]);
        data.putString(NBT_FLEE_SOURCE_UUID, player.getStringUUID());
        data.putInt(NBT_FLEE_TICKS, 0);
        data.putInt(NBT_FLEE_STUCK_TICKS, 0);
        data.putInt(NBT_FLEE_REPATH_FAILS, 0);
        data.putInt(NBT_FLOCK_TICK, 0);
        data.putDouble(NBT_FLOCK_X, 0.0);
        data.putDouble(NBT_FLOCK_Z, 0.0);
        data.putBoolean(NBT_FLEE_ACTIVE, true);
        data.putBoolean(NBT_ORIGINAL_NO_AI, mob.isNoAi());
        mob.setNoAi(true);
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        mob.getNavigation().stop();
        addStepUpModifier(mob);
    }

    public static void tickFlee(Mob mob) {
        CompoundTag data = mob.getPersistentData();
        if (!data.getBoolean(NBT_FLEE_ACTIVE)) return;

        if (isEnclosed(mob)) {
            stopFlee(mob);
            return;
        }

        if (isSubmerged(mob)) {
            mob.setDeltaMovement(0.0, SWIM_UP_SPEED, 0.0);
            mob.hurtMarked = true;
            mob.move(MoverType.SELF, mob.getDeltaMovement());
            return;
        }

        Player player = findPlayer(mob, data.getString(NBT_FLEE_SOURCE_UUID));
        double triggerDist = Config.FLEE_TRIGGER_DISTANCE.get();
        double safeDist = safeDistance(triggerDist);
        if (player == null || !player.isAlive() || shouldIgnorePlayer(player)
                || distanceXZ(mob, player) > safeDist) {
            stopFlee(mob);
            return;
        }

        int ticks = data.getInt(NBT_FLEE_TICKS) + 1;
        data.putInt(NBT_FLEE_TICKS, ticks);
        if (ticks >= MAX_FLEE_TICKS) {
            stopFlee(mob);
            return;
        }

        double targetX = data.getDouble(NBT_FLEE_TARGET_X);
        double targetZ = data.getDouble(NBT_FLEE_TARGET_Z);
        double dx = targetX - mob.getX();
        double dz = targetZ - mob.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        boolean needRepath = false;

        if (dist < 1.0) {
            needRepath = true;
        } else if (distanceToSegment(mob.getX(), mob.getZ(), targetX, targetZ, player.getX(), player.getZ()) < FLEE_PATH_CHECK_DIST) {
            needRepath = true;
        }

        if (needRepath) {
            if (distanceXZ(mob, player) > safeDist) {
                stopFlee(mob);
                return;
            }
            double[] path = pickFleeTarget(mob, player);
            data.putDouble(NBT_FLEE_TARGET_X, path[0]);
            data.putDouble(NBT_FLEE_TARGET_Z, path[1]);
            data.putInt(NBT_FLEE_TICKS, 0);
            targetX = path[0];
            targetZ = path[1];
            dx = targetX - mob.getX();
            dz = targetZ - mob.getZ();
            dist = Math.sqrt(dx * dx + dz * dz);
        }

        if (dist < 0.5) return;

        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        mob.setYRot(yaw);
        mob.setYHeadRot(yaw);
        mob.setYBodyRot(yaw);
        boolean inWater = mob.isInWater();
        float speed = panicSpeed(mob, player, triggerDist);
        if (inWater) speed *= (float) SWIM_SPEED_FACTOR;
        float rad = (float) Math.toRadians(yaw);
        float moveX = -Mth.sin(rad) * speed;
        float moveZ = Mth.cos(rad) * speed;

        if (Config.FLOCK_FLEE.get()) {
            Vec3 steered = applyFlockSteering(mob, data, moveX, moveZ, speed);
            moveX = (float) steered.x;
            moveZ = (float) steered.z;
        }

        Vec3 moveVec = new Vec3(moveX, mob.getDeltaMovement().y, moveZ);
        moveOrBlock(mob, moveVec);
        double currentVy = mob.getDeltaMovement().y;
        if (inWater) {
            mob.setDeltaMovement(mob.getDeltaMovement().x, currentVy * 0.8, mob.getDeltaMovement().z);
        } else if (!mob.isNoGravity()) {
            mob.setDeltaMovement(mob.getDeltaMovement().x, currentVy - 0.08, mob.getDeltaMovement().z);
        }

        double lastX = data.getDouble(NBT_FLEE_LAST_X);
        double lastZ = data.getDouble(NBT_FLEE_LAST_Z);
        double movedXZ = Math.abs(mob.getX() - lastX) + Math.abs(mob.getZ() - lastZ);
        double stuckThreshold = inWater ? STUCK_THRESHOLD_WATER : STUCK_THRESHOLD_LAND;
        if (movedXZ < stuckThreshold) {
            int stuckTicks = data.getInt(NBT_FLEE_STUCK_TICKS) + 1;
            data.putInt(NBT_FLEE_STUCK_TICKS, stuckTicks);
            if (stuckTicks >= STUCK_REPATH_TICKS) {
                data.putInt(NBT_FLEE_STUCK_TICKS, 0);
                int fails = data.getInt(NBT_FLEE_REPATH_FAILS) + 1;
                if (fails >= MAX_REPATH_FAILS) {
                    stopFlee(mob);
                    return;
                }
                data.putInt(NBT_FLEE_REPATH_FAILS, fails);
                double[] path = pickFleeTarget(mob, player);
                data.putDouble(NBT_FLEE_TARGET_X, path[0]);
                data.putDouble(NBT_FLEE_TARGET_Z, path[1]);
            }
        } else {
            data.putInt(NBT_FLEE_STUCK_TICKS, 0);
            data.putInt(NBT_FLEE_REPATH_FAILS, 0);
        }
        data.putDouble(NBT_FLEE_LAST_X, mob.getX());
        data.putDouble(NBT_FLEE_LAST_Z, mob.getZ());

        mob.setTarget(null);
        mob.setLastHurtByMob(null);
    }

    public static void stopFlee(Mob mob) {
        CompoundTag data = mob.getPersistentData();
        if (!data.getBoolean(NBT_FLEE_ACTIVE)) return;
        boolean originalNoAi = data.getBoolean(NBT_ORIGINAL_NO_AI);
        data.remove(NBT_FLEE_TARGET_X);
        data.remove(NBT_FLEE_TARGET_Z);
        data.remove(NBT_FLEE_SOURCE_UUID);
        data.remove(NBT_FLEE_TICKS);
        data.remove(NBT_FLEE_STUCK_TICKS);
        data.remove(NBT_FLEE_REPATH_FAILS);
        data.remove(NBT_FLOCK_X);
        data.remove(NBT_FLOCK_Z);
        data.remove(NBT_FLOCK_TICK);
        data.remove(NBT_FLEE_LAST_X);
        data.remove(NBT_FLEE_LAST_Z);
        data.remove(NBT_ORIGINAL_NO_AI);
        data.putBoolean(NBT_FLEE_ACTIVE, false);
        mob.setNoAi(originalNoAi);
        removeStepUpModifier(mob);
    }

    private static void addStepUpModifier(Mob mob) {
        AttributeInstance stepHeight = mob.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight == null) return;
        ResourceLocation id = ResourceLocation.parse(STEP_MODIFIER_ID);
        if (stepHeight.hasModifier(id)) return;
        stepHeight.addTransientModifier(new AttributeModifier(
                id,
                STEP_UP_BONUS,
                AttributeModifier.Operation.ADD_VALUE
        ));
    }

    private static void removeStepUpModifier(Mob mob) {
        AttributeInstance stepHeight = mob.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight == null) return;
        stepHeight.removeModifier(ResourceLocation.parse(STEP_MODIFIER_ID));
    }

    private static double safeDistance(double triggerDist) {
        return Math.max(FLEE_SAFE_DISTANCE, triggerDist * 2.0);
    }

    private static float panicSpeed(Mob mob, LivingEntity player, double triggerDist) {
        double dist = distanceXZ(mob, player);
        if (dist >= triggerDist) return (float) PANIC_SPEED_FAR;
        double closeness = 1.0 - dist / triggerDist;
        return (float) (PANIC_SPEED_FAR + closeness * (PANIC_SPEED_NEAR - PANIC_SPEED_FAR));
    }

    private static Vec3 applyFlockSteering(Mob mob, CompoundTag data, float moveX, float moveZ, float speed) {
        int flockTick = data.getInt(NBT_FLOCK_TICK);
        if (flockTick % FLOCK_RECALC_INTERVAL == 0) {
            List<Mob> nearby = NearbyMobTracker.sameTypeWithin(mob, FLOCK_SEARCH_RANGE);
            Vec3 steering = PANIC_FLOCK.apply(mob, nearby);
            data.putDouble(NBT_FLOCK_X, steering.x);
            data.putDouble(NBT_FLOCK_Z, steering.z);
        }
        data.putInt(NBT_FLOCK_TICK, flockTick + 1);

        double flockX = data.getDouble(NBT_FLOCK_X);
        double flockZ = data.getDouble(NBT_FLOCK_Z);
        if (flockX == 0.0 && flockZ == 0.0) {
            return new Vec3(moveX, 0.0, moveZ);
        }

        double fleeX = moveX / speed;
        double fleeZ = moveZ / speed;
        double steerLen = Math.sqrt(flockX * flockX + flockZ * flockZ);
        double fleeLen = Math.sqrt(fleeX * fleeX + fleeZ * fleeZ);
        if (steerLen < 1.0e-6 || fleeLen < 1.0e-6) {
            return new Vec3(moveX, 0.0, moveZ);
        }

        double dot = (flockX * fleeX + flockZ * fleeZ) / (steerLen * fleeLen);
        double weight = FLOCK_INFLUENCE;
        if (dot < 0.0) {
            weight *= FLOCK_CONTRARY_FACTOR;
        }

        double outX = moveX + flockX * weight;
        double outZ = moveZ + flockZ * weight;
        double outLen = Math.sqrt(outX * outX + outZ * outZ);
        if (outLen > speed * 1.25) {
            double scale = (speed * 1.25) / outLen;
            outX *= scale;
            outZ *= scale;
        }
        return new Vec3(outX, 0.0, outZ);
    }

    private static double distanceXZ(LivingEntity a, LivingEntity b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static double distanceToSegment(double x1, double z1, double x2, double z2, double px, double pz) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        double lenSq = dx * dx + dz * dz;
        if (lenSq < 0.001) return Math.sqrt((px - x1) * (px - x1) + (pz - z1) * (pz - z1));
        double t = ((px - x1) * dx + (pz - z1) * dz) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));
        double projX = x1 + t * dx;
        double projZ = z1 + t * dz;
        return Math.sqrt((px - projX) * (px - projX) + (pz - projZ) * (pz - projZ));
    }

    private static Player findPlayer(LivingEntity self, String uuid) {
        if (uuid.isEmpty()) return null;
        try {
            UUID id = UUID.fromString(uuid);
            if (self.level() instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(id);
                return entity instanceof Player player ? player : null;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
        return null;
    }

    private static boolean isPathClear(LivingEntity entity, double startX, double startZ, double dirX, double dirZ, int distance) {
        for (int i = 1; i <= distance; i++) {
            int bx = (int) Math.floor(startX + dirX * i);
            int bz = (int) Math.floor(startZ + dirZ * i);
            int by = entity.getBlockY();
            BlockPos feet = new BlockPos(bx, by, bz);
            BlockPos head = feet.above();
            BlockState feetState = entity.level().getBlockState(feet);
            BlockState headState = entity.level().getBlockState(head);
            if (!headState.getCollisionShape(entity.level(), head).isEmpty()) {
                return false;
            }
            if (!feetState.getCollisionShape(entity.level(), feet).isEmpty()
                    && !isOneBlockStep(entity, feet, feetState)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBlockedAhead(LivingEntity entity, Vec3 move) {
        if (move.x == 0.0 && move.z == 0.0) return false;
        double len = Math.sqrt(move.x * move.x + move.z * move.z);
        double dx = move.x / len;
        double dz = move.z / len;
        double probe = Math.max(0.6, len + 0.1);

        AABB box = entity.getBoundingBox();
        double reachX = box.maxX + dx * probe;
        double reachZ = box.maxZ + dz * probe;
        double minX = Math.min(box.minX, reachX);
        double maxX = Math.max(box.maxX, reachX);
        double minZ = Math.min(box.minZ, reachZ);
        double maxZ = Math.max(box.maxZ, reachZ);
        int startX = Mth.floor(minX);
        int endX = Mth.floor(maxX);
        int startZ = Mth.floor(minZ);
        int endZ = Mth.floor(maxZ);

        double feetY = box.minY + 0.05;
        double stepTopY = feetY + entity.maxUpStep();
        AABB probeBox = new AABB(minX, feetY, minZ, maxX, stepTopY, maxZ);

        for (int bx = startX; bx <= endX; bx++) {
            for (int bz = startZ; bz <= endZ; bz++) {
                BlockPos pos = new BlockPos(bx, Mth.floor(feetY), bz);
                BlockState state = entity.level().getBlockState(pos);
                if (state.isAir()) continue;
                VoxelShape shape = state.getCollisionShape(entity.level(), pos);
                if (shape.isEmpty()) continue;
                if (!shape.move(pos.getX(), pos.getY(), pos.getZ()).intersects(probeBox)) {
                    continue;
                }
                double top = shape.max(Direction.Axis.Y) + pos.getY();
                if (top > stepTopY) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void moveOrBlock(Mob mob, Vec3 move) {
        if (!isBlockedAhead(mob, move)) {
            mob.setDeltaMovement(move.x, mob.getDeltaMovement().y, move.z);
            mob.hurtMarked = true;
            mob.move(MoverType.SELF, mob.getDeltaMovement());
        } else {
            mob.setDeltaMovement(0.0, mob.getDeltaMovement().y, 0.0);
            mob.hurtMarked = true;
            mob.move(MoverType.SELF, mob.getDeltaMovement());
            CompoundTag data = mob.getPersistentData();
            data.putInt(NBT_FLEE_STUCK_TICKS, STUCK_REPATH_TICKS - 1);
        }
    }

    private static double[] pickFleeTarget(Mob mob, Player player) {
        int fleeDist = FLEE_MIN_DIST + mob.level().random.nextInt(FLEE_MAX_DIST - FLEE_MIN_DIST + 1);
        double toPlayerAngle = Math.atan2(player.getZ() - mob.getZ(), player.getX() - mob.getX());
        double bestAngle = 0;
        int bestClearance = -1;

        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = mob.level().random.nextDouble() * Math.PI * 2;
            double diff = angle - toPlayerAngle;
            while (diff > Math.PI) diff -= Math.PI * 2;
            while (diff < -Math.PI) diff += Math.PI * 2;
            if (Math.abs(diff) < Math.PI / 2) continue;

            double dirX = Math.cos(angle);
            double dirZ = Math.sin(angle);
            if (isPathClear(mob, mob.getX(), mob.getZ(), dirX, dirZ, fleeDist)) {
                return new double[]{mob.getX() + dirX * fleeDist, mob.getZ() + dirZ * fleeDist};
            }
            int clearance = 0;
            for (int d = 1; d <= fleeDist; d++) {
                int bx = (int) Math.floor(mob.getX() + dirX * d);
                int bz = (int) Math.floor(mob.getZ() + dirZ * d);
                BlockPos feet = new BlockPos(bx, mob.getBlockY(), bz);
                BlockState fs = mob.level().getBlockState(feet);
                BlockState hs = mob.level().getBlockState(feet.above());
                boolean headFree = hs.getCollisionShape(mob.level(), feet.above()).isEmpty();
                boolean feetFree = fs.getCollisionShape(mob.level(), feet).isEmpty();
                if (headFree && (feetFree || isOneBlockStep(mob, feet, fs))) {
                    clearance++;
                } else {
                    break;
                }
            }
            if (clearance > bestClearance) {
                bestClearance = clearance;
                bestAngle = angle;
            }
        }

        if (bestClearance > 0) {
            double dirX = Math.cos(bestAngle);
            double dirZ = Math.sin(bestAngle);
            int actualDist = Math.max(1, Math.min(bestClearance, fleeDist));
            return new double[]{mob.getX() + dirX * actualDist, mob.getZ() + dirZ * actualDist};
        }

        double fallbackAngle = toPlayerAngle + Math.PI;
        double fdirX = Math.cos(fallbackAngle);
        double fdirZ = Math.sin(fallbackAngle);
        return new double[]{mob.getX() + fdirX * 6, mob.getZ() + fdirZ * 6};
    }
}