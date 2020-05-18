package com.marvin.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @author marvin
 */
public class SimpleRateLimiter {
    /**
     * request counter
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
    private volatile int timeMillisPerSlice;
    /**
     * 该滑窗的起始创建时间，也就是第一个数据
     */
    private volatile long beginTimestamp;

    /**
     * 时间片最后一个数据
     */
    private volatile long lastAddTimestamp ;
    /**
     * 当前起始时间片
     */
    private volatile int cursor;

    private SimpleRateLimiter(int slideWindowSize, int timeMillisPerSlice,int threshold){
//        this.beginTimestamp = System.currentTimeMillis();
        this.lastAddTimestamp = this.beginTimestamp;
        this.slideWindowSize = slideWindowSize;
        this.threshold = threshold;
        this.timeMillisPerSlice = timeMillisPerSlice;
        reset();
    }
    /**
     * all request count
     * @return
     */
    private int totalCount(){
        int sum = 0;
        for (int i = 0; i < slideWindowSize; i++) {
            sum += slotCounter[i].get();
        }
//        return Arrays.stream(slotCounter).mapToInt(slotCounter -> slotCounter.get()).sum();
        return sum;
    }
    public void wipeSlot(int slotSize) {
        slotCounter[slotSize].set(0);
    }
    public void increaseSlot(int slotSize) {
        slotCounter[slotSize].incrementAndGet();
    }

    private int locationIndex(){
        long now = System.currentTimeMillis();
//        if((now-lastAddTimestamp) > timeMillisPerSlice*slideWindowSize){
//            //重置
//            reset();
//        }
        long time = now-beginTimestamp;
        if(time <= timeMillisPerSlice*slideWindowSize){
            //不需要滑动
            return cursor+(int)((time/timeMillisPerSlice)%slideWindowSize);
        } else {
            long diffWindow = time / timeMillisPerSlice;
            if (diffWindow < slideWindowSize * 2) {
                //最大滑动slideWindowSize
                int i;
                for (i = 0; i < diffWindow - slideWindowSize; i++) {
                    int index = cursor + i;
                    if (index > slideWindowSize) {
                        index %= slideWindowSize;
                    }
                    wipeSlot(index);
                    this.beginTimestamp = this.beginTimestamp + timeMillisPerSlice;
                }
            } else {
                reset();
            }
            return cursor+(int)((time/timeMillisPerSlice)%slideWindowSize);
        }

//        double advanceOffset = ((double)time/timeMillisPerSlice);
//        if(advanceOffset> 1){
//            advance(1);
//        }
//        int result = cursor+(int)((time/timeMillisPerSlice)%slideWindowSize);
//        if(result>=slideWindowSize){
//            result = 0;
//        }
//        return result;
    }

    private void reset(){
        beginTimestamp = System.currentTimeMillis();
        //窗口个数
        AtomicInteger[] localTimeSlices = new AtomicInteger[slideWindowSize];
        for (int i = 0; i < slideWindowSize; i++) {
            localTimeSlices[i] = new AtomicInteger(0);
        }
        slotCounter = localTimeSlices;
    }
    public void advance(int offset) {
        int tail = (cursor + offset) % slideWindowSize;
        int temp = tail;
        for(int i = 0;i<offset;i++){
            temp = temp-1;
            if(temp<0){
                temp = slideWindowSize-1;
            }
            wipeSlot(temp);
        }
        cursor = tail;
        this.beginTimestamp = this.beginTimestamp+timeMillisPerSlice*offset;
    }

    public synchronized boolean tryAcquire(int arg){
        int slot = locationIndex();
        int a = slotCounter[slot].getAndAdd(arg);
        if(this.totalCount()<=threshold){
            this.lastAddTimestamp = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public boolean grant(){

        return false;
    }

    public static void main(String[] args) throws Exception{
        SimpleRateLimiter limiter = new SimpleRateLimiter(5,1000,1);

        Thread thread1 = new Thread(()->{
            while(!limiter.tryAcquire(1)){
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +" "+Thread.currentThread().getName()+"pass !");
        },"thread1");
        Thread thread2 = new Thread(()->{
            while(!limiter.tryAcquire(1)){
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +" "+Thread.currentThread().getName()+"pass !");
        },"thread2");
        Thread thread3 = new Thread(()->{
            while(!limiter.tryAcquire(1)){
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +" "+Thread.currentThread().getName()+"pass !");
        },"thread3");
        Thread thread4 = new Thread(()->{
            while(!limiter.tryAcquire(1)){
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +" "+Thread.currentThread().getName()+"pass !");
        },"thread4");
        Thread thread5 = new Thread(()->{
            while(!limiter.tryAcquire(1)){
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +" "+Thread.currentThread().getName()+"pass !");
        },"thread5");
        Thread thread6 = new Thread(()->{
            while(!limiter.tryAcquire(1)){
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +" "+Thread.currentThread().getName()+"pass !");
        },"thread6");
        thread1.start();
        Thread.sleep(500);
        thread2.start();

//        thread3.start();thread4.start();thread5.start();thread6.start();

        Thread.sleep(100000);
    }
}
