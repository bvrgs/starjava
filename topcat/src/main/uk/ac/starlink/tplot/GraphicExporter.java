package uk.ac.starlink.tplot;

import Acme.JPM.Encoders.GifEncoder;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import org.jibble.epsgraphics.EpsGraphics2D;

/**
 * Exports a graphical component to a graphics file.
 *
 * @author   Mark Taylor
 * @since    1 Aug 2008
 */
public abstract class GraphicExporter {

    private final String name_;
    private final String mimeType_;
    private final String[] fileSuffixes_;

    private static final Logger logger_ =
        Logger.getLogger( "uk.ac.starlink.plot" );

    /**
     * Constructor.
     *
     * @param   name  exporter name (usually graphics format name)
     * @param   mimeType  MIME type for this exporter's output format
     * @param   fileSuffixes  file suffixes which usually indicate the
     *          export format used by this instance (may be null)
     */
    protected GraphicExporter( String name, String mimeType,
                               String[] fileSuffixes ) {
        name_ = name;
        mimeType_ = mimeType;
        fileSuffixes_ = fileSuffixes == null ? new String[ 0 ]
                                             : (String[]) fileSuffixes.clone();
    }

    /**
     * Exports the graphic content of a given component to an output stream
     * using some graphics format or other.
     * The output stream should not be closed following the write.
     *
     * @param   comp  component to draw
     * @param   out   destination output stream
     */
    public abstract void exportGraphic( JComponent comp, OutputStream out )
            throws IOException;

    /**
     * Returns the name of this exporter (usually the graphics format name).
     *
     * @return  exporter name
     */
    public String getName() {
        return name_;
    }

    /**
     * Returns the MIME type for the graphics format used by this exporter.
     *
     * @return  MIME type string
     */
    public String getMimeType() {
        return mimeType_;
    }

    /**
     * Returns an array of file suffixes which usually indicate a file with
     * an export format used by this instance.
     *
     * @return  copy of file suffix list; may be empty but will not be null
     */
    public String[] getFileSuffixes() {
        return (String[]) fileSuffixes_.clone();
    }

    public String toString() {
        return name_;
    }

    /** Exports to JPEG format. */
    public static final GraphicExporter JPEG =
         new ImageIOExporter( "jpeg", "image/jpeg",
                              new String[] { ".jpg", ".jpeg" }, false );

    /** Exports to PNG format. */
    public static final GraphicExporter PNG =
         new ImageIOExporter( "png", "image/png",
                              new String[] { ".png" }, true );

    /**
     * Exports to GIF format.
     *
     * <p>There's something wrong with this - it ought to produce a
     * transparent background, but it doesn't.  I'm not sure why, or
     * even whether it's to do with the plot or the encoder.
     */
    public static final GraphicExporter GIF =
            new GraphicExporter( "gif", "image/gif",
                                 new String[] { ".gif", } ) {
        public void exportGraphic( JComponent comp, OutputStream out )
                throws IOException {

            /* Get component dimensions. */
            int w = comp.getWidth();
            int h = comp.getHeight();

            /* Create a BufferedImage to draw it onto. */
            BufferedImage image =
                new BufferedImage( w, h, BufferedImage.TYPE_4BYTE_ABGR );

            /* Set the background to transparent white. */
            Graphics2D g = image.createGraphics();
            g.setBackground( new Color( 0x00ffffff, true ) );
            g.clearRect( 0, 0, w, h );

            /* Draw the component onto the image. */
            comp.print( g );
            g.dispose();

            /* Count the number of colours represented in the resulting
             * image. */
            Set colors = new HashSet();
            for ( int ix = 0; ix < w; ix++ ) {
                for ( int iy = 0; iy < h; iy++ ) {
                    colors.add( new Integer( image.getRGB( ix, iy ) ) );
                }
            }

            /* If there are too many, redraw the image into an indexed image
             * instead.  This is necessary since the GIF encoder we're using
             * here just gives up if there are too many. */
            if ( colors.size() > 254 ) {
                logger_.warning( "GIF export colour map filled up - "
                               + "JPEG or PNG might do a better job" );

                /* Create an image with a suitable colour model. */
                IndexColorModel gifColorModel = getGifColorModel();
                image = new BufferedImage( w, h,
                                           BufferedImage.TYPE_BYTE_INDEXED,
                                           gifColorModel );

                /* Zero all pixels to the transparent colour. */
                WritableRaster raster = image.getRaster();
                int itrans = gifColorModel.getTransparentPixel();
                if ( itrans >= 0 ) {
                    byte[] pixValue = new byte[] { (byte) itrans };
                    for ( int ix = 0; ix < w; ix++ ) {
                        for ( int iy = 0; iy < h; iy++ ) {
                            raster.setDataElements( ix, iy, pixValue );
                        }
                    }
                }

                /* Draw the component on it. */
                Graphics2D gifG = image.createGraphics();

                /* Set dithering false.  But it still seems to dither on a
                 * drawImage!  Can't get to the bottom of it. */
                gifG.setRenderingHint( RenderingHints.KEY_DITHERING,
                                       RenderingHints.VALUE_DITHER_DISABLE );
                comp.print( gifG );
                gifG.dispose();
            }

            /* Write the image as a gif down the provided stream. */
            new GifEncoder( image, out ).encode();
        }
    };

    /** Exports to Encapsulated PostScript. */
    public static final GraphicExporter EPS =
            new GraphicExporter( "eps", "application/postscript",
                                 new String[] { ".eps", ".ps", } ) {
        public void exportGraphic( JComponent comp, OutputStream out )
                throws IOException {
        
            /* Scale to a pixel size which makes the bounding box sit
             * sensibly on an A4 or letter page.  EpsGraphics2D default
             * scale is 72dpi. */
            Rectangle bounds = comp.getBounds();
            double padfrac = 0.05;       
            double xdpi = bounds.width / 6.0;
            double ydpi = bounds.height / 9.0;
            double scale;
            int pad;
            if ( xdpi > ydpi ) {
                scale = 72.0 / xdpi;     
                pad = (int) Math.ceil( bounds.width * padfrac * scale );
            }           
            else {
                scale = 72.0 / ydpi;
                pad = (int) Math.ceil( bounds.height * padfrac * scale );
            }
            int xlo = (int) Math.floor( scale * bounds.x ) - pad;
            int ylo = (int) Math.floor( scale * bounds.y ) - pad;
            int xhi = (int) Math.ceil( scale * ( bounds.x + bounds.width ) )
                    + pad;
            int yhi = (int) Math.ceil( scale * ( bounds.y + bounds.height ) )
                    + pad;
            
            /* Construct a graphics object which will write postscript
             * down this stream. */
            EpsGraphics2D g2 = 
                new FixedEpsGraphics2D( "Plot", out, xlo, ylo, xhi, yhi );
            g2.scale( scale, scale );

            /* Do the drawing. */
            comp.print( g2 );

            /* Note this close call *must* be made, otherwise the
             * eps file is not flushed or correctly terminated.
             * This closes the output stream too. */ 
            g2.close();
        }
    };

    /**
     * GraphicExporter implementation which uses the ImageIO framework.
     */
    private static class ImageIOExporter extends GraphicExporter {
        private final String formatName_;
        private final int imageType_;
        private final boolean isSupported_;

        /**
         * Constructor.
         *
         * @param  formatName  ImageIO format name
         * @param  mimeType  MIME type for this exporter's output format
         * @param  transparent  true iff format is capable of supporting
         *                      transparency
         * @param   fileSuffixes  file suffixes which usually indicate the
         *          export format used by this instance (may be null)
         */
        ImageIOExporter( String formatName, String mimeType, 
                         String[] fileSuffixes, boolean transparent ) {
            super( formatName, mimeType, fileSuffixes );
            formatName_ = formatName;
            imageType_ = transparent ? BufferedImage.TYPE_INT_ARGB
                                     : BufferedImage.TYPE_INT_RGB;
            isSupported_ =
                ImageIO.getImageWritersByFormatName( formatName ).hasNext();
        }

        public void exportGraphic( JComponent comp, OutputStream out )
                throws IOException {
            if ( ! isSupported_ ) {
                throw new IOException( "Graphics export to " + formatName_
                                     + " not supported" );
            }

            /* Create an image buffer on which to paint. */
            int w = comp.getWidth();
            int h = comp.getHeight();
            BufferedImage image = new BufferedImage( w, h, imageType_ );
            Graphics2D g2 = image.createGraphics();

            /* Clear the background to transparent white.  Failing to do this
             * leaves all kinds of junk in the background. */
            Color color = g2.getColor();
            Composite compos = g2.getComposite();
            g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC ) );
            g2.setColor( new Color( 1f, 1f, 1f, 0f ) );
            g2.fillRect( 0, 0, w, h );
            g2.setColor( color );
            g2.setComposite( compos );

            /* Paint the graphics to the buffer. */
            comp.print( g2 );

            /* Export. */
            boolean done = ImageIO.write( image, formatName_, out );
            out.flush();
            if ( ! done ) {
                throw new IOException( "No handler for format " + formatName_ +
                                       " (surprising - thought there was)" );
            }
        }
    }

    /**
     * Returns a colour model suitable for use with GIF images.
     * It has a selection of RGB colours and one transparent colour.
     *
     * @return  standard GIF indexed colour model
     */
    private static IndexColorModel getGifColorModel() {

        /* Acquire a standard general-purpose 256-entry indexed colour model. */
        IndexColorModel rgbModel =
            (IndexColorModel)
            new BufferedImage( 1, 1, BufferedImage.TYPE_BYTE_INDEXED )
           .getColorModel();

        /* Get r/g/b entries from it. */
        byte[][] rgbs = new byte[ 3 ][ 256 ];
        rgbModel.getReds( rgbs[ 0 ] );
        rgbModel.getGreens( rgbs[ 1 ] );
        rgbModel.getBlues( rgbs[ 2 ] );

        /* Set one entry transparent. */
        int itrans = 254; 
        rgbs[ 0 ][ itrans ] = (byte) 255;
        rgbs[ 1 ][ itrans ] = (byte) 255;
        rgbs[ 2 ][ itrans ] = (byte) 255;
        IndexColorModel gifModel =
            new IndexColorModel( 8, 256, rgbs[ 0 ], rgbs[ 1 ], rgbs[ 2 ],
                                 itrans );

        /* Return the  model. */
        return gifModel;
    }
}
