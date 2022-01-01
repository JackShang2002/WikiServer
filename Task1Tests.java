package cpen221.mp3;

import cpen221.mp3.fsftbuffer.BufferObject;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.fsftbuffer.ObjectNotInCacheException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Task1Tests {
    private static final BufferObject obj1 = new BufferObject();
    private static final BufferObject obj2 = new BufferObject();
    private static final BufferObject obj3 = new BufferObject();
    private static final BufferObject obj4 = new BufferObject();
    private static final BufferObject obj5 = new BufferObject();
    private static final BufferObject obj6 = new BufferObject();
    private static final BufferObject obj7 = new BufferObject();
    private static final BufferObject objnotincache = new BufferObject();

    @Test
    public void testPutTrueFalse() {
        FSFTBuffer<BufferObject> defaultBuffer = new FSFTBuffer<>();

        Assertions.assertFalse(defaultBuffer.put(null));
        Assertions.assertTrue(defaultBuffer.put(obj1));
        Assertions.assertTrue(defaultBuffer.put(obj1));
        Assertions.assertTrue(defaultBuffer.put(obj2));
        Assertions.assertTrue(defaultBuffer.put(obj3));
    }

    @Test
    public void testPutOverwritesLRAObject_1() throws ObjectNotInCacheException {
        FSFTBuffer<BufferObject> buffer1 = new FSFTBuffer<>(4, 5);

        buffer1.put(obj1);
        buffer1.put(obj2);
        buffer1.put(obj3);
        buffer1.put(obj4);
        buffer1.put(obj5);
        buffer1.put(obj6);

        Assertions.assertEquals(obj5, buffer1.get(obj5.id()));
        Assertions.assertEquals(obj6, buffer1.get(obj6.id()));
        Assertions.assertEquals(obj3, buffer1.get(obj3.id()));
        Assertions.assertEquals(obj4, buffer1.get(obj4.id()));

        assertThrows(ObjectNotInCacheException.class, () -> buffer1.get(obj1.id()));

        Assertions.assertThrows(ObjectNotInCacheException.class, () -> buffer1.get(obj2.id()));
        Assertions.assertThrows(ObjectNotInCacheException.class, () -> buffer1.get(objnotincache.id()));
    }

    @Test
    public void testGetAccessesObject() throws ObjectNotInCacheException {
        FSFTBuffer<BufferObject> buffer1 = new FSFTBuffer<>(4, 10);

        buffer1.put(obj1);
        buffer1.put(obj2);
        buffer1.put(obj3);
        buffer1.put(obj4);

        buffer1.get(obj1.id());

        buffer1.put(obj5);
        buffer1.put(obj6);
        buffer1.put(obj7);

        Assertions.assertEquals(obj1, buffer1.get(obj1.id()));
        Assertions.assertEquals(obj5, buffer1.get(obj5.id()));
        Assertions.assertEquals(obj6, buffer1.get(obj6.id()));
        Assertions.assertEquals(obj7, buffer1.get(obj7.id()));

        assertThrows(ObjectNotInCacheException.class, () -> buffer1.get(obj2.id()));

        Assertions.assertThrows(ObjectNotInCacheException.class, () -> buffer1.get(obj3.id()));
        Assertions.assertThrows(ObjectNotInCacheException.class, () -> buffer1.get(obj4.id()));
    }

    @Test
    public void testPutTimeout() throws InterruptedException, ObjectNotInCacheException {
        FSFTBuffer<BufferObject> timeoutBuffer = new FSFTBuffer<>(4, 2);
        timeoutBuffer.put(obj1);
        timeoutBuffer.put(obj2);
        Thread.sleep(1000);
        timeoutBuffer.put(obj3);
        Thread.sleep(1000);
        timeoutBuffer.put(obj4);


        assertThrows(ObjectNotInCacheException.class, () -> timeoutBuffer.get(obj1.id()));
        assertThrows(ObjectNotInCacheException.class, () -> timeoutBuffer.get(obj2.id()));

        Assertions.assertEquals(obj3, timeoutBuffer.get(obj3.id()));
        Assertions.assertEquals(obj4, timeoutBuffer.get(obj4.id()));

        Thread.sleep(1000);

        assertThrows(ObjectNotInCacheException.class, () -> timeoutBuffer.get(obj3.id()));
    }

    @Test
    public synchronized void testMultiplePutsBehaveSameAsUpdate() throws InterruptedException, ObjectNotInCacheException {
        FSFTBuffer<BufferObject> buffer1 = new FSFTBuffer<>(4, 3);
        FSFTBuffer<BufferObject> buffer2 = new FSFTBuffer<>(4, 3);

        buffer1.put(obj1);
        buffer2.put(obj1);

        Thread.sleep(2000);

        buffer1.put(obj1);
        buffer2.update(obj1);

        Assertions.assertEquals(obj1, buffer1.get(obj1.id()));
        Assertions.assertEquals(obj1, buffer2.get(obj1.id()));

        Thread.sleep(3000);

        assertThrows(ObjectNotInCacheException.class, () -> buffer1.get(obj1.id()));

        assertThrows(ObjectNotInCacheException.class, () -> buffer2.get(obj1.id()));
    }

    @Test
    public void testBasicPutAndGet() throws ObjectNotInCacheException {
        FSFTBuffer<BufferObject> buffer1 = new FSFTBuffer<>(4, 5);

        buffer1.put(obj1);
        buffer1.put(obj2);
        buffer1.put(obj3);
        buffer1.put(obj4);
        Assertions.assertEquals(obj1, buffer1.get(obj1.id()));
        Assertions.assertEquals(obj2, buffer1.get(obj2.id()));
        Assertions.assertEquals(obj3, buffer1.get(obj3.id()));
        Assertions.assertEquals(obj4, buffer1.get(obj4.id()));
    }

    @Test
    public void testGetThrowsException() {
        FSFTBuffer<BufferObject> buffer1 = new FSFTBuffer<>(4, 5);

        assertThrows(ObjectNotInCacheException.class, () -> buffer1.get("non-existent ID because buffer is empty"));

        buffer1.put(obj1);
        String obj1_fakeID = obj1.id() + "abc";
        assertThrows(ObjectNotInCacheException.class, () -> buffer1.get(obj1_fakeID));
    }

    @Test
    public void testTouchExtendsTimeoutTime() throws InterruptedException, ObjectNotInCacheException {
        FSFTBuffer<BufferObject> buffer1 = new FSFTBuffer<>(4, 2);

        buffer1.put(obj1);
        Thread.sleep(1000);
        buffer1.touch(obj1.id());

        Assertions.assertEquals(obj1, buffer1.get(obj1.id()));
        Thread.sleep(1000);

        Assertions.assertEquals(obj1, buffer1.get(obj1.id()));
        Thread.sleep(1000);

        assertThrows(ObjectNotInCacheException.class, () -> buffer1.get(obj1.id()));
    }

    @Test
    public void testTouchTrueFalse() {
        FSFTBuffer<BufferObject> buffer1 = new FSFTBuffer<>(4, 2);

        buffer1.put(obj1);
        buffer1.put(obj2);
        buffer1.put(obj3);

        Assertions.assertTrue(buffer1.touch(obj1.id()));
        Assertions.assertTrue(buffer1.touch(obj2.id()));
        Assertions.assertTrue(buffer1.touch(obj3.id()));
        Assertions.assertFalse(buffer1.touch(obj4.id()));
        Assertions.assertFalse(buffer1.touch(obj5.id()));
    }

    @Test
    public void testUpdateTrueFalse() {
        FSFTBuffer<BufferObject> buffer1 = new FSFTBuffer<>(4, 2);

        buffer1.put(obj1);
        buffer1.put(obj2);
        buffer1.put(obj3);

        Assertions.assertTrue(buffer1.update(obj1));
        Assertions.assertTrue(buffer1.update(obj2));
        Assertions.assertTrue(buffer1.update(obj3));
        Assertions.assertFalse(buffer1.update(obj4));
        Assertions.assertFalse(buffer1.update(obj5));
    }

    @Test
    public void testBasicUpdate() throws InterruptedException, ObjectNotInCacheException {
        FSFTBuffer<BufferObject> buffer1 = new FSFTBuffer<>(4, 2);

        buffer1.put(obj1);
        Thread.sleep(1000);

        buffer1.update(obj1);
        Thread.sleep(1000);

        Assertions.assertEquals(obj1, buffer1.get(obj1.id()));
        Thread.sleep(1000);

        assertThrows(ObjectNotInCacheException.class, () -> buffer1.get(obj1.id()));
    }
}

