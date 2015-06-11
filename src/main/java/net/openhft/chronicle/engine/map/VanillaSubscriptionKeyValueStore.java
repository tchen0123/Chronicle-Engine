package net.openhft.chronicle.engine.map;

import net.openhft.chronicle.engine.api.Asset;
import net.openhft.chronicle.engine.api.InvalidSubscriberException;
import net.openhft.chronicle.engine.api.RequestContext;
import net.openhft.chronicle.engine.api.map.KeyValueStore;

/**
 * Created by peter on 22/05/15.
 */
public class VanillaSubscriptionKeyValueStore<K, MV, V> extends AbstractKeyValueStore<K, MV, V> implements ObjectKeyValueStore<K, MV, V>, AuthenticatedKeyValueStore<K, MV, V> {
    private final SubscriptionKVSCollection<K, MV, V> subscriptions;

    public VanillaSubscriptionKeyValueStore(RequestContext context, Asset asset, KeyValueStore<K, MV, V> item) {
        super(item);
        this.subscriptions = new VanillaSubscriptionKVSCollection<>(ObjectSubscription.class, asset);
        subscriptions.setKvStore(this);
    }

    @Override
    public SubscriptionKVSCollection<K, MV, V> subscription(boolean createIfAbsent) {
        return subscriptions;
    }

    @Override
    public V replace(K key, V value) {
        V oldValue = kvStore.replace(key, value);
        if (oldValue != null) {
            try {
                subscriptions.notifyEvent(UpdatedEvent.of(key, oldValue, value));
            } catch (InvalidSubscriberException e) {
                throw new AssertionError(e);
            }
        }
        return oldValue;
    }

    @Override
    public boolean put(K key, V value) {
        if (subscriptions.needsPrevious()) {
            return getAndPut(key, value) != null;
        }
        boolean replaced = kvStore.put(key, value);
        try {
            subscriptions.notifyEvent(replaced
                    ? InsertedEvent.of(key, value)
                    : UpdatedEvent.of(key, null, value));
        } catch (InvalidSubscriberException e) {
            throw new AssertionError(e);
        }
        return replaced;

    }

    @Override
    public boolean remove(K key) {
        if (subscriptions.needsPrevious()) {
            return getAndRemove(key) != null;
        }
        if (kvStore.remove(key)) {
            try {
                subscriptions.notifyEvent(RemovedEvent.of(key, null));
            } catch (InvalidSubscriberException e) {
                throw new AssertionError(e);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean replaceIfEqual(K key, V oldValue, V newValue) {
        if (kvStore.replaceIfEqual(key, oldValue, newValue)) {
            try {
                subscriptions.notifyEvent(UpdatedEvent.of(key, oldValue, newValue));
            } catch (InvalidSubscriberException e) {
                throw new AssertionError(e);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean removeIfEqual(K key, V value) {
        if (kvStore.removeIfEqual(key, value)) {
            try {
                subscriptions.notifyEvent(RemovedEvent.of(key, value));
            } catch (InvalidSubscriberException e) {
                throw new AssertionError(e);
            }
            return true;
        }
        return false;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        V ret = kvStore.putIfAbsent(key, value);
        if (ret == null)
            try {
                subscriptions.notifyEvent(InsertedEvent.of(key, value));
            } catch (InvalidSubscriberException e) {
                throw new AssertionError(e);
            }
        return ret;
    }

    @Override
    public V getAndPut(K key, V value) {
        V oldValue = kvStore.getAndPut(key, value);
        try {
            subscriptions.notifyEvent(oldValue == null
                    ? InsertedEvent.of(key, value)
                    : UpdatedEvent.of(key, oldValue, value));
        } catch (InvalidSubscriberException e) {
            throw new AssertionError(e);
        }
        return oldValue;
    }

    @Override
    public V getAndRemove(K key) {
        V oldValue = kvStore.getAndRemove(key);
        if (oldValue != null) {
            try {
                subscriptions.notifyEvent(RemovedEvent.of(key, oldValue));
            } catch (InvalidSubscriberException e) {
                throw new AssertionError(e);
            }
        }
        return oldValue;
    }
}
