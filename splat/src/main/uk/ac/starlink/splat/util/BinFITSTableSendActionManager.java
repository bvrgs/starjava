/*
 * Copyright (C) 2009 Science and Technology Facilities Council
 *
 *  History:
 *     06-MAR-2009 (Mark Taylor):
 *        Original version.
 *     14-JUL-2009 (Peter Draper):
 *        Give up on 1D FITS and always transmit FITS tables.
 *     16-OCT-2009 (Peter Draper):
 *        Send SSA meta-data as required by HIPE (paul.balm@sciops.esi.int)
 *        More SSA 1.0 compatible.
 */
package uk.ac.starlink.splat.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.event.ListSelectionListener;

import org.astrogrid.samp.Message;
import org.astrogrid.samp.gui.GuiHubConnector;

import uk.ac.starlink.splat.data.SpecData;
import uk.ac.starlink.splat.data.SpecDataFactory;
import uk.ac.starlink.splat.vo.SSAQueryBrowser;
import uk.ac.starlink.util.URLUtils;

/**
 * Provides GUI actions for sending spectra by SAMP.
 *
 * @author Mark Taylor
 * @author David Andresic
 * @version $Id$
 */
public class BinFITSTableSendActionManager
    extends SpectraAsTablesSendActionManager
{
    private static final String MTYPE = "table.load.fits";
    private static final String SENDTYPE = "table";
	
    
    /**
     * Constructor.
     *
     * @param  ssaQueryBrowser  SSA Query Browser instance
     * @param  hubConnector  controls connection with SAMP hub
     */
    public BinFITSTableSendActionManager( SSAQueryBrowser ssaQueryBrowser,
                                      GuiHubConnector hubConnector )
    {
        super( ssaQueryBrowser, hubConnector, MTYPE,
        		SENDTYPE );
        updateSpecState();
    }
    
    /**
     * Constructor.
     *
     * @param  specList  global list of spectra
     * @param  hubConnector  controls connection with SAMP hub
     */
    public BinFITSTableSendActionManager( JList specList,
                                      GuiHubConnector hubConnector )
    {
        super( specList, hubConnector, MTYPE,
        		SENDTYPE );
        specList.addListSelectionListener( this );
        updateSpecState();
    }

    /**
     * Constructs and returns a message for transmitting load of the
     * currently selected spectrum.
     */
    protected List<Message> createMessages()
        throws IOException, SplatException
    {
    	List<Message> messages = new LinkedList<Message>();
    	
    	for (SpecData spec : getSpecData()) {
    		String mime = null;
            URL locUrl = null;
            File tmpFile = null;

            //  Write spectrum as a FITS table and use that. Note we cannot
            //  find out if a FITS table already exists as StarTables are
            //  anonymous. Use "fits-basic" as SPLAT gets distracted by the
            //  primary array.
            if ( locUrl == null ) {
                tmpFile = File.createTempFile( "spec", ".fits");
                tmpFile.deleteOnExit();
//                locUrl = URLUtils.makeFileURL( tmpFile );
                locUrl = getURLOfSpec(spec) == null ? URLUtils.makeFileURL( tmpFile ) : new URL(getURLOfSpec(spec));
                mime = "application/fits";
                spec = SpecDataFactory.getInstance()
                    .getTableClone( spec, tmpFile.toString(),
                                    "fits-basic" );
                spec.save();
                assert tmpFile.exists() : tmpFile;
            }
            assert mime != null;
            assert locUrl != null;

            //  Prepare a metadata map describing the spectrum.
            //  There should probably be more items in here.
            Map meta = new HashMap();
            meta.put( "Access.Reference", locUrl.toString() );
            meta.put( "Access.Format", mime );
            String shortName = spec.getShortName();
            if ( shortName != null && shortName.trim().length() > 0 ) {
                meta.put( "vox:image_title", shortName );
                meta.put( "Target.Name", shortName );
            }

            //  Units.
            String dataUnits = spec.getDataUnits();
            String coordUnits = spec.getFrameSet().getUnit( 1 );
            if ( dataUnits != null && coordUnits != null ) {
                if ( ! coordUnits.equals( "" ) ) {
                    meta.put( "vox:spectrum_units",
                              coordUnits + " " + dataUnits );
                    meta.put( "Spectrum.Char.SpectralAxis.unit", coordUnits );
                    meta.put( "Spectrum.Char.FluxAxis.unit", dataUnits );
                }
            }

            //  Columns.
            String xColName = spec.getXDataColumnName();
            String yColName = spec.getYDataColumnName();
            if ( xColName != null && yColName != null ) {
                meta.put( "vox:spectrum_axes", xColName + " " + yColName );
                meta.put( "Spectrum.Char.SpectralAxis.Name", xColName );
                meta.put( "Spectrum.Char.FluxAxis.Name", yColName );
            }

            //  Prepare and return the actual message.
            Message msg = new Message( MTYPE );
            msg.addParam( "url", locUrl.toString() );
            msg.addParam( "meta", meta );
            if ( shortName != null && shortName.trim().length() > 0 ) {
                msg.addParam( "name", shortName );
            }
            messages.add(msg);
    	}
    	
    	return messages;
    }
    
    @Override
    public JMenu createSendMenu() {
    	switch (getSpectraSource()) {
		case JLIST:
			return super.createSendMenu("Send spectrum as FITS table to...");
		case SSAP_BROWSER:
			return super.createSendMenu("Send result spectra as FITS table to...");
		default:
			throw new IllegalStateException("Unsupported source.");
		}
    }
    
    
}
