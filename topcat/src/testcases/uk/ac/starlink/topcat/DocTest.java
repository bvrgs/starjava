package uk.ac.starlink.topcat;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.ac.starlink.util.TestCase;

public class DocTest extends TestCase {

    public static final String DOC_NAME = "sun253";
    public static final String DOC_BUILD_DIR = "build/docs";
    public static final String DOC_SRC_DIR = "src/docs";

    File docFile = new File( DOC_BUILD_DIR, DOC_NAME + ".xml" );
    

    public DocTest( String name ) {
        super( name );
    }

    public void testValidity() throws IOException, SAXException {
        assertValidXML( new InputSource( docFile.toString() ) );
    }

    public void testLinks() 
            throws TransformerException, MalformedURLException {
        File docXslt1 = new File( DOC_SRC_DIR, "toHTML1.xslt" );
        File docXslt = new File( DOC_SRC_DIR, "toHTML.xslt" );
        File context = new File( DOC_BUILD_DIR, DOC_NAME );
        assertTrue( docXslt1.isFile() );
        assertTrue( docXslt.isFile() );
        assertTrue( docFile.isFile() );
        assertTrue( context.isDirectory() );
        assertTrue( LinkChecker.checkLinks( new StreamSource( docXslt1 ),
                                            new StreamSource( docFile ),
                                            context.toURL() ) );
        assertTrue( LinkChecker.checkLinks( new StreamSource( docXslt ),
                                            new StreamSource( docFile ),
                                            context.toURL() ) );
    }
}
