package com.morecruit.ext.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定时管理器，可以定时执行各种任务，另支持高级功能如取消任务，重新计时，暂停、取消等。
 * <p>
 * Created by zhaiyifan on 2015/8/4.
 */
public class TimingManager {
    private final static int WHAT_SCHEDULE_TASK = 1;
    private final static int WHAT_CANCEL_TASK = 2;
    private final static int WHAT_CLEAR_TASK = 3;
    private final static int WHAT_TICK = 4;

    private final static int WHAT_SCHEDULE_INDIE_TASK = 5;
    private final static int WHAT_EXECUTE_INDIE_TASK = 6;

    private final static int WHAT_PAUSE = 7;
    private final static int WHAT_RESUME = 8;
    private final static int WHAT_STOP = 9;

    private final static int STATE_RESUMED = 1;
    private final static int STATE_PAUSED = 2;
    private final static int STATE_STOPPED = 3;

    private final static AtomicInteger sId = new AtomicInteger();

    private final Handler mTimingHandler;
    private final Looper mOwnLooper;
    private final ArrayList<TaskRecord> mTaskList = new ArrayList<TaskRecord>();
    private final HashMap<Integer, TaskRecord> mTaskMap = new HashMap<Integer, TaskRecord>();
    private final HashMap<Integer, TaskRecord> mIndieTaskMap = new HashMap<Integer, TaskRecord>();
    private long mTickPeriod;
    private volatile int mState = STATE_RESUMED;

    private long mLastPauseTime;

    public TimingManager(String name) {
        this(name, android.os.Process.THREAD_PRIORITY_BACKGROUND);
    }

    public TimingManager(String name, int threadPriority) {
        this(createTimingThread(name, threadPriority).getLooper(), true);
    }

    public TimingManager(Looper looper) {
        this(looper, false);
    }

    private TimingManager(Looper looper, boolean ownLooper) {
        mTimingHandler = new Handler(looper, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                handleMessageImpl(msg);
                return true;
            }
        });
        mOwnLooper = ownLooper ? looper : null;
    }

    private static long now() {
        return SystemClock.uptimeMillis();
    }

    private static long computeCommonDivisor(long v1, long v2) {
        long m = 1;
        if (v1 < v2) {
            m = v1;
            v1 = v2;
            v2 = m;
        }
        while (m != 0) {
            m = v1 % v2;
            v1 = v2;
            v2 = m;
        }
        return v1;
    }

    private static TaskRecord createTaskRecord(Runnable runnable, long delay, long interval) {
        return new TaskRecord(sId.getAndIncrement(), runnable, delay, interval);
    }

    private static HandlerThread createTimingThread(String name, int threadPriority) {
        HandlerThread thread = new TimingThread(name, threadPriority);
        thread.start();
        return thread;
    }

    /**
     * Schedule a new tick timing task. Tick means we won't guarantee
     * the first execution of this task (it depends on the global tick).
     *
     * @param runnable task to run.
     * @param period   period of schedule.
     * @return the new timing task id.
     */
    public int schedule(Runnable runnable, long period) {
        return schedule(runnable, 0, period);
    }

    /**
     * Schedule a new tick timing task. Tick means we won't guarantee
     * the first execution of this task (it depends on the global tick).
     *
     * @param runnable task to run.
     * @param delay    delay of first execution.
     * @param period   period of schedule.
     * @return the new timing task id.
     */
    public int schedule(Runnable runnable, long delay, long period) {
        checkNotStopped("Timing is stopped");
        if (period <= 0) {
            throw new IllegalArgumentException("period should be greater than 0");
        }
        if (delay < 0) {
            delay = 0;
        }
        TaskRecord record = createTaskRecord(runnable, delay, period);
        Message msg = Message.obtain();
        msg.what = WHAT_SCHEDULE_TASK;
        msg.obj = record;
        mTimingHandler.sendMessage(msg);
        return record.id;
    }

    /**
     * Schedule a new indie timing task. Indie means this task will has it's own tick rather than the global one.
     *
     * @param runnable task to run.
     * @param period   period of schedule.
     * @return the new timing task id.
     */
    public int scheduleIndie(Runnable runnable, long period) {
        return scheduleIndie(runnable, 0, period);
    }

    /**
     * Schedule a new indie timing task. Indie means this task will has it's own tick rather than the global one.
     *
     * @param runnable task to run.
     * @param delay    delay of first execution.
     * @param period   period of schedule.
     * @return the new timing task id.
     */
    public int scheduleIndie(Runnable runnable, long delay, long period) {
        checkNotStopped("Timing is stopped");
        if (period <= 0) {
            throw new IllegalArgumentException("period should be greater than 0");
        }
        if (delay < 0) {
            delay = 0;
        }
        TaskRecord record = createTaskRecord(runnable, delay, period);
        Message msg = Message.obtain();
        msg.what = WHAT_SCHEDULE_INDIE_TASK;
        msg.obj = record;
        mTimingHandler.sendMessage(msg);
        return record.id;
    }

    /**
     * Cancel a timing task.
     *
     * @param runnable task.
     */
    public void cancel(Runnable runnable) {
        checkNotStopped("Timing is stopped");
        Message msg = Message.obtain();
        msg.what = WHAT_CANCEL_TASK;
        msg.obj = runnable;
        mTimingHandler.sendMessage(msg);
    }

    /**
     * Cancel a timing task.
     *
     * @param id task id.
     */
    public void cancel(int id) {
        checkNotStopped("Timing is stopped");
        Message msg = Message.obtain();
        msg.what = WHAT_CANCEL_TASK;
        msg.arg1 = id;
        mTimingHandler.sendMessage(msg);
    }

    /**
     * Clear all timing tasks.
     */
    public void clear() {
        checkNotStopped("Timing is stopped");
        Message msg = Message.obtain();
        msg.what = WHAT_CLEAR_TASK;
        mTimingHandler.sendMessage(msg);
    }

    /**
     * Pause this timing manager. All scheduled task won't be executed after this.
     */
    public void pause() {
        checkNotStopped("Cannot pause already stopped timing");
        Message msg = Message.obtain();
        msg.what = WHAT_PAUSE;
        mTimingHandler.sendMessageAtFrontOfQueue(msg);
    }

    /**
     * Resume this timing manager.
     */
    public void resume() {
        checkNotStopped("Cannot resume already stopped timing");
        Message msg = Message.obtain();
        msg.what = WHAT_RESUME;
        mTimingHandler.sendMessageAtFrontOfQueue(msg);
    }

    /**
     * Stop this timing manager. All operation is not allowed after this. This operation is irreversible.
     */
    public void stop() {
        checkNotStopped("Timing is already stopped");
        Message msg = Message.obtain();
        msg.what = WHAT_STOP;
        mTimingHandler.sendMessageAtFrontOfQueue(msg);
    }

    /**
     * Whether this timing manager is resumed.
     */
    public final boolean isResumed() {
        return mState == STATE_RESUMED;
    }

    /**
     * Whether this timing manager is completely stopped.
     */
    public final boolean isStopped() {
        return mState == STATE_STOPPED;
    }

    private void handleMessageImpl(Message msg) {
        switch (msg.what) {
            case WHAT_SCHEDULE_TASK:
                performScheduleTask((TaskRecord) msg.obj);
                break;

            case WHAT_CANCEL_TASK:
                if (msg.obj != null) {
                    performCancelTask((Runnable) msg.obj);
                } else {
                    performCancelTask(msg.arg1);
                }
                break;

            case WHAT_CLEAR_TASK:
                performClearTask();
                break;

            case WHAT_TICK:
                if (isResumed()) {
                    // perform tick when resumed.
                    performTick();
                } else {
                    // just continue tick.
                    continueTick();
                }
                break;

            case WHAT_SCHEDULE_INDIE_TASK:
                performScheduleIndieTask((TaskRecord) msg.obj);
                break;

            case WHAT_EXECUTE_INDIE_TASK:
                if (isResumed()) {
                    performIndie(msg.arg1);
                } else {
                    continueIndie(msg.arg1);
                }
                break;

            case WHAT_PAUSE:
                if (updateState(STATE_PAUSED)) {
                    performPause();
                }
                break;

            case WHAT_RESUME:
                if (updateState(STATE_RESUMED)) {
                    performResume();
                }
                break;

            case WHAT_STOP:
                if (updateState(STATE_STOPPED)) {
                    performStop();
                }
                break;
        }
    }

    private void performScheduleTask(TaskRecord record) {
        record.initialTime = now();
        mTaskList.add(record);
        mTaskMap.put(record.id, record);
        // update time unit.
        if (updateTickPeriod(record.period)) {
            // restart tick if period changed.
            restartTick();
        } else {
            // start tick if need.
            startTick();
        }
    }

    private void performScheduleIndieTask(TaskRecord record) {
        record.initialTime = now();
        mIndieTaskMap.put(record.id, record);
        // start indie immediately.
        startIndie(record.id);
    }

    private void performCancelTask(Runnable runnable) {
        Iterator<TaskRecord> iterator = mTaskList.iterator();
        while (iterator.hasNext()) {
            TaskRecord record = iterator.next();
            if (record.runnable.equals(runnable)) {
                iterator.remove();
                mTaskMap.remove(record.id);
            }
        }
        if (mTaskList.isEmpty()) {
            // stop when no task.
            stopTick();
        }
        // indie task.
        Iterator<Map.Entry<Integer, TaskRecord>> indieIterator = mIndieTaskMap.entrySet().iterator();
        while (indieIterator.hasNext()) {
            Map.Entry<Integer, TaskRecord> recordEntry = indieIterator.next();
            if (recordEntry.getValue().runnable.equals(runnable)) {
                indieIterator.remove();
            }
        }
        if (mIndieTaskMap.isEmpty()) {
            // stop when no indie task.
            stopIndie();
        }
    }

    private void performCancelTask(int id) {
        TaskRecord record = mTaskMap.get(id);
        if (record != null && mTaskList.remove(record)) {
            if (mTaskList.isEmpty()) {
                // stop when no task.
                stopTick();
            }
        }
        // indie task.
        if (mIndieTaskMap.remove(id) != null) {
            if (mIndieTaskMap.isEmpty()) {
                // stop when no indie task.
                stopIndie();
            }
        }
    }

    private void performClearTask() {
        mTaskList.clear();
        mTaskMap.clear();
        stopTick();
        // indie task.
        mIndieTaskMap.clear();
        stopIndie();
    }

    private void startTick() {
        if (!mTimingHandler.hasMessages(WHAT_TICK)) {
            mTimingHandler.sendEmptyMessage(WHAT_TICK);
        }
    }

    private void restartTick() {
        mTimingHandler.removeMessages(WHAT_TICK);
        mTimingHandler.sendEmptyMessage(WHAT_TICK);
    }

    private void stopTick() {
        mTimingHandler.removeMessages(WHAT_TICK);
    }

    private void continueTick() {
        mTimingHandler.sendEmptyMessageDelayed(WHAT_TICK, getTickPeriod());
    }

    private void performTick() {
        if (mTaskList.isEmpty()) {
            // stop tick if no task.
            stopTick();
            return;
        }
        // continue to tick before execution to exclude execution cost.
        continueTick();

        long now = now();
        for (TaskRecord record : mTaskList) {
            if ((record.lastExecuteTime == 0 && record.initialTime + record.delay <= now)
                    || (record.lastExecuteTime + record.period <= now)) {
                record.runnable.run();
                record.lastExecuteTime = now;
            }
        }
    }

    private long getTickPeriod() {
        return mTickPeriod;
    }

    private boolean updateTickPeriod(long newPeriod) {
        if (newPeriod <= 0) {
            return false;
        }
        long prev = mTickPeriod;
        if (mTickPeriod <= 0) {
            mTickPeriod = newPeriod;
        } else {
            mTickPeriod = computeCommonDivisor(mTickPeriod, newPeriod);
        }
        return mTickPeriod != prev;
    }

    private void startIndie(int id) {
        TaskRecord record = mIndieTaskMap.get(id);
        if (record == null) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = WHAT_EXECUTE_INDIE_TASK;
        msg.arg1 = record.id;
        mTimingHandler.sendMessageDelayed(msg, record.delay);
    }

    private void restartIndie(int id) {
        Message msg = Message.obtain();
        msg.what = WHAT_EXECUTE_INDIE_TASK;
        msg.arg1 = id;
        mTimingHandler.sendMessage(msg);
    }

    private void stopIndie() {
        mTimingHandler.removeMessages(WHAT_EXECUTE_INDIE_TASK);
    }

    private void continueIndie(int id) {
        TaskRecord record = mIndieTaskMap.get(id);
        if (record == null) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = WHAT_EXECUTE_INDIE_TASK;
        msg.arg1 = record.id;
        mTimingHandler.sendMessageDelayed(msg, record.period);
    }

    private void performIndie(int id) {
        if (mIndieTaskMap.isEmpty()) {
            // stop if no indie task.
            stopIndie();
            return;
        }
        TaskRecord record = mIndieTaskMap.get(id);
        if (record == null) {
            return;
        }
        // continue before execution to exclude execution cost.
        continueIndie(id);

        record.runnable.run();
        record.lastExecuteTime = now();
    }

    private void checkNotStopped(String msg) {
        if (mState == STATE_STOPPED) {
            throw new IllegalStateException(msg);
        }
    }

    private boolean updateState(int state) {
        boolean allow = false;
        switch (state) {
            case STATE_RESUMED:
                if (mState == STATE_STOPPED) {
                    throw new RuntimeException("Cannot resume already stopped timing");
                }
                allow = mState == STATE_PAUSED;
                break;

            case STATE_PAUSED:
                if (mState == STATE_STOPPED) {
                    throw new RuntimeException("Cannot pause already stopped timing");
                }
                allow = mState == STATE_RESUMED;
                break;

            case STATE_STOPPED:
                if (mState == STATE_STOPPED) {
                    throw new RuntimeException("Timing is already stopped");
                }
                allow = mState != STATE_STOPPED;
                break;
        }
        if (allow) {
            mState = state;
        }
        return allow;
    }

    private void performPause() {
        mLastPauseTime = now();
    }

    private void performResume() {
        long now = now();
        if (mLastPauseTime > 0 && mLastPauseTime + mTickPeriod <= now) {
            // if pause period exceeds the tick period, re-start tick.
            restartTick();
        }
        // indie task.
        for (TaskRecord record : mIndieTaskMap.values()) {
            if (mLastPauseTime > 0 && mLastPauseTime + record.period <= now) {
                // if pause period exceeds the indie task period, re-start it.
                restartIndie(record.id);
            }
        }
        mLastPauseTime = 0;
    }

    private void performStop() {
        if (mOwnLooper != null) {
            mOwnLooper.quit();
        }
        // clear tick tasks.
        mTaskList.clear();
        mTaskMap.clear();
        // clear indie tasks.
        mIndieTaskMap.clear();
    }

    /**
     * Task record to track a timing task.
     */
    final static class TaskRecord {
        final int id;
        final Runnable runnable;
        final long delay;
        final long period;
        long initialTime;
        long lastExecuteTime;

        public TaskRecord(int _id, Runnable _runnable, long _delay, long _interval) {
            id = _id;
            runnable = _runnable;
            delay = _delay;
            period = _interval;
            lastExecuteTime = 0;
        }
    }

    /**
     * Own timing thread.
     */
    final static class TimingThread extends HandlerThread {
        public TimingThread(String name, int priority) {
            super(name, priority);
        }
    }
}