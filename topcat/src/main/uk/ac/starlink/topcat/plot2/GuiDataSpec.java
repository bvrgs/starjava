package uk.ac.starlink.topcat.plot2;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import uk.ac.starlink.table.ColumnData;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.ValueInfo;
import uk.ac.starlink.topcat.RowSubset;
import uk.ac.starlink.topcat.TopcatModel;
import uk.ac.starlink.ttools.plot2.Slow;
import uk.ac.starlink.ttools.plot2.data.AbstractDataSpec;
import uk.ac.starlink.ttools.plot2.data.Coord;
import uk.ac.starlink.ttools.plot2.data.DataSpec;
import uk.ac.starlink.ttools.plot2.data.UserDataReader;

/**
 * DataSpec implementation used by TOPCAT classes.
 *
 * @author   Mark Taylor
 * @since    13 Mar 2013
 */
public class GuiDataSpec extends AbstractDataSpec {

    private final TopcatModel tcModel_;
    private final RowSubset subset_;
    private final GuiCoordContent[] contents_;

    /**
     * Constructor.
     *
     * @param  tcModel  topcat model supplying data
     * @param  subset  row inclusion mask
     * @param  contents   coordinate value definitions
     */
    public GuiDataSpec( TopcatModel tcModel, RowSubset subset,
                        GuiCoordContent[] contents ) {
        tcModel_ = tcModel;
        subset_ = subset;
        contents_ = contents;
    }

    public StarTable getSourceTable() {
        return tcModel_.getDataModel();
    }

    public int getCoordCount() {
        return contents_.length;
    }

    public Coord getCoord( int ic ) {
        return contents_[ ic ].getCoord();
    }

    public Object getCoordId( int ic ) {
        return Arrays.asList( contents_[ ic ].getDataLabels() );
    }

    public Object getMaskId() {
        return subset_;
    }

    public ValueInfo[] getUserCoordInfos( int ic ) {
        ColumnData[] colDatas = contents_[ ic ].getColDatas();
        int nu = colDatas.length;
        ValueInfo[] infos = new ValueInfo[ nu ];
        for ( int iu = 0; iu < nu; iu++ ) {
            infos[ iu ] = colDatas[ iu ].getColumnInfo();
        }
        return infos;
    }

    public UserDataReader createUserDataReader() {
        int ncoord = contents_.length;
        final Object[][] userRows = new Object[ ncoord ][];
        for ( int ic = 0; ic < ncoord; ic++ ) {
            GuiCoordContent content = contents_[ ic ];
            int nu = content.getDataLabels().length;
            assert content.getColDatas().length == nu;
            userRows[ ic ] = new Object[ nu ];
        }

        /* Different instances of this class need to be usable concurrently,
         * according to the DataSpec contract.  I *think* these are. */
        return new UserDataReader() {
            public boolean getMaskFlag( RowSequence rseq, long irow ) {
                return subset_.isIncluded( irow );
            }
            public Object[] getUserCoordValues( RowSequence rseq, long irow,
                                                int icoord )
                    throws IOException {
                ColumnData[] cdatas = contents_[ icoord ].getColDatas();
                int nu = cdatas.length;
                Object[] userRow = userRows[ icoord ];
                for ( int iu = 0; iu < nu; iu++ ) {
                    userRow[ iu ] = cdatas[ iu ].readValue( irow );
                }
                return userRow;
            }
        };
    }

    /**
     * Returns the number of rows associated with this data spec.
     * In most cases this will execute quickly, but if necessary a count
     * will be carried out by scanning the associated RowSubset.
     * The result may not be 100% reliable.  If the result is not known,
     * -1 may be returned, though this shouldn't happen.
     *
     * @return   number of tuples in this object's tuple sequence,
     *           or -1 if not known (shouldn't happen)
     */
    @Slow
    public long getRowCount() {

        /* If the row count for the relevant subset is already known,
         * use that. */
        Map subsetCounts = tcModel_.getSubsetCounts();
        Object countObj = subsetCounts.get( subset_ );
        if ( countObj instanceof Number ) {
            return ((Number) countObj).longValue();
        }

        /* If not, count it now. */
        else {
            assert countObj == null;
            long nrow = tcModel_.getDataModel().getRowCount();
            long count = 0;
            for ( long ir = 0; ir < nrow; ir++ ) {
                if ( subset_.isIncluded( ir ) ) {
                    count++;
                }
            }

            /* Having got the result, save it for later. */
            subsetCounts.put( subset_, new Long( count ) );
            return count;
        }
    }
}
