package com.marvin.util;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author marvin
 */
public class SimpleRateLimiter {
    /**
     * request counter
     */
    private AtomicInteger counter = new AtomicInteger();
    /**
     * start time
     */
    private long nowTime = System.currentTimeMillis();
    /**
     * 循环数组
     */
    private AtomicInteger[] slotCounter;
    /**
     * 时间片允许通过的最大阈值
     */
    private int threshold;
    /**
     * 滑动窗口个数
     */
    private volatile int slideWindowSize;
    /**
     * 每个滑动窗口的时长，以毫秒为单位
     */
    private int timeMillisPerSlice;
    /**
     * 该滑窗的起始创建时间，也就是第一个数据
     */
    private long beginTimestamp;
    /**
     * 最后一个数据的时间戳
     */
    private long lastAddTimestamp;
    /**
     * 当前起始时间片
     */
    private volatile int cursor;

    private boolean counterLimiter(){

        return false;
    }
    /**
     * all request count
     * @return
     */
    private int totalCount(){
        return Arrays. stream(slotCounter).mapToInt(slotCounter -> slotCounter.get()).sum();
    }
    public void wipeSlot(int slotSize) {
        slotCounter[slotSize].set(0);
    }
    public void increaseSlot(int slotSize) {
        slotCounter[slotSize].incrementAndGet();
    }

    private int locationIndex(){
        long now = System.currentTimeMillis();
        return (int)((now-beginTimestamp)/timeMillisPerSlice)%slideWindowSize;
    }

    public boolean grant(){

        return false;
    }

    public static void main(String[] args) {

    }
}
