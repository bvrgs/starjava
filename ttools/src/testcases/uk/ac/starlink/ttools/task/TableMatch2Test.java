package uk.ac.starlink.ttools.task;

import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.starlink.table.ColumnData;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.Tables;
import uk.ac.starlink.task.UsageException;
import uk.ac.starlink.ttools.QuickTable;
import uk.ac.starlink.ttools.TableTestCase;
import uk.ac.starlink.ttools.join.MatchEngineParameter;

public class TableMatch2Test extends TableTestCase {

    private final StarTable t1_;
    private final StarTable t2_;

    public TableMatch2Test( String name ) {
        super( name );

        t1_ = new QuickTable( 3, new ColumnData[] {
            col( "X", new double[] { 1134.822, 659.68, 909.613 } ),
            col( "Y", new double[] { 599.247, 1046.874, 543.293 } ),
            col( "Vmag", new double[] { 13.8, 17.2, 9.3 } ),
        } );
        t2_ = new QuickTable( 4, new ColumnData[] {
            col( "X", new double[] { 909.523, 1832.114, 1135.201, 702.622 } ),
            col( "Y", new double[] { 543.800, 409.567, 600.100, 1004.972 } ),
            col( "Bmag", new double[] { 10.1, 12.3, 14.6, 19.0 } ),
        } );

        Logger.getLogger( "uk.ac.starlink.ttools.task" )
              .setLevel( Level.WARNING );
    }

    public void testCols() throws Exception {
        
        String[] cols12sep = new String[] { "X_1", "Y_1", "Vmag",
                                            "X_2", "Y_2", "Bmag",
                                            "Separation" };
        assertArrayEquals(
            cols12sep,
            getColNames( join12( "1and2", "best", 1.0 ) ) );
        assertArrayEquals(
            cols12sep,
            getColNames( join12( "1or2", "all", 1.0 ) ) );
        assertArrayEquals(
            cols12sep,
            getColNames( join12( "all1", "all", 1.0 ) ) );
        assertArrayEquals(
            cols12sep,
            getColNames( join12( "all2", "all", 1.0 ) ) );

        assertArrayEquals(
            new String[] { "X_1", "Y_1", "Vmag", "X_2", "Y_2", "Bmag" },
            getColNames( join12( "1xor2", "all", 1.0 ) ) );
        assertArrayEquals(
            new String[] { "X_1", "Y_1", "Vmag", "X_2", "Y_2", "Bmag" },
            getColNames( join12( "1and2", "all", 0.5 ) ) );

        assertArrayEquals(
            new String[] { "X", "Y", "Vmag" },
            getColNames( join12( "1not2", "best", 1.0 ) ) );
        assertArrayEquals(
            new String[] { "X", "Y", "Bmag" },
            getColNames( join12( "2not1", "all", 1.0 ) ) );

        assertArrayEquals(
            cols12sep,
            getColNames( join12( "1and2", "best", 1.0, null, null ) ) );
        assertArrayEquals(
            new String[] { "X", "Y", "Vmag", "X", "Y", "Bmag", "Separation" },
            getColNames( join12( "1and2", "best", 1.0, "", "" ) ) );
        assertArrayEquals(
            new String[] { "X_1", "Y_1", "Vmag", "X", "Y", "Bmag",
                           "Separation" },
            getColNames( join12( "1and2", "best", 1.0, null, "" ) ) );

        assertArrayEquals(
            new String[] { "X-a", "Y-a", "Vmag", "X-b", "Y-b", "Bmag",
                           "Separation" },
            getColNames( join12( "1or2", "best", 1.0, "-a", "-b" ) ) );
    }

    public void testRows() throws Exception {
        assertEquals( 2L, join12( "1and2", "best", 1.0 ).getRowCount() );
        assertEquals( 5L, join12( "1or2", "best", 1.0 ).getRowCount() );
        assertEquals( 3L, join12( "all1", "best", 1.0 ).getRowCount() );
        assertEquals( 4L, join12( "all2", "best", 1.0 ).getRowCount() );
        assertEquals( 1L, join12( "1not2", "best", 1.0 ).getRowCount() );
        assertEquals( 2L, join12( "2not1", "best", 1.0 ).getRowCount() );
        assertEquals( 3L, join12( "1xor2", "best", 1.0 ).getRowCount() );
    }

    public void testAnd() throws Exception {
        StarTable tAnd = join12( "1and2", "best", 1.0 );
        assertArrayEquals( box( new double[] { 1134.822, 909.613 } ),
                           getColData( tAnd, 0 ) );
        assertArrayEquals( box( new double[] { 600.100, 543.800 } ),
                           getColData( tAnd, 4 ) );
        assertArrayEquals( new double[] { 0.933, 0.515 },
                           unbox( getColData( tAnd, 6 ) ), 0.0005 );

        assertEquals( 2, join12( "1and2", "best", 0.934 ).getRowCount() );
        assertEquals( 1, join12( "1and2", "best", 0.932 ).getRowCount() );
        assertEquals( 1, join12( "1and2", "best", 0.516 ).getRowCount() );
        assertEquals( 0, join12( "1and2", "best", 0.514 ).getRowCount() );
    }

    public void testNot() throws Exception {
        StarTable tNot = join12( "1not2", "best", 1.0 );
        assertArrayEquals( new double[] { 659.68, 1046.874, 17.2 },
                           unbox( tNot.getRow( 0 ) ) );
        assertEquals( 1L, tNot.getRowCount() );
    }

    public void testExamples() throws UsageException {
        String[] examps = MatchEngineParameter.getExampleValues();
        MatchEngineParameter matcherParam =
            new MatchEngineParameter( "matcher" );
        for ( int i = 0; i < examps.length; i++ ) {
            assertTrue( matcherParam.createEngine( examps[ i ] ) != null );
        }
    }

    private StarTable join12( String join, String find, double err )
            throws Exception {
        return join12( join, find, err, null, null );
    }

    private StarTable join12( String join, String find, double err,
                              String duptag1, String duptag2 )
            throws Exception {
        MapEnvironment env = new MapEnvironment()
                            .setValue( "in1", t1_ )
                            .setValue( "in2", t2_ )
                            .setValue( "matcher", "2d" )
                            .setValue( "values1", "X Y" )
                            .setValue( "values2", "X Y" )
                            .setValue( "params", Double.toString( err ) )
                            .setValue( "join", join )
                            .setValue( "find", find );
        if ( duptag1 != null ) {
            env.setValue( "duptag1", duptag1 );
        }
        if ( duptag2 != null ) {
            env.setValue( "duptag2", duptag2 );
        }
        new TableMatch2().createExecutable( env ).execute();
        StarTable result = env.getOutputTable( "omode" );
        if ( result != null ) {
            Tables.checkTable( result );
        }
        return result;
    }
}
