package com.yufeiblog.cassandra.loadbalance;

import com.datastax.driver.core.*;
import com.google.common.base.Joiner;
import com.google.common.collect.AbstractIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DCSwitchRoundRobinPolicy implements SwitchLoadbalancePolicy {
    private static final Logger logger = LoggerFactory.getLogger(DCSwitchRoundRobinPolicy.class);

    private static final String UNSET = "";
    private final ConcurrentMap<String, CopyOnWriteArrayList<Host>> perDcLiveHosts = new ConcurrentHashMap<String, CopyOnWriteArrayList<Host>>();
    private final AtomicInteger index = new AtomicInteger();
    private volatile String localDC;
    private volatile Configuration configuration;
    private final int usedHostsPerRemoteDc = 0;
    private boolean isDegraded = false;
    private boolean isShard = false;
    private final boolean dontHopForLocalCL = true;

    @Override
    public void setLoaclDC(String dc) {
        this.localDC = dc;
    }

    @Override
    public void setShard(boolean isShard) {
        this.isShard = isShard;
    }

    @Override
    public void setDegraded(boolean isDegraded) {
        this.isDegraded = isDegraded;
    }

    @Override
    public void init(Cluster cluster, Collection<Host> hosts) {
        if (!UNSET.equals(localDC))
            logger.info("Using provided data-center name '{}' for DCAwareRoundRobinPolicy", localDC);
        this.configuration = cluster.getConfiguration();

        ArrayList<String> notInLocalDC = new ArrayList<String>();

        for (Host host : hosts) {
            String dc = dc(host);

            // If the localDC was in "auto-discover" mode and it's the first host for which we have a DC, use it.
            if (UNSET.equals(localDC) && !UNSET.equals(dc)) {
                logger.info("Using data-center name '{}' for DCAwareRoundRobinPolicy (if this is incorrect, please provide the correct datacenter name with DCAwareRoundRobinPolicy constructor)", dc);
                localDC = dc;
            } else if (!dc.equals(localDC))
                notInLocalDC.add(String.format("%s (%s)", host.toString(), dc));

            CopyOnWriteArrayList<Host> prev = perDcLiveHosts.get(dc);
            if (prev == null)
                perDcLiveHosts.put(dc, new CopyOnWriteArrayList<Host>(Collections.singletonList(host)));
            else
                prev.addIfAbsent(host);
        }

        if (notInLocalDC.size() > 0) {
            String nonLocalHosts = Joiner.on(",").join(notInLocalDC);
            logger.warn("Some contact points don't match local data center. Local DC = {}. Non-conforming contact points: {}", localDC, nonLocalHosts);
        }

        this.index.set(new Random().nextInt(Math.max(hosts.size(), 1)));
    }

    private String dc(Host host) {
        String dc = host.getDatacenter();
        return dc == null ? localDC : dc;
    }

    @Override
    public HostDistance distance(Host host) {
        String dc = dc(host);
        if (UNSET.equals(dc) || dc.equals(localDC))
            return HostDistance.LOCAL;

        CopyOnWriteArrayList<Host> dcHosts = perDcLiveHosts.get(dc);
        if (dcHosts == null || usedHostsPerRemoteDc == 0)
            return HostDistance.IGNORED;

        // We need to clone, otherwise our subList call is not thread safe
        dcHosts = cloneList(dcHosts);
        return dcHosts.subList(0, Math.min(dcHosts.size(), usedHostsPerRemoteDc)).contains(host)
                ? HostDistance.REMOTE
                : HostDistance.IGNORED;
    }

    @SuppressWarnings("unchecked")
    private static CopyOnWriteArrayList<Host> cloneList(CopyOnWriteArrayList<Host> list) {
        return (CopyOnWriteArrayList<Host>) list.clone();
    }

    @Override
    public Iterator<Host> newQueryPlan(String loggedKeyspace, Statement statement) {
        CopyOnWriteArrayList<Host> localLiveHosts = perDcLiveHosts.get(localDC);
        final List<Host> hosts = localLiveHosts == null ? Collections.<Host>emptyList() : cloneList(localLiveHosts);
        final int startIdx = index.getAndIncrement();

        return new AbstractIterator<Host>() {

            private int idx = startIdx;
            private int remainingLocal = hosts.size();

            // For remote Dcs
            private Iterator<String> remoteDcs;
            private List<Host> currentDcHosts;
            private int currentDcRemaining;

            @Override
            protected Host computeNext() {
                while (true) {
                    if (remainingLocal > 0) {
                        remainingLocal--;
                        int c = idx++ % hosts.size();
                        if (c < 0) {
                            c += hosts.size();
                        }
                        return hosts.get(c);
                    }

                    if (currentDcHosts != null && currentDcRemaining > 0) {
                        currentDcRemaining--;
                        int c = idx++ % currentDcHosts.size();
                        if (c < 0) {
                            c += currentDcHosts.size();
                        }
                        return currentDcHosts.get(c);
                    }

                    ConsistencyLevel cl = statement.getConsistencyLevel() == null
                            ? configuration.getQueryOptions().getConsistencyLevel()
                            : statement.getConsistencyLevel();

                    if (dontHopForLocalCL && cl.isDCLocal())
                        return endOfData();

                    if (remoteDcs == null) {
                        Set<String> copy = new HashSet<String>(perDcLiveHosts.keySet());
                        copy.remove(localDC);
                        remoteDcs = copy.iterator();
                    }

                    if (!remoteDcs.hasNext())
                        break;

                    String nextRemoteDc = remoteDcs.next();
                    CopyOnWriteArrayList<Host> nextDcHosts = perDcLiveHosts.get(nextRemoteDc);
                    if (nextDcHosts != null) {
                        // Clone for thread safety
                        List<Host> dcHosts = cloneList(nextDcHosts);
                        currentDcHosts = dcHosts.subList(0, Math.min(dcHosts.size(), usedHostsPerRemoteDc));
                        currentDcRemaining = currentDcHosts.size();
                    }
                }
                return endOfData();
            }
        };
    }

    @Override
    public void onAdd(Host host) {
        onUp(host);
    }

    @Override
    public void onUp(Host host) {
        String dc = dc(host);

        // If the localDC was in "auto-discover" mode and it's the first host for which we have a DC, use it.
        if (localDC.equals(UNSET) && !dc.equals(UNSET)) {
            logger.info("Using data-center name '{}' for DCAwareRoundRobinPolicy (if this is incorrect, please provide the correct datacenter name with DCAwareRoundRobinPolicy constructor)", dc);
            localDC = dc;
        }

        CopyOnWriteArrayList<Host> dcHosts = perDcLiveHosts.get(dc);
        if (dcHosts == null) {
            CopyOnWriteArrayList<Host> newMap = new CopyOnWriteArrayList<Host>(Collections.singletonList(host));
            dcHosts = perDcLiveHosts.putIfAbsent(dc, newMap);
            // If we've successfully put our new host, we're good, otherwise we've been beaten so continue
            if (dcHosts == null)
                return;
        }
        dcHosts.addIfAbsent(host);
    }

    @Override
    public void onDown(Host host) {
        CopyOnWriteArrayList<Host> dcHosts = perDcLiveHosts.get(dc(host));
        if (dcHosts != null)
            dcHosts.remove(host);
    }

    @Override
    public void onRemove(Host host) {
        onDown(host);
    }

    @Override
    public void close() {

    }
}
