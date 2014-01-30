package gov.va.isaac.search;

import java.util.Collection;

/**
 * Handle object to get search results.
 *
 * @author Dan Armbrust
 * @author ocarlsen
 */
public class SearchHandle {

    private final long searchStartTime = System.currentTimeMillis();

    private Collection<GuiSearchResult> result;
    private volatile boolean cancelled = false;
    private Exception error = null;

    /**
     * Blocks until the results are available....
     *
     * @return
     * @throws Exception
     */
    public Collection<GuiSearchResult> getResults() throws Exception {
        if (result == null) {
            synchronized (SearchHandle.this) {
                while (result == null && error == null && !cancelled) {
                    try {
                        SearchHandle.this.wait();
                    } catch (InterruptedException e) {
                        // noop
                    }
                }
            }
        }
        if (error != null) {
            throw error;
        }
        return result;
    }

    protected void setResults(Collection<GuiSearchResult> results) {
        synchronized (SearchHandle.this) {
            result = results;
        }
    }

    protected void setError(Exception e) {
        synchronized (SearchHandle.this) {
            this.error = e;
        }
    }

    public long getSearchStartTime() {
        return searchStartTime;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }
}
