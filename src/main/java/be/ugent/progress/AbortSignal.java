package be.ugent.progress;

import java.util.concurrent.atomic.AtomicBoolean;

public class AbortSignal {
    private AtomicBoolean aborted;

    public AbortSignal() {
        this.aborted = new AtomicBoolean(false);
    }

    public void abort(){
        this.aborted.set(true);
    }

    public void reset(){
        this.aborted.set(false);
    }

    public boolean isAborted(){
        return this.aborted.get();
    }
}
