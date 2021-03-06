/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.carts;

import mods.railcraft.api.carts.IFluidCart;
import mods.railcraft.common.core.RailcraftConfig;
import mods.railcraft.common.fluids.FluidTools;
import mods.railcraft.common.fluids.Fluids;
import mods.railcraft.common.fluids.TankManager;
import mods.railcraft.common.fluids.tanks.FilteredTank;
import mods.railcraft.common.util.effects.EffectManager;
import mods.railcraft.common.util.inventory.wrappers.InventoryMapper;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.sounds.RailcraftSoundEvents;
import mods.railcraft.common.util.steam.IBoilerContainer;
import mods.railcraft.common.util.steam.Steam;
import mods.railcraft.common.util.steam.SteamBoiler;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class EntityLocomotiveSteam extends EntityLocomotive implements IFluidCart, IBoilerContainer {
    @SuppressWarnings("WeakerAccess")
    public static final int SLOT_LIQUID_INPUT = 0;
    @SuppressWarnings("WeakerAccess")
    public static final int SLOT_LIQUID_OUTPUT = 1;
    private static final byte SMOKE_FLAG = 6;
    private static final byte STEAM_FLAG = 7;
    private static final byte TICKS_PER_BOILER_CYCLE = 2;
    private static final int FUEL_PER_REQUEST = 3;
    public SteamBoiler boiler;
    protected FilteredTank tankWater;
    @SuppressWarnings("WeakerAccess")
    protected FilteredTank tankSteam;
    @SuppressWarnings("WeakerAccess")
    protected InventoryMapper invWaterInput;
    @SuppressWarnings("WeakerAccess")
    protected InventoryMapper invWaterOutput = new InventoryMapper(this, SLOT_LIQUID_OUTPUT, 1);
    private TankManager tankManager = new TankManager();
    private int update = rand.nextInt();

    @SuppressWarnings("WeakerAccess")
    protected EntityLocomotiveSteam(World world) {
        super(world);
    }

    @SuppressWarnings("WeakerAccess")
    protected EntityLocomotiveSteam(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    {
        setAllowedSpeeds(EnumSet.of(LocoSpeed.MAX, LocoSpeed.NORMAL, LocoSpeed.SLOWER, LocoSpeed.SLOWEST, LocoSpeed.REVERSE_SLOWEST));

        tankWater = new FilteredTank(FluidTools.BUCKET_VOLUME * 6) {
            @Override
            public int fillInternal(FluidStack resource, boolean doFill) {
                IBoilerContainer.onFillWater(EntityLocomotiveSteam.this);
                return super.fillInternal(resource, doFill);
            }
        };
        tankWater.setFilter(Fluids.WATER::get);

        tankSteam = new FilteredTank(FluidTools.BUCKET_VOLUME * 16);
        tankSteam.setFilter(Fluids.STEAM::get);
        tankSteam.setCanDrain(false);
        tankSteam.setCanFill(false);

        tankManager.add(tankWater);
        tankManager.add(tankSteam);

        invWaterInput = new InventoryMapper(this, SLOT_LIQUID_INPUT, 1);
        invWaterInput.setStackSizeLimit(4);

        boiler = new SteamBoiler(tankWater, tankSteam);
        boiler.setEfficiencyModifier(RailcraftConfig.steamLocomotiveEfficiencyMultiplier());
        boiler.setTicksPerCycle(TICKS_PER_BOILER_CYCLE);
    }

    @Override
    public SoundEvent getWhistle() {
        return RailcraftSoundEvents.ENTITY_LOCOMOTIVE_STEAM_WHISTLE.getSoundEvent();
    }

    @Nullable
    @Override
    protected ItemStack getCartItemBase() {
        return RailcraftCarts.LOCO_STEAM_SOLID.getStack();
    }

    @Override
    public boolean doInteract(EntityPlayer player, @Nullable ItemStack stack, @Nullable EnumHand hand) {
        return FluidTools.interactWithFluidHandler(stack, getTankManager(), player) || super.doInteract(player, stack, hand);
    }

    public TankManager getTankManager() {
        return tankManager;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return (T) getTankManager();
        return super.getCapability(capability, facing);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (Game.isHost(worldObj)) {
            update++;

            if (tankWater.isEmpty())
                setMode(LocoMode.SHUTDOWN);

            setSteaming(tankSteam.getFluidAmount() > 0);
            if (tankSteam.getRemainingSpace() >= Steam.STEAM_PER_UNIT_WATER || isShutdown()) {
                boiler.tick(1);

                setSmoking(boiler.isBurning());

                if (!boiler.isBurning())
                    ventSteam();
            }

            //FIXME
//            if (update % FluidTools.BUCKET_FILL_TIME == 0)
//                FluidTools.drainContainers(this, this, SLOT_LIQUID_INPUT, SLOT_LIQUID_OUTPUT);
        } else {
            if (isSmoking())
                if (rand.nextInt(3) == 0) {
                    double rads = renderYaw * Math.PI / 180D;
                    float offset = 0.4f;
                    worldObj.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX - Math.cos(rads) * offset, posY + 1.2f, posZ - Math.sin(rads) * offset, 0, 0, 0);
                }
            if (isSteaming())
                EffectManager.instance.steamEffect(worldObj, this, getEntityBoundingBox().minY - posY - 0.3);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isSmoking() {
        return getFlag(SMOKE_FLAG);
    }

    private void setSmoking(boolean smoke) {
        if (getFlag(SMOKE_FLAG) != smoke)
            setFlag(SMOKE_FLAG, smoke);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isSteaming() {
        return getFlag(STEAM_FLAG);
    }

    private void setSteaming(boolean steam) {
        if (getFlag(STEAM_FLAG) != steam)
            setFlag(STEAM_FLAG, steam);
    }

    private void ventSteam() {
        tankSteam.drainInternal(4, true);
    }

    @Nullable
    @Override
    public SteamBoiler getBoiler() {
        return boiler;
    }

    @Override
    public float getTemperature() {
        return (float) boiler.getHeat();
    }

    @Override
    public int getMoreGoJuice() {
        FluidStack steam = tankSteam.getFluid();
        if (steam != null && steam.amount >= tankSteam.getCapacity() / 2) {
            tankSteam.drainInternal(Steam.STEAM_PER_UNIT_WATER, true);
            return FUEL_PER_REQUEST;
        }
        return 0;
//        return 100;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound data) {
        super.writeEntityToNBT(data);
        tankManager.writeTanksToNBT(data);
        boiler.writeToNBT(data);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound data) {
        super.readEntityFromNBT(data);
        tankManager.readTanksFromNBT(data);
        boiler.readFromNBT(data);
    }

    public boolean isSafeToFill() {
        return !boiler.isSuperHeated() || !tankWater.isEmpty();
    }

    @Override
    public boolean canPassFluidRequests(Fluid fluid) {
        return Fluids.WATER.is(fluid);
    }

    @Override
    public boolean canAcceptPushedFluid(EntityMinecart requester, Fluid fluid) {
        return Fluids.WATER.is(fluid);
    }

    @Override
    public boolean canProvidePulledFluid(EntityMinecart requester, Fluid fluid) {
        return false;
    }

    @Override
    public void setFilling(boolean filling) {
    }
}

