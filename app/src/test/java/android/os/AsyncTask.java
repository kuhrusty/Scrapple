package android.os;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * "This is a shadow class for AsyncTask which forces it to run synchronously."
 * This approach (and most of the code below) was copied from
 * http://ryanharter.com/blog/2015/12/28/dealing-with-asynctask-in-unit-tests/
 */
public abstract class AsyncTask<Params, Progress, Result> {

    private boolean cancelled = false;
    private Result result = null;

    protected abstract Result doInBackground(Params... params);

    protected void onPreExecute() {
    }

    protected void onPostExecute(Result result) {
    }

    protected void onProgressUpdate(Progress... values) {
    }

    public final boolean isCancelled() {
        return cancelled;
    }

    public final boolean cancel(boolean mayInterruptIfRunning) {
        cancelled = true;
        //return mFuture.cancel(mayInterruptIfRunning);
        return true;
    }

    public final Result get() throws InterruptedException, ExecutionException {
        return result;
    }

    public final Result get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        //return mFuture.get(timeout, unit);
        return result;
    }

    public AsyncTask<Params, Progress, Result> execute(Params... params) {
        onPreExecute();
        result = doInBackground(params);
        onPostExecute(result);
        return this;
    }
}