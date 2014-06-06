package uk.ac.starlink.topcat.join;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JComponent;
import uk.ac.starlink.topcat.AuxWindow;

/**
 * Window for doing upload sky matches using the CDS X-Match service.
 *
 * @author   Mark Taylor
 * @since    14 May 2014
 */
public class UploadMatchWindow extends AuxWindow {

    /**
     * Constructor.
     *
     * @param  parent  parent window
     */
    public UploadMatchWindow( Component parent ) {
        super( "Upload Match", parent );
        JComponent main = getMainArea();
        UploadMatchPanel matchPanel =
            new UploadMatchPanel( placeProgressBar() );
        main.add( matchPanel, BorderLayout.CENTER );
        JComponent controls = getControlPanel();
        controls.add( new JButton( matchPanel.getStartAction() ) );
        controls.add( new JButton( matchPanel.getStopAction() ) );
        addHelp( "UploadMatchWindow" );
    }
}