package uk.ac.starlink.array;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Allows convenient stepping through an array.
 * This class is provided as a convenience for applications code which 
 * wishes to iterate through an array in sections, at each stage 
 * obtaining a java primitive array containing a contiguous chunk of
 * its pixels.  This may be more efficient than using the
 * single-element read/write methods of NDArray.
 * <p>
 * This class does not do anything very clever; it simply provides at
 * each iteration base and length of a block which will take you from
 * the start to the end of an array of given size over the lifetime of
 * the iterator.  These blocks will
 * be of the same (user-defined or default) size with the possible 
 * exception of the last one, which will just mop up any remaining
 * elements.
 * <p>
 * The simplest use of this class would therefore look something like this
 * <pre>
 *     ArrayAccess acc = nda.getAccess(); 
 *     long npix = acc.getShape().getNumPixels();
 *     for ( ChunkIterator cIt = new ChunkIterator( npix ); 
 *           cIt.hasNext(); cIt.next() ) {
 *         int size = cIt.getSize();
 *         Object buffer = acc.getType().newArray( size );
 *         acc.read( buffer, 0, size );
 *         doStuff( buffer );
 *     }
 * </pre>
 * A more efficient loop would reuse the same buffer array to save on
 * object creation/collection costs as follows:
 * <pre>
 *     ChunkIterator cIt = new ChunkIterator( npix );
 *     Object buffer = acc.getType().newArray( cIt.getSize() );
 *     for ( ; cIt.hasNext(); cIt.next() ) {
 *         acc.read( buffer, 0, cIt.getSize() );
 *         doStuff( buffer );
 *     }
 * </pre>
 * 
 * @author   Mark Taylor
 * @version  $Id$
 */
public class ChunkIterator {

    private long chunkBase = 0L;
    private final long length;
    private final int chunkSize;

    /** The default size of chunks if not otherwise specified. */
    public static int defaultChunkSize = 16384;

    /**
     * Create a new ChunkIterator with a given chunk size.
     *
     * @param   length     the total number of elements to iterate over
     * @param   chunkSize  the size of chunk which will be used (except
     *                     perhaps for the last chunk)
     * @throws  IllegalArgumentException  if <tt>chunkSize&lt;=0</tt>
     *                                    or <tt>length&lt;0</tt>
     */
    public ChunkIterator( long length, int chunkSize ) {
        if ( chunkSize <= 0 ) {
            throw new IllegalArgumentException( 
                "chunkSize " + chunkSize + " <= 0" );
        }
        if ( length < 0L ) {
            throw new IllegalArgumentException(
                "length " + length + " < 0" );
        }
        this.length = length;
        this.chunkSize = chunkSize;
    }

    /**
     * Create a new ChunkIterator with the default chunk size.
     *
     * @param   length     the total number of elements to iterate over
     */
    public ChunkIterator( long length ) {
        this( length, defaultChunkSize );
    }

    /**
     * See if iteration has finished.
     *
     * @return   true if there are no more chunks
     */
    public boolean hasNext() {
        return chunkBase < length;
    }

    /**
     * Get the size of the current chunk.  It will be equal to the size
     * specified in the constructor (or the default if none was specified),
     * except for the last chunk, when it may be smaller.
     *
     * @return   the current chunk size
     */
    public int getSize() {
        return (int) Math.min( length - chunkBase, (long) chunkSize );
    }

    /**
     * The offset of the base of the current chunk.  Zero for the first 
     * chunk, and increasing by getSize with each iteration after that.
     *
     * @return  the base of the current chunk
     */
    public long getBase() {
        return chunkBase;
    }

    /**
     * Iterates to the next chunk.
     *
     * @throws  NoSuchElementException if hasNext would return false
     */
    public void next() {
        if ( chunkBase < length ) {
            chunkBase += getSize();
        }
        else {
            throw new NoSuchElementException();
        }
    }
}
