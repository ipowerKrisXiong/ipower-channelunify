package com.ipower.cloud.channelunify.domain.event;


import com.club.clubmanager.domain.event.dto.BarAreaChangeEventParams;
import com.ipower.cloud.channelunify.domain.event.dto.SampleEventParams;
import com.mars.ddd.core.event.Event;

/**
 * @Author xionglin
 * @create 2022/9/7 15:26
 */
public class SampleChangeEvent extends Event<SampleEventParams> {

    public SampleChangeEvent(BarAreaChangeEventParams body) {
        super(SampleChangeEvent.class.getName(), body);
    }

    public static SampleChangeEvent build(BarAreaChangeEventParams body){
        return new SampleChangeEvent(body);
    }

}
