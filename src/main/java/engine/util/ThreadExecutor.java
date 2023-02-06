package engine.util;

import org.jetbrains.annotations.NotNull;
import engine.util.logging.Logger;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class ThreadExecutor
{
    private static final Logger LOGGER = Logger.getLogger();
    
    private final Thread thread;
    
    private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
    
    public Runnable onTaskReceived = null;
    
    public ThreadExecutor(@NotNull Thread thread)
    {
        this.thread = thread;
    }
    
    @Override
    public String toString()
    {
        return "ThreadExecutor{" + "thread=" + this.thread + ", tasks=" + this.taskQueue.size() + '}';
    }
    
    public void processQueue()
    {
        if (Thread.currentThread() != this.thread)
        {
            ThreadExecutor.LOGGER.warning("Attempted to process ThreadExecutor from another thread. '%s' != '%s'", Thread.currentThread(), this.thread);
            return;
        }
        Runnable task;
        while ((task = this.taskQueue.poll()) != null) runTask(task);
    }
    
    @NotNull
    private <T extends Runnable> T addTask(@NotNull T task)
    {
        if (Thread.currentThread() == this.thread) return runTask(task);
        this.taskQueue.offer(task);
        if (this.onTaskReceived != null) this.onTaskReceived.run();
        return task;
    }
    
    @NotNull
    protected <T extends Runnable> T runTask(@NotNull T task)
    {
        try
        {
            task.run();
        }
        catch (Exception e)
        {
            ThreadExecutor.LOGGER.severe("An exception occurred while trying to run task.");
            ThreadExecutor.LOGGER.severe(e);
        }
        return task;
    }
    
    @NotNull
    public Future<?> submit(@NotNull Runnable task)
    {
        return addTask(new FutureTask<>(task, null));
    }
    
    @NotNull
    public <T> Future<T> submit(@NotNull Callable<T> task)
    {
        return addTask(new FutureTask<>(task));
    }
}
