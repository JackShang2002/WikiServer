package cpen221.mp3.fsftbuffer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A FSFTBuffer represents a finite-space finite-time buffer which can store a finite
 * number of bufferable objects for a finite amount of time. A bufferable object is a generic object
 * that extends the Bufferable interface.
 *
 * The buffer can only store a finite amount of objects, and objects added to the buffer are retained only for
 * a finite amount of time unless the object is accessed, updated, or touched using methods available to
 * instances of the FSFTBuffer class.
 * When an object is added to a FSFTBuffer and the buffer is full, the least recently
 * accessed object is removed to make space for the new object to be added.
 *
 * Abstraction Function:
 * A FSFTBuffer is represented by a buffer, which is a synchronized map with keys containing the string IDs of buffer objects
 * mapped to values, which are the bufferable objects themselves. Each object ID inside the buffer represents a
 * unique buffer object. In other words, two different IDs cannot map to the same buffer object. Thus, the buffer
 * cannot have duplicate buffer object IDs in the HashMap key set, nor can it have two different IDs in the key set
 * that map to the same bufferable object.
 * A FSFTBuffer with no key-value pairs in the buffer represents an empty FSFTBuffer.
 */

public class FSFTBuffer<T extends Bufferable> {

    /* the default buffer size is 32 objects */
    public static final int DSIZE = 32;

    /* the default timeout value is 3600s */
    public static final int DTIMEOUT = 3600;

    /* conversion constant 1 second = 1000 milliseconds */
    public static final int MILLIS = 1000;

    private final int capacity;
    private final int timeout;
    private Map<String, T> buffer = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Long> IdToTimeoutTime = Collections.synchronizedMap(new HashMap<>());
    private final List<String> accessQueue = Collections.synchronizedList(new ArrayList<>());

    /*
     * Representation Invariant:
     *  - capacity > 0
     *  - timeout > 0
     *  - All keys in buffer must be the id of the object stored in the key's corresponding value
     *  - All keys in buffer must appear exactly once in IdToTimeoutTime
     *  - All keys in buffer must appear exactly once in accessQueue
     *  - IdToTimeoutTime should have keys with values that map to a number > 0
     */

    /*
     * Thread Safety Arguments:
     *  - capacity and timeout are private and final, and each thread will have a local copy
     *    thus avoiding sharing capacity and timeout with other threads
     *  - buffer and IdToTimeoutTime both point to threadsafe map data type
     *  - accessQueue points to threadsafe list data type
     *  - for buffer, IdToTimeoutTime, and accessQueue, the non-threadsafe data type's reference
     *    is thrown away by directly passing either a new HashMap directly into synchronizedMap()
     *    or a new ArrayList directly into synchronizedList()
     *  - methods that contain iterators or for loops iterating through either buffer, IdToTimeoutTime,
     *    or accessQueue are synchronized
     */

    /**
     * Check that the representation invariants for FSFTBuffer hold true.
     * @throws RuntimeException if any representation invariants are violated.
     */
    private synchronized void checkRep() {
        if (capacity <= 0) {
            throw new RuntimeException("capacity should be > 0");
        }
        if (timeout <= 0) {
            throw new RuntimeException("timeout should be > 0");
        }

        for (String key : buffer.keySet()) {
            int countId = 0;
            int countQueue = 0;
            for (String ID : IdToTimeoutTime.keySet()) {
                if (Objects.equals(key, ID)) {
                    countId++;
                }
            }
            for (String queueId : accessQueue) {
                if (Objects.equals(key, queueId)) {
                    countQueue++;
                }
            }
            if (countId > 1) {
                throw new RuntimeException("Key " + key + " in buffer does not appear exactly once in IdToTimeoutTime. It appears " + countId + "times.");
            }
            if (countQueue > 1) {
                throw new RuntimeException("Key " + key + " in buffer does not appear exactly once in accessQueue. It appears " + countQueue + "times.");
            }
            if (!Objects.equals(key, buffer.get(key).id())) {
                throw new RuntimeException("Key " + key + " in buffer does not correspond to the id of the object stored as the key's value.");
            }
        }

        for (Long time : IdToTimeoutTime.values()) {
            if (time < 0) {
                throw new RuntimeException("IdToTimeoutTime contains a key that maps to a negative value.");
            }
        }
    }

    /**
     * Create a FSFTBuffer with a fixed capacity and a timeout value.
     * Objects in the buffer that have not been "refreshed" (had
     * their timeout time be extended) within the timeout period are
     * removed from the cache.
     *
     * @param capacity The number of objects the buffer can hold.
     *                 Requires that capacity be a positive number greater than zero.
     * @param timeout  The duration, in seconds, an object should
     *                 be in the buffer before it times out.
     *                 Requires that timeout be a positive number greater than zero.
     */
    public FSFTBuffer(int capacity, int timeout) {
        this.capacity = capacity;
        this.timeout = timeout;
        checkRep();
    }

    /**
     * Create a buffer with default capacity and timeout values.
     * The default capacity is 32 and the default timeout is 3600.
     */
    public FSFTBuffer() {
        this(DSIZE, DTIMEOUT);
    }

    /**
     * First remove all objects that have been timed out from the buffer.
     * Then add a bufferable object (an object that extends the Bufferable
     * interface) to the buffer.
     *
     * Repeated calls to put with the same bufferable object would update
     * the object's timeout time, behaving the same way as multiple calls
     * to the update method available to instances of the FSFTBuffer class.
     * If the buffer is full, remove the least recently accessed
     * object to make room for the new object.
     *
     * @param t Value to be added to the buffer.
     *          Must extend the Bufferable interface.
     * @return
     *  - true if t was successfully added to buffer, or successfully
     *  replaced with a more recent version of the object if the object
     *  was already in the buffer prior to method call.
     *  - false if t is null, or if t was not successfully added.
     *
     * Frame Condition:
     * - buffer is modified such that all stale objects at time of method call are removed
     */
    public synchronized boolean put(T t) {
        checkRep();
        Map<String, T> updatedBuffer = removeStaleObjects(buffer, IdToTimeoutTime);

        if (t == null) {
            buffer = Collections.synchronizedMap(new HashMap<>(updatedBuffer));
            return false;
        }

        if (updatedBuffer.keySet().size() < capacity) {
            updatedBuffer.put(t.id(), t);
            IdToTimeoutTime.put(t.id(), System.currentTimeMillis() / MILLIS + timeout);
            buffer = Collections.synchronizedMap(new HashMap<>(updatedBuffer));

            if (!accessQueue.contains(t.id())) {
                accessQueue.add(t.id());
            }
        }

        else {
            int leastRecentPosition = 0;
            String leastRecentId = accessQueue.get(leastRecentPosition);
            accessQueue.remove(leastRecentPosition);
            updatedBuffer.remove(leastRecentId);
            IdToTimeoutTime.remove(leastRecentId);

            updatedBuffer.put(t.id(), t);
            if (!accessQueue.contains(t.id())) {
                accessQueue.add(t.id());
            }
            IdToTimeoutTime.put(t.id(), System.currentTimeMillis() / MILLIS + timeout);
            buffer = Collections.synchronizedMap(new HashMap<>(updatedBuffer));
        }

        checkRep();
        return true;
    }

    /**
     * Retrieve the object specified by parameter id from the FSFTBuffer.
     * When an object is retrieved from the buffer, it is "used", or accessed,
     * at that time. This means that if the object specified by id was the
     * least recently accessed object in the FSFTBuffer, calling this method would
     * access it and ensure that it would not be the next object to be
     * removed, assuming another object needed to be added to the FSFTBuffer
     * and the buffer was at capacity.
     *
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the
     * buffer
     * @throws ObjectNotInCacheException if the object specified by
     * parameter id is not in the buffer
     *
     * Frame Condition:
     * - buffer is modified such that all stale objects at time of method call are removed
     * - accessQueue is modified by adding removing parameter id from index zero and adding
     *   it to the last index
     */
    public synchronized T get(String id) throws ObjectNotInCacheException {
        checkRep();

        Map<String, T> updatedBuffer = removeStaleObjects(buffer, IdToTimeoutTime);
        buffer = Collections.synchronizedMap(new HashMap<>(updatedBuffer));

        if (buffer.containsKey(id)) {
            accessQueue.add(id);
            for (int i = 0; i < accessQueue.size(); i++) {
                if (Objects.equals(accessQueue.get(i), id)) {
                    accessQueue.remove(i);
                    break;
                }
            }

            checkRep();
            return buffer.get(id);
        }

        else {
            checkRep();
            throw new ObjectNotInCacheException();
        }

    }

    /**
     * Update the timeout time for the object with the provided id.
     * This method is used to mark an object as "not stale" so that its
     * timeout is delayed. The updated timeout time for the object is the
     * current time in seconds plus the timeout time set for all objects in the buffer.
     *
     * @param id the identifier of the object whose timeout time will be updated.
     * @return - true if object in buffer specified by id has had its timeout time extended
     *         - false if timeout time of object was not successfully updated, possibly because
     *           the parameter id does not correspond to the identifier of an object stored in the buffer
     *
     * Frame Condition:
     * - buffer is modified such that all stale objects at time of method call are removed
     * - IdToTimeoutTime is modified so that timeout value corresponding to the id string key is updated
     *   to have a delayed timeout time
     */
    public boolean touch(String id) {
        checkRep();

        Map<String, T> updatedBuffer = removeStaleObjects(buffer, IdToTimeoutTime);
        buffer = Collections.synchronizedMap(new HashMap<>(updatedBuffer));

        if (buffer.containsKey(id)) {
            IdToTimeoutTime.put(id, System.currentTimeMillis() / MILLIS + timeout);
            checkRep();
            return true;
        }

        checkRep();
        return false;
    }

    /**
     * Update the object stored in the buffer. If an object with the same id as t exists
     * in the buffer, replace that object with t and update the timeout time of the object.
     * The updated timeout time for the object is the current time in seconds plus the
     * timeout time set for all objects in the buffer.
     *
     * @param t the object to update. Must extend the Bufferable interface.
     * @return - true if object in buffer with the same id as t has been replaced by t
     *         - false if timeout time of object, or the object itself was not successfully updated, possibly because
     *           the parameter id does not correspond to the identifier of an object stored in the buffer
     *
     * Frame Condition:
     * - buffer is modified such that all stale objects at time of method call are removed
     * - IdToTimeoutTime is modified so that timeout value corresponding to the id string key is updated
     *   to have a delayed timeout time
     */
    public boolean update(T t) {
        checkRep();
        boolean timeUpdated = touch(t.id());

        if (timeUpdated) {
            buffer.put(t.id(), t);
            checkRep();
            return true;
        }

        checkRep();
        return false;
    }

    /**
     * Remove stale objects from the buffer, where stale refers to an
     * object that has exceeded its timeout time.
     *
     * @param buffer the buffer from which stale objects will be removed
     * @param IdToTimeoutTime contains the IDs of all objects in buffer and their corresponding
     *                        timeout time, which is the duration, in seconds, an object should be
     *                        in the buffer before it goes stale.
     * @return An updated buffer containing only the objects from the original buffer that were not stale.
     */
    private synchronized Map<String, T> removeStaleObjects(Map<String, T> buffer, Map<String, Long> IdToTimeoutTime) {

        Map<String, T> updatedBuffer = new HashMap<>();
        List<Long> timeoutTimes = Collections.synchronizedList(new ArrayList<>(IdToTimeoutTime.values()));

        List<Long> notTimedOutTimes = timeoutTimes.stream()
                .filter(time -> time > System.currentTimeMillis() / MILLIS)
                .collect(Collectors.toList());

        for (String ID : buffer.keySet()) {
            if (notTimedOutTimes.contains(IdToTimeoutTime.get(ID))) {
                updatedBuffer.put(ID, buffer.get(ID));
            }
        }

        return updatedBuffer;
    }
}
