/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.gui.containers;

import mods.railcraft.common.blocks.machine.manipulator.TileItemManipulator;
import mods.railcraft.common.gui.slots.SlotFilter;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerManipulatorCartItem extends ContainerManipulatorCart {

    public TileItemManipulator tile;

    public ContainerManipulatorCartItem(InventoryPlayer inventoryplayer, TileItemManipulator tile) {
        super(inventoryplayer, tile);
        this.tile = tile;

        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 3; k++) {
                addSlot(new SlotFilter(tile.getItemFilters(), k + i * 3, 8 + k * 18, 26 + i * 18));
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 3; k++) {
                addSlot(tile.getBufferSlot(k + i * 3, 116 + k * 18, 26 + i * 18));
            }
        }

    }

}
