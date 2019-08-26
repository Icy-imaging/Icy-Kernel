/**
 * 
 */
package icy.sequence;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import icy.image.IcyBufferedImage;

/**
 * Class used to accelerate Sequence data access on first loading using data prefetching.
 * 
 * @author Stephane
 */
public class SequencePrefetcher extends Thread
{
    private static class PrefetchEntry
    {
        final Reference<Sequence> sequence;
        final int t;
        final int z;
        final int hc;

        public PrefetchEntry(Sequence sequence, int t, int z)
        {
            super();

            this.sequence = new WeakReference<Sequence>(sequence);
            this.t = t;
            this.z = z;

            hc = sequence.hashCode() ^ (t << 0) ^ (z << 16);
        }

        @Override
        public int hashCode()
        {
            return hc;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof PrefetchEntry)
            {
                final PrefetchEntry entry = (PrefetchEntry) obj;

                return (entry.sequence.get() == sequence.get()) && (entry.t == t) && (entry.z == z);
            }

            return super.equals(obj);
        }
    }

    // singleton
    private final static SequencePrefetcher prefetcher = new SequencePrefetcher();

    public static void prefetch(Sequence sequence, int t, int z)
    {
        prefetcher.prefetchInternal(sequence, t, z);
    }

    public static void cancel(Sequence sequence)
    {
        prefetcher.cancelInternal(sequence);
    }

    public static void shutdown()
    {
        prefetcher.interrupt();
    }

    private final Set<PrefetchEntry> prefetchSet;
    private final Deque<PrefetchEntry> prefetchQueue;

    public SequencePrefetcher()
    {
        super("Sequence prefetcher");

        prefetchSet = new HashSet<PrefetchEntry>();
        prefetchQueue = new LinkedBlockingDeque<PrefetchEntry>();

        start();
    }

    private void prefetchInternal(Sequence sequence, int t, int z)
    {
        final PrefetchEntry entry = new PrefetchEntry(sequence, t, z);

        // already in queue ? nothing to do...
        if (prefetchSet.contains(entry))
            return;

        final IcyBufferedImage image = sequence.getImage(t, z, false);

        // nothing to prefetch here
        if (image == null)
            return;

        // data already initialized..
        if (image.isDataInitialized())
            return;

        synchronized (prefetchSet)
        {
            prefetchSet.add(entry);
            prefetchQueue.addFirst(entry);
        }
    }

    private void cancelInternal(Sequence sequence)
    {
        final List<PrefetchEntry> toRemove = new ArrayList<PrefetchEntry>();

        // build list of element to remove
        synchronized (prefetchSet)
        {
            final Iterator<PrefetchEntry> it = prefetchSet.iterator();

            while (it.hasNext())
            {
                final PrefetchEntry entry = it.next();
                final Sequence entrySeq = entry.sequence.get();

                if ((entrySeq == null) || (entrySeq == sequence))
                    toRemove.add(entry);
            }

            prefetchSet.removeAll(toRemove);
            prefetchQueue.removeAll(toRemove);
        }
    }

    @Override
    public void run()
    {
        while (!interrupted())
        {
            final PrefetchEntry entry;

            synchronized (prefetchSet)
            {
                entry = prefetchQueue.pollFirst();
                if (entry != null)
                    prefetchSet.remove(entry);
            }

            // need to prefetch something ?
            if (entry != null)
            {
                // we use weak reference to not retain Sequence with prefetch process..
                final Sequence entrySeq = entry.sequence.get();

                // prefetch data
                if (entrySeq != null)
                    entrySeq.getImage(entry.t, entry.z, true);
            }
            else
            {
                // nothing to do..
                try
                {
                    // sleep a bit
                    sleep(1);
                }
                catch (InterruptedException e)
                {
                    // ignore safely
                }
            }
        }
    }
}
