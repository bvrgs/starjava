package uk.ac.starlink.array;

import java.io.IOException;

/**
 * Implements an ArrayAccess using an AccessImpl object which may be 
 * shared with other clients.
 * It is safe to instances of this class based on the same shared 
 * AccessImpl in different threads concurrently <i>(I think)</i>.
 *
 * @author   Mark Taylor (Starlink)
 */
class MultiplexArrayAccess extends DefaultArrayAccess {

    private long offset = 0L;
    private AccessImpl impl;

    /**
     * Constructs an ArrayAccess object from a description of the array's
     * characteristics and a basic, possibly shared, implementation of
     * pixel access functionality.
     *
     * @param   adesc    array characteristics description
     * @param   impl     array access service provider; may be shared with
     *                   other objects.  Must provide random access.
     * @param  mappedArray  a java primitve array containing all the pixel
     *                      data of the accessed array.  May be null if
     *                      mapped access is not provided
     * @throws  UnsupportedOperationException  if adesc.isRandom() returns false
     */
    public MultiplexArrayAccess( ArrayDescription adesc, AccessImpl impl, 
                                 Object mappedArray ) {
        super( adesc, impl, null );
        this.impl = impl;
        if ( ! adesc.isRandom() ) {
            throw new UnsupportedOperationException( 
                "Cannot create MultiplexArrayAccess " +
                "from non-random AccessImpl" );
        }
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset( long off ) throws IOException {

        /* Check offset is in legal range. */
        if ( off >= 0L && off < arrayNpix ) {
            offset = off;
        }

        /* If it's not, nvoke the superclass implementation just to 
         * throw a consistent exception. */
        else {
            synchronized ( impl ) {
                super.setOffset( off );
            }
            // assert false;
        }
    }

    public long[] getPosition() {
        return arrayShape.indexToPosition( offset );
    }

    public void setPosition( long[] pos ) throws IOException {
        setOffset( arrayShape.positionToIndex( pos ) );
    }

    public void read( Object buffer, int start, int size ) throws IOException {
        synchronized ( impl ) {
            super.setOffset( offset );
            super.read( buffer, start, size );
        }
    }

    public void write( Object buffer, int start, int size ) throws IOException {
        synchronized ( impl ) {
            super.setOffset( offset );
            super.write( buffer, start, size );
        }
    }

    public void readTile( Object buffer, NDShape tile ) throws IOException {
        synchronized ( impl ) {
            super.readTile( buffer, tile );
        }
    }

    public void writeTile( Object buffer, NDShape tile ) throws IOException {
        synchronized ( impl ) {
            super.writeTile( buffer, tile );
        }
    }

    public void close() {
        doClose();
        offset = -1L;
    }

}
