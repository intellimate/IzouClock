package jundl77.izou.izouclock;

import org.intellimate.izou.sdk.Context;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A ScheduledThreadPoolExecutor that logs exceptions and does not just drop them
 */
public class LoggedScheduledExecutor extends ScheduledThreadPoolExecutor {
    private Context context;

    /**
     * Creates a new LoggedScheduledExecutor object
     *
     * @param context The context of the addOn
     * @param corePoolSize the core pool size of the executor, for more info see the oracle documentation for
     *                     ScheduledThreadPoolExecutor
     */
    public LoggedScheduledExecutor(Context context, int corePoolSize) {
        super(corePoolSize);
        this.context = context;
    }

    @Override
    public ScheduledFuture scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return super.scheduleAtFixedRate(wrapRunnable(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return super.scheduleWithFixedDelay(wrapRunnable(command), initialDelay, delay, unit);
    }

    private Runnable wrapRunnable(Runnable command) {
        return new LogOnExceptionRunnable(command);
    }

    private class LogOnExceptionRunnable implements Runnable {
        private Runnable theRunnable;

        public LogOnExceptionRunnable(Runnable theRunnable) {
            super();
            this.theRunnable = theRunnable;
        }

        @Override
        public void run() {
            try {
                theRunnable.run();
            } catch (Exception e) {
                // Log it here
                context.getLogger().error("error in executing: " + theRunnable
                        + " - It will no longer be run!", e);

                // Re-throw error so that the Executor also gets the error so that it can continue its expected behavior
                throw new RuntimeException(e);
            }
        }
    }
}