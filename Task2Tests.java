package cpen221.mp3;

import cpen221.mp3.fsftbuffer.BufferObject;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.fsftbuffer.ObjectNotInCacheException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Task2Tests {
    private static final BufferObject obj1 = new BufferObject();
    private static final BufferObject obj2 = new BufferObject();
    private static final BufferObject obj3 = new BufferObject();
    private static final BufferObject obj4 = new BufferObject();
    private static FSFTBuffer<BufferObject> testBuffer = new FSFTBuffer<>(2, 3);

    Runnable put1 = () -> testBuffer.put(obj1);
    Runnable put2 = () -> testBuffer.put(obj2);
    Runnable put3 = () -> testBuffer.put(obj3);

    Runnable touch1 = () -> testBuffer.touch(obj1.id());
    Runnable touch2 = () -> testBuffer.touch(obj2.id());

    Runnable update1 = () -> testBuffer.update(obj1);
    Runnable update2 = () -> testBuffer.update(obj2);

    Runnable get1 = () -> {
        try {
            testBuffer.get(obj1.id());
        } catch (ObjectNotInCacheException e) {
            e.printStackTrace();
        }
    };

    Runnable get2 = () -> {
        try {
            testBuffer.get(obj2.id());
        } catch (ObjectNotInCacheException e) {
            e.printStackTrace();
        }
    };

    Runnable get3 = () -> {
        try {
            testBuffer.get(obj3.id());
        } catch (ObjectNotInCacheException e) {
            e.printStackTrace();
        }
    };

    @Test
    public void testPutPutOnEmptyBuffer() throws ObjectNotInCacheException {
        Thread thread1 = new Thread(put1);
        Thread thread2 = new Thread(put2);

        thread1.run();
        thread2.run();

        Assertions.assertEquals(obj1, testBuffer.get(obj1.id()));
        Assertions.assertEquals(obj2, testBuffer.get(obj2.id()));
    }

    @Test
    public void testPutPutOnFullBuffer() throws ObjectNotInCacheException {
        Thread thread1 = new Thread(put1);
        Thread thread2 = new Thread(put2);
        Thread thread3 = new Thread(put3);

        thread1.run();
        thread3.run();
        thread2.run();

        Assertions.assertEquals(obj3, testBuffer.get(obj3.id()));
        Assertions.assertEquals(obj2, testBuffer.get(obj2.id()));
        assertThrows(ObjectNotInCacheException.class, () -> testBuffer.get(obj1.id()));
    }

    @Test
    public void testPutGetOnEmptyBuffer() throws ObjectNotInCacheException {
        Thread thread1 = new Thread(put3);
        Thread thread2 = new Thread(get3);
        Thread thread3 = new Thread(put2);
        Thread thread4 = new Thread(get2);

        thread1.run();
        thread2.run();
        thread3.run();
        thread4.run();

        Assertions.assertEquals(obj3, testBuffer.get(obj3.id()));
        Assertions.assertEquals(obj2, testBuffer.get(obj2.id()));
        assertThrows(ObjectNotInCacheException.class, () -> testBuffer.get(obj1.id()));
    }

    @Test
    public void testPutGetOnFullBuffer() throws ObjectNotInCacheException {
        Thread thread1 = new Thread(put1);
        Thread thread2 = new Thread(put2);
        Thread thread3 = new Thread(put3);
        Thread thread4 = new Thread(get2);
        Thread thread5 = new Thread(get3);

        thread1.run();
        thread2.run();
        thread3.run();
        thread4.run();
        thread5.run();

        Assertions.assertEquals(obj3, testBuffer.get(obj3.id()));
        Assertions.assertEquals(obj2, testBuffer.get(obj2.id()));
        assertThrows(ObjectNotInCacheException.class, () -> testBuffer.get(obj1.id()));
    }
}
