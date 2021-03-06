/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.modules;

import mods.railcraft.api.core.RailcraftModule;
import mods.railcraft.common.blocks.RailcraftBlocks;

@RailcraftModule("railcraft:tracks|reinforced")
public class ModuleTracksReinforced extends RailcraftModulePayload {

    public ModuleTracksReinforced() {
        setEnabledEventHandler(new ModuleEventHandler() {
            @Override
            public void construction() {
                add(
                        RailcraftBlocks.TRACK_FLEX_REINFORCED
//                        RailcraftBlocks.TRACK_JUNCTION_REINFORCED
                );
//                add(RailcraftBlocks.track);
            }

            @Override
            public void preInit() {
//                TrackKits.REINFORCED.register();
//                TrackKits.REINFORCED_BOOSTER.register();
//                TrackKits.REINFORCED_JUNCTION.register();
//                TrackKits.REINFORCED_SWITCH.register();
//                TrackKits.REINFORCED_WYE.register();
            }
        });
    }
}
