package uk.ac.starlink.ndx;

import java.io.IOException;
import java.net.URL;
import javax.xml.transform.Source;
import org.w3c.dom.Element;
import uk.ac.starlink.array.Requirements;
import uk.ac.starlink.array.NDArray;
import uk.ac.starlink.ast.FrameSet;

/**
 * N-dimensional astronomical data.
 * An Ndx represents an N-dimensional hypercuboid of pixels 
 * (the <i>image</i> data), plus associated auxiliary information,
 * in particular an optional array of pixel variances, and of 
 * pixel quality flags.  Metadata such as history information and
 * coordinate system information may also be included, as well as
 * user-defined extension data of unrestricted scope, represented in XML.
 *
 * @author Norman Gray
 * @author Mark Taylor
 * @author Peter W. Draper.
 * @version $Id$
 */
public interface Ndx {

    /**
     * Returns the image component of this NDX.
     * <p>
     * <b>Note</b> that this returns the raw image <tt>NDArray</tt> object,
     * which should not in general be used directly for reading image
     * data, since it has not been masked by a quality component if present.
     * To obtain a properly masked version of the image data for read access,
     * use the {@link Ndxs#getMaskedImage(Ndx)} method.
     *
     * @return   the NDArray representing the image component
     */
    NDArray getImage();

    /**
     * Indicates whether there is a variance component.
     *
     * @return   true if {@link #getVariance} may be called
     */
    boolean hasVariance();

    /**
     * Returns the variance component of this NDX.
     * <p>
     * <b>Note</b> that this returns the raw variance <tt>NDArray</tt> object,
     * which should not in general be used directly for reading variance
     * data, since it has not been masked by a quality component if present.
     * To obtain a properly masked version of the variance data for read access,
     * use the {@link Ndxs#getMaskedVariance(Ndx)} method.
     * <p>
     * May only be called if {@link #hasVariance} returns <tt>true</tt>.
     *
     * @return   an NDArray representing the variance component, 
     * @throws   UnsupportedOperationException  if <tt>hasVariance</tt>
     *           returns <tt>false</tt>
     */
    NDArray getVariance();

    /**
     * Indicates whether there is a quality component. 
     *
     * @return   true if {@link #getQuality} may be called
     */
    boolean hasQuality();

    /**
     * Returns the quality component of this NDX.
     * <p>
     * May only be called if {@link #hasQuality} returns <tt>true</tt>.
     *
     * @return  an NDArray of integer type representing the quality component, 
     * @throws  UnsupportedOperationException  if <tt>hasQuality</tt>
     *          returns <tt>false</tt>
     */
    NDArray getQuality();

    /**
     * Indicates whether there is a title component.
     *
     * @return   true if {@link #getTitle} may be called
     */
    boolean hasTitle();

    /**
     * Returns the title of this Ndx.
     * May only be called if {@link #hasTitle} returns <tt>true</tt>.
     *
     * @return  the title
     * @throws  UnsupportedOperationException  if <tt>hasTitle</tt> 
     *          returns <tt>false</tt>
     */
    String getTitle();

    /**
     * Find out if the NDX contains user-defined extension information.
     *
     * @return true if {@link #getEtc} may be called
     */
    boolean hasEtc();

    /**
     * Returns the XML containing extension information for this NDX.
     * The base element of the returned Source is an element of type 
     * <tt>&lt;etc&gt;</tt> which contains an element for each extension.
     * May only be called if {@link #hasEtc} returns <tt>true</tt>.
     *
     * @return  an XML Source containing any user-defined extension information.
     * @throws  UnsupportedOperationException  if <tt>hasEtc</tt>
     *          returns <tt>false</tt>
     * @see     uk.ac.starlink.util.SourceReader
     */
    Source getEtc();

    /**
     * Find out if the NDX has World Coordinate System information.
     * <p>
     * If it exists, then the {@link #getAst} method may be called to
     * access it as an AST {@link uk.ac.starlink.ast.FrameSet}.
     * <p>
     * <i>Note:</i> in due course, when the <tt>uk.ac.starlink.wcs</tt>
     * package has been released a <tt>getWCS</tt> method will be
     * provided to access it as a <tt>WCS</tt> object.
     *
     * @return  true if {@link #getAst} (and in due course <tt>getWCS</tt>
     *          can be called
     */
    boolean hasWCS();

    /**
     * Get the world coordinate system of the NDX as an AST <tt>FrameSet</tt>.
     * <p>
     * <i>Note:</i> This method is intended as a temporary measure 
     * until the <tt>uk.ac.starlink.wcs</tt> package has been released.
     * At that time a <tt>getWCS</tt> method will be provided, and 
     * this one will be deprecated.
     *
     * @return the AST FrameSet representing the world coordinate system
     *         information
     * @throws UnsupportedOperationException  if <tt>hasWCS</tt>
     *         returns <tt>false</tt>
     * @see    #hasWCS
     */
    FrameSet getAst();

    /**
     * Gets the value of the badBits mask.
     * This value is used in conjunction
     * with the quality array to determine which pixels are bad; 
     * a pixel is bad if the logical AND of its quality value and the
     * bad bits mask is not zero; hence a value of zero has no effect.
     * Has no effect if there is no quality array.
     * 
     * @return   the bad bits mask
     */
    int getBadBits();

    /**
     * Indicates whether this Ndx represents a persistent object.
     * If this returns true, then the array components in the XML source
     * generated by the {@link #toXML} method all contain URLs referencing 
     * genuine resources.  If false, then this Ndx is in some sense 
     * virtual, and one or more of the array elements in the the XML
     * generated by <tt>toXML</tt> will reference phantom resources.
     *
     * @return   true if an XML representation capable of containing the 
     *           full state of this Ndx can be generated
     */
    boolean isPersistent();
 
    /**
     * Generates an XML view of this Ndx object as a 
     * {@link javax.xml.transform.Source}.  Note that the
     * array components (image, variance, quality) of this Ndx will only
     * be recoverable from the returned XML in the case that the 
     * {@link #isPersistent} method returns true.
     * <p>
     * The XML in general may contain URLs, for instance referencing the
     * array components of the NDX.  How these are written is determined
     * by the <tt>base</tt> parameter; URLs will be written as relative
     * URLs relative to <tt>base</tt> if this is possible (e.g. if they
     * share a part of their path).  If there is no common part of the
     * path, including the case in which <tt>base</tt> is <tt>null</tt>,
     * then an absolute reference will be written.
     *
     * @param  base  the base URL against which URLs written within the XML
     *           are considered relative.  If null, all are written absolute.
     * @return   an XML Source representation of this Ndx
     * @see     uk.ac.starlink.util.SourceReader
     */
    Source toXML( URL base );
}
