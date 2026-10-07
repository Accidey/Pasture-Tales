package com.xulai.animalflee.mixin;

import com.xulai.animalflee.logic.GenderLogic;
import com.xulai.animalflee.logic.HungerSystem;
import com.xulai.animalflee.logic.PregnancySystem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chicken.class)
public class ChickenEggLayMixin {

    private static final long DAY_TICKS = 24000L;
    private static final long HOUR_TICKS = 1000L;
    private static final long MIN_FLOAT = 1L * HOUR_TICKS;
    private static final long MAX_FLOAT = 3L * HOUR_TICKS;
    private static final long DISPLAY_LIMIT = 48000L;
    private static final int MALE_NO_EGG_TIMER = Integer.MAX_VALUE;
    private static final String NBT_NEXT_EGG = "af_next_egg_daytime";
    private static final String NBT_NEXT_FEATHER = "af_next_feather_daytime";

    @Inject(method = "aiStep()V", at = @At("TAIL"))
    private void animalflee$syncEggTimer(CallbackInfo ci) {
        Chicken self = (Chicken) (Object) this;
        if (self.level().isClientSide) return;
        if (self.isBaby()) return;

        CompoundTag data = self.getPersistentData();
        if (data.getString(GenderLogic.NBT_GENDER).isEmpty()) {
            GenderLogic.initGender(self);
        }
        if (!data.contains(NBT_NEXT_FEATHER)) {
            data.putLong(NBT_NEXT_FEATHER, nextEggInterval(self, self.level().getDayTime()));
        }

        long dayTime = self.level().getDayTime();

        if (GenderLogic.isFemale(self)) {
            if (!data.contains(NBT_NEXT_EGG)) {
                data.putLong(NBT_NEXT_EGG, nextEggInterval(self, dayTime));
            }
            long remain = data.getLong(NBT_NEXT_EGG) - dayTime;
            self.eggTime = (int) Math.min(Math.max(remain, 0L), DISPLAY_LIMIT - 1L);
        } else {
            self.eggTime = MALE_NO_EGG_TIMER;
        }

        if (dayTime < data.getLong(NBT_NEXT_FEATHER)) return;

        data.putLong(NBT_NEXT_FEATHER, nextEggInterval(self, dayTime));
        int count = 1 + self.getRandom().nextInt(2);
        self.spawnAtLocation(new ItemStack(Items.FEATHER, count));
    }

    @Redirect(
            method = "aiStep()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Chicken;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;")
    )
    private ItemEntity animalflee$layEgg(Chicken self, ItemLike item) {
        if (self.level().isClientSide) return self.spawnAtLocation(item);
        if (self.isBaby()) return null;
        if (!GenderLogic.isFemale(self)) return null;

        CompoundTag data = self.getPersistentData();
        long dayTime = self.level().getDayTime();
        if (dayTime < data.getLong(NBT_NEXT_EGG)) return null;

        data.putLong(NBT_NEXT_EGG, nextEggInterval(self, dayTime));

        boolean fertilized = PregnancySystem.canLayFertilizedEgg(self);
        ItemStack egg = new ItemStack(Items.EGG);
        if (fertilized) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean(PregnancySystem.NBT_FERTILIZED_EGG, true);
            tag.putString(PregnancySystem.NBT_SPECIES, "minecraft:chicken");
            tag.putBoolean(PregnancySystem.NBT_OFFSPRING_DOMESTICATED, HungerSystem.isDomesticated(self));
            CustomData.set(DataComponents.CUSTOM_DATA, egg, tag);
        }
        return self.spawnAtLocation(egg);
    }

    private static long nextEggInterval(Chicken self, long dayTime) {
        long range = MAX_FLOAT - MIN_FLOAT + 1L;
        long randomFloat = MIN_FLOAT + self.getRandom().nextInt((int) range);
        return dayTime + DAY_TICKS + randomFloat;
    }
}
