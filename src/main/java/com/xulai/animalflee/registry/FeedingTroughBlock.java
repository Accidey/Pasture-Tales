package com.xulai.animalflee.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.xulai.animalflee.Config;
import com.xulai.animalflee.logic.AnimalFoods;

import java.util.List;

public class FeedingTroughBlock extends Block implements EntityBlock {

    public static final MapCodec<FeedingTroughBlock> CODEC = simpleCodec(FeedingTroughBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_COMPOSTER;
    public static final BooleanProperty CAP_NORTH = BooleanProperty.create("cap_north");
    public static final BooleanProperty CAP_EAST = BooleanProperty.create("cap_east");
    public static final BooleanProperty CAP_SOUTH = BooleanProperty.create("cap_south");
    public static final BooleanProperty CAP_WEST = BooleanProperty.create("cap_west");
    private static final VoxelShape SHAPE = Block.box(0, 0, 3, 16, 6, 13);

    public FeedingTroughBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(LEVEL, 0)
                .setValue(CAP_NORTH, true)
                .setValue(CAP_EAST, true)
                .setValue(CAP_SOUTH, true)
                .setValue(CAP_WEST, true));
    }

    @Override
    protected MapCodec<? extends FeedingTroughBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FeedingTroughBlockEntity(pos, state);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (direction.getAxis().isHorizontal()) {
            state = state.setValue(capFor(direction), capClosed(state, direction, level, neighborPos));
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockState state = defaultBlockState().setValue(FACING, facing);
        for (Direction direction : List.of(facing.getClockWise(), facing.getCounterClockWise())) {
            state = state.setValue(capFor(direction), capClosed(state, direction, context.getLevel(), context.getClickedPos().relative(direction)));
        }
        return state;
    }

    private static boolean capClosed(BlockState state, Direction capDirection, LevelAccessor level, BlockPos neighborPos) {
        if (capDirection.getAxis() == state.getValue(FACING).getAxis()) return true;
        BlockState neighbor = level.getBlockState(neighborPos);
        return !(neighbor.getBlock() instanceof FeedingTroughBlock
                && neighbor.getValue(FACING).getAxis() == state.getValue(FACING).getAxis());
    }

    private static BooleanProperty capFor(Direction direction) {
        return switch (direction) {
            case NORTH -> CAP_NORTH;
            case EAST -> CAP_EAST;
            case SOUTH -> CAP_SOUTH;
            default -> CAP_WEST;
        };
    }

    private static boolean isFood(ItemStack stack) {
        if (Config.TROUGH_ITEM_BLACKLIST.get()
                .contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
            return false;
        }
        return stack.has(DataComponents.FOOD) || AnimalFoods.getSaturation(stack) > 0.0F;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough && isFood(stack)) {
            ItemStack remaining = trough.insert(stack);
            if (remaining.getCount() != stack.getCount()) {
                player.setItemInHand(hand, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
                trough.syncLevel();
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            return ItemInteractionResult.FAIL;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
            boolean removed = false;
            for (ItemStack taken : trough.extractAll()) {
                trough.syncLevel();
                if (!player.getInventory().add(taken)) {
                    player.drop(taken, false);
                }
                removed = true;
            }
            if (removed) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
            for (ItemStack stack : trough.getItems()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LEVEL, CAP_NORTH, CAP_EAST, CAP_SOUTH, CAP_WEST);
    }
}
