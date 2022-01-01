package cpen221.mp3.fsftbuffer;

/**
 * A BufferObject is a data type that implements the Bufferable interface. It represents
 * an object that can be stored inside a FSFTBuffer. This class is used for testing
 * methods that belong to instances of the FSFTBuffer class.
 */
public class BufferObject implements Bufferable {

    /**
     * Return the identifier, as a String, of the current BufferObject, which in this case
     * is the BufferObject's hash code.
     * @return The hash code of the BufferObject.
     */
    @Override
    public String id() {
        return String.valueOf(this.hashCode());
    }
}
