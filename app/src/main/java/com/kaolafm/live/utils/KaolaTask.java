package com.kaolafm.live.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.Message;
import android.os.Process;

/**
 * 根据{@link android.os.AsyncTask}
 * 改写，并发执行任务，队列和可运行都满的时候不会抛出RejectedExecutionException; 添加 onExecute，在执行线程返回结果
 * 
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
public abstract class KaolaTask<Params, Progress, Result> {

	private static final int CPU_COUNT = Runtime.getRuntime()
		.availableProcessors();
	private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
	private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
	private static final int KEEP_ALIVE = 1;

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "KaolaTask #" + mCount.getAndIncrement());
		}
	};

	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(
		128);

	/**
	 * An {@link Executor} that can be used to execute tasks in parallel.
	 */
	public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
		CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
		sPoolWorkQueue, sThreadFactory,
		new ThreadPoolExecutor.DiscardOldestPolicy());

	private static final int MESSAGE_POST_RESULT = 0x1;
	private static final int MESSAGE_POST_PROGRESS = 0x2;

	private static final InternalHandler sHandler = new InternalHandler();

	private static volatile Executor sDefaultExecutor = THREAD_POOL_EXECUTOR;
	private final WorkerRunnable<Params, Result> mWorker;
	private final FutureTask<Result> mFuture;

	private volatile Status mStatus = Status.PENDING;

	private final AtomicBoolean mCancelled = new AtomicBoolean();
	private final AtomicBoolean mTaskInvoked = new AtomicBoolean();
	private Object tag;

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}

	public enum Status {
		PENDING, RUNNING, FINISHED,
	}

	public static void init() {
		sHandler.getLooper();
	}

	// public static void setDefaultExecutor(Executor exec) {
	// sDefaultExecutor = exec;
	// }

	public KaolaTask() {
		mWorker = new WorkerRunnable<Params, Result>() {
			public Result call() throws Exception {
				mTaskInvoked.set(true);

				Process
					.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				// noinspection unchecked
				return postResult(doInBackground(mParams));
			}
		};

		mFuture = new FutureTask<Result>(mWorker) {
			@Override
			protected void done() {
				try {
					postResultIfNotInvoked(get());
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
					throw new RuntimeException(
						"An error occured while executing doInBackground()",
						e.getCause());
				} catch (CancellationException e) {
					postResultIfNotInvoked(null);
				}
			}
		};
	}

	private void postResultIfNotInvoked(Result result) {
		final boolean wasTaskInvoked = mTaskInvoked.get();
		if (!wasTaskInvoked) {
			postResult(result);
		}
	}

	private Result postResult(Result result) {
		@SuppressWarnings("unchecked")
		Message message = sHandler.obtainMessage(MESSAGE_POST_RESULT,
			new KaolaTaskResult<Result>(this, result));
		message.sendToTarget();
		onExecute(result);
		return result;
	}

	public final Status getStatus() {
		return mStatus;
	}

	protected abstract Result doInBackground(Params... params);

	protected void onPreExecute() {
	}

	@SuppressWarnings({ "UnusedDeclaration" })
	protected void onPostExecute(Result result) {
	}

	/**
	 * 在线程中返回执行结果
	 * 
	 * @param result
	 */
	protected void onExecute(Result result) {
	}

	@SuppressWarnings({ "UnusedDeclaration" })
	protected void onProgressUpdate(Progress... values) {
	}

	@SuppressWarnings({ "UnusedParameters" })
	protected void onCancelled(Result result) {
		onCancelled();
	}

	protected void onCancelled() {
	}

	public final boolean isCancelled() {
		return mCancelled.get();
	}

	public final boolean cancel(boolean mayInterruptIfRunning) {
		mCancelled.set(true);
		return mFuture.cancel(mayInterruptIfRunning);
	}

	public final Result get() throws InterruptedException, ExecutionException {
		return mFuture.get();
	}

	public final Result get(long timeout, TimeUnit unit)
		throws InterruptedException, ExecutionException, TimeoutException {
		return mFuture.get(timeout, unit);
	}

	public final KaolaTask<Params, Progress, Result> execute(Params... params) {
		return executeOnExecutor(sDefaultExecutor, params);
	}

	public final KaolaTask<Params, Progress, Result> executeOnExecutor(
		Executor exec, Params... params) {
		if (mStatus != Status.PENDING) {
			switch (mStatus) {
			case RUNNING:
				throw new IllegalStateException("Cannot execute task:"
					+ " the task is already running.");
			case FINISHED:
				throw new IllegalStateException("Cannot execute task:"
					+ " the task has already been executed "
					+ "(a task can be executed only once)");
			}
		}

		mStatus = Status.RUNNING;

		onPreExecute();

		mWorker.mParams = params;
		exec.execute(mFuture);

		return this;
	}

	public static void execute(Runnable runnable) {
		sDefaultExecutor.execute(runnable);
	}

	protected final void publishProgress(Progress... values) {
		if (!isCancelled()) {
			sHandler.obtainMessage(MESSAGE_POST_PROGRESS,
				new KaolaTaskResult<Progress>(this, values)).sendToTarget();
		}
	}

	private void finish(Result result) {
		if (isCancelled()) {
			onCancelled(result);
		} else {
			onPostExecute(result);
		}
		mStatus = Status.FINISHED;
	}

	private static class InternalHandler extends Handler {
		@SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
		@Override
		public void handleMessage(Message msg) {
			KaolaTaskResult result = (KaolaTaskResult) msg.obj;
			switch (msg.what) {
			case MESSAGE_POST_RESULT:
				// There is only one result
				result.mTask.finish(result.mData[0]);
				break;
			case MESSAGE_POST_PROGRESS:
				result.mTask.onProgressUpdate(result.mData);
				break;
			}
		}
	}

	private static abstract class WorkerRunnable<Params, Result> implements
		Callable<Result> {
		Params[] mParams;
	}

	@SuppressWarnings({ "RawUseOfParameterizedType" })
	private static class KaolaTaskResult<Data> {
		final KaolaTask mTask;
		final Data[] mData;

		KaolaTaskResult(KaolaTask task, Data... data) {
			mTask = task;
			mData = data;
		}
	}
}
