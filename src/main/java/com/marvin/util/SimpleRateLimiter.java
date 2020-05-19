package com.marvin.util;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
/**
 * @author marvin
 * 滑动窗口限流
 */
public class SimpleRateLimiter {
    /**
     * 循环数组
     */
    private AtomicInteger[] slotCounter;
    /**
     * 时间片允许通过的最大阈值
     */
    private volatile int threshold;
    /**
     * 滑动窗口个数
     */
    private volatile int slideWindowSize;
    /**
     * 每个滑动窗口的时长，以毫秒为单位
     */
    private volatile int timeMillisPerSlice;
    /**
     * 该滑窗的起始创建时间，也就是第一个数据
     */
    private volatile long beginTimestamp;
    /**
     * 当前起始时间片
     */
    private volatile int cursor;
    /**
     * Exclusived lock
     */
    private ReentrantLock lock;

    private SimpleRateLimiter(int slideWindowSize, int timeMillisPerSlice,int threshold){
        this.slideWindowSize = slideWindowSize;
        this.threshold = threshold;
        this.timeMillisPerSlice = timeMillisPerSlice;
        lock = new ReentrantLock();
        reset();
    }
    /**
     * all request count
     * @return
     */
    private int totalCount(){
        return Arrays.stream(slotCounter).mapToInt(slotCounter -> slotCounter.get()).sum();
    }
    public void wipeSlot(int slotSize) {
        slotCounter[slotSize].set(0);
    }
    private int locationIndex(){
        long now = System.currentTimeMillis();
        long time = now-beginTimestamp;
        if(time > timeMillisPerSlice*slideWindowSize){
            advance();
        }
        int result = cursor+(int)(((now-beginTimestamp)/timeMillisPerSlice));
        if(result >= slideWindowSize) {
            result = result % slideWindowSize;
        }
        return result;
    }

    /**
     * 向前滑动
     */
    private void advance(){
        lock.lock();
        try {
            long now = System.currentTimeMillis();
            long time = now - beginTimestamp;
            if (time > timeMillisPerSlice * slideWindowSize) {
                //需要滑动
                long diffWindow = time / timeMillisPerSlice;
                if (diffWindow >= slideWindowSize * 2) {
                    reset();
                    return;
                }
                int i;
                for (i = 0; i <= diffWindow - slideWindowSize; i++) {
                    int index = (cursor + 1)%slideWindowSize;
                    wipeSlot(cursor);
                    this.beginTimestamp = this.beginTimestamp + timeMillisPerSlice;
                    cursor = index;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void reset(){
        beginTimestamp = System.currentTimeMillis();
        //窗口个数
        AtomicInteger[] localTimeSlices = new AtomicInteger[slideWindowSize];
        for (int i = 0; i < slideWindowSize; i++) {
            localTimeSlices[i] = new AtomicInteger(0);
        }
        slotCounter = localTimeSlices;
        cursor = 0;
    }

    public boolean tryAcquire(int arg){
        int slot = locationIndex();
        if(this.totalCount()+arg<=threshold){
            slotCounter[slot].getAndAdd(arg);
            return true;
        }
        return false;
    }
}
