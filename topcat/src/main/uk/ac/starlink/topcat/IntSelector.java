package uk.ac.starlink.topcat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * Some sort of component which allows the user to select an integer.
 * At time of writing this is only used in one context, but could be
 * generalised for other uses, in which case it might need some less
 * specific methods/constructors.
 *
 * @author   Mark Taylor (Starlink)
 * @since    7 Oct 2004
 */
public class IntSelector extends JPanel implements ItemListener {

    private final JComboBox cbox_;
    private int selected_;

    /**
     * Constructs a new selector with a set of preselected options.
     * Other options can be chosen by the user.
     *
     * @param  initial selections
     */
    public IntSelector( int[] options ) {
        super( new BorderLayout() );
        if ( options != null ) {
            int nopt = options.length;
            Object[] items = new Object[ nopt ];
            for ( int i = 0; i < nopt; i++ ) {
                items[ i ] = new Integer( options[ i ] );
            }
            cbox_ = new JComboBox( items );
        }
        else {
            cbox_ = new JComboBox();
        }
        cbox_.setEditable( true );
        cbox_.addItemListener( this );
        cbox_.setPreferredSize(
                  new Dimension( 64, cbox_.getPreferredSize().height ) );
        add( cbox_ );
    }

    /**
     * Returns the currently selected value.
     *
     * @return  selected integer
     */
    public int getValue() {
        return selected_;
    }

    /**
     * Sets the seleced value.
     *
     * @param  value  selected integer
     */
    public void setValue( int value ) {
        cbox_.setSelectedItem( new Integer( value ) );
    }

    public void setEnabled( boolean enabled ) {
        cbox_.setEnabled( enabled );
    }

    /**
     * Implements ItemListener interface, called when the selection is
     * changed.  This maintains the value of the currently selected value
     * and validates it.
     */
    public void itemStateChanged( ItemEvent evt ) {
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            Object item = evt.getItem();
            if ( item instanceof Integer ) {
                selected_ = ((Integer) item).intValue();
            }
            else if ( item instanceof String ) {
                try {
                    selected_ = Integer.parseInt( (String) item );
                }
                catch ( NumberFormatException e ) {
                    Toolkit.getDefaultToolkit().beep();
                    cbox_.setSelectedItem( new Integer( selected_ ) );
                }
            }
            else {  // can't happen?
                Toolkit.getDefaultToolkit().beep();
                cbox_.setSelectedItem( new Integer( selected_ ) );
            }
        }
    }
}
