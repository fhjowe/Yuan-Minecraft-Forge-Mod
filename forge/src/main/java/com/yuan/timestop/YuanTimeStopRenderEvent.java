package com.yuan.timestop;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;

public class YuanTimeStopRenderEvent extends TickEvent {
    public final float renderTickTime;

    public YuanTimeStopRenderEvent(TickEvent.Phase phase, float renderTickTime) {
        super(TickEvent.Type.RENDER, LogicalSide.CLIENT, phase);
        this.renderTickTime = renderTickTime;
    }
}
