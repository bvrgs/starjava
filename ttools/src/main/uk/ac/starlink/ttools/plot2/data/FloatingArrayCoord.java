package uk.ac.starlink.ttools.plot2.data;

import java.util.function.Function;
import uk.ac.starlink.table.Domain;
import uk.ac.starlink.table.DomainMapper;
import uk.ac.starlink.table.ValueInfo;
import uk.ac.starlink.ttools.plot2.PlotUtil;

/**
 * Coord implementation for a variable-length array of floating point values.
 * This covers both single and double precision.
 *
 * @author   Mark Taylor
 * @since    15 Jul 2013
 */
public abstract class FloatingArrayCoord extends SingleCoord {

    /* Implementations could be improved: in many cases the number of elements
     * will be the same for every row.  The implementation could be smart
     * enough to spot this and optimise storage accordingly. */

    /**
     * Constructor.
     *
     * @param  meta  input value metadata
     * @param  isRequired   true if this coordinate is required for plotting
     * @param  isDouble  true for double precision, false for single
     */
    private FloatingArrayCoord( InputMeta meta, boolean isRequired,
                                boolean isDouble ) {
        super( meta, isRequired, ArrayDomain.INSTANCE,
               isDouble ? StorageType.DOUBLE_ARRAY : StorageType.FLOAT_ARRAY );
    }

    /**
     * Reads an array value from an appropriate column in a given tuple.
     *
     * @param  tuple  tuple
     * @param  icol  index of field in tuple corresponding to this Coord
     * @return  value of floating array field
     */
    public abstract double[] readArrayCoord( Tuple tuple, int icol );

    /**
     * Returns the length of an array value at an appropriate column
     * in a given Tuple.
     *
     * @param  tuple  tuple
     * @param  icol  index of column in tuple corresponding to this Coord
     * @return   array length
     */
    public abstract int getArrayCoordLength( Tuple tuple, int icol );

    /**
     * Returns a list of the classes which can be used as single user
     * coordinate values for floating array coordinates.
     * It's all the primitive numeric array types.
     */
    public static Class<?>[] getAcceptableClasses() {
        return new Class<?>[] {
            double[].class,
            float[].class,
            long[].class,
            int[].class,
            short[].class,
            byte[].class,
        };
    }

    /**
     * Constructs a new FloatingArrayCoord.
     *
     * @param  meta   input value metadata
     * @param  isRequired   true if this coordinate is required for plotting
     * @return   new coord
     */
    public static FloatingArrayCoord createCoord( InputMeta meta,
                                                  boolean isRequired ) {
        if ( PlotUtil.storeFullPrecision() ) {

            /* Double precision implementation. */
            final double[] d0 = new double[ 0 ];
            return new FloatingArrayCoord( meta, isRequired, true ) {
                public Function<Object[],double[]>
                       inputStorage( ValueInfo[] infos, DomainMapper[] dms ) {
                    Class<?> clazz = infos[ 0 ].getContentClass();
                    if ( double[].class.equals( clazz ) ) {
                        return values -> {
                            Object array = values[ 0 ];
                            return array instanceof double[]
                                 ? (double[]) array
                                 : d0;
                        };
                    }
                    else if ( float[].class.equals( clazz ) ) {
                        return values -> {
                            Object array = values[ 0 ];
                            if ( array instanceof float[] ) {
                                float[] fa = (float[]) array;
                                int n = fa.length;
                                double[] da = new double[ n ];
                                for ( int i = 0; i < n; i++ ) {
                                    da[ i ] = fa[ i ];
                                }
                                return da;
                            }
                            else {
                                return d0;
                            }
                        };
                    }
                    else if ( long[].class.equals( clazz ) ) {
                        return values -> {
                            Object array = values[ 0 ];
                            if ( array instanceof long[] ) {
                                long[] la = (long[]) array;
                                int n = la.length;
                                double[] da = new double[ n ];
                                for ( int i = 0; i < n; i++ ) {
                                    da[ i ] = (double) la[ i ];
                                }
                                return da;
                            }
                            else {
                                return d0;
                            }
                        };
                    }
                    else if ( int[].class.equals( clazz ) ) { 
                        return values -> {
                            Object array = values[ 0 ];
                            if ( array instanceof int[] ) {
                                int[] ia = (int[]) array;
                                int n = ia.length;
                                double[] da = new double[ n ];
                                for ( int i = 0; i < n; i++ ) {
                                    da[ i ] = ia[ i ];
                                }
                                return da;
                            }
                            else {
                                return d0;
                            }
                        };
                    }
                    else if ( short[].class.equals( clazz ) ) {
                        return values -> {
                            Object array = values[ 0 ];
                            if ( array instanceof short[] ) {
                                short[] sa = (short[]) array;
                                int n = sa.length;
                                double[] da = new double[ n ];
                                for ( int i = 0; i < n; i++ ) {
                                    da[ i ] = sa[ i ];
                                }
                                return da;
                            }
                            else {
                                return d0;
                            }
                        };
                    }
                    else if ( byte[].class.equals( clazz ) ) {
                        return values -> {
                            Object array = values[ 0 ];
                            if ( array instanceof byte[] ) {
                                byte[] ba = (byte[]) array;
                                int n = ba.length;
                                double[] da = new double[ n ];
                                for ( int i = 0; i < n; i++ ) {
                                    da[ i ] = ba[ i ];
                                }
                                return da;
                            }
                            else {
                                return d0;
                            }
                        };
                    }
                    else {
                        assert false;
                        return values -> d0;
                    }
                }

                public int getArrayCoordLength( Tuple tuple, int icol ) {
                    return ((double[]) tuple.getObjectValue( icol )).length;
                }
 
                public double[] readArrayCoord( Tuple tuple, int icol ) {
                    double[] dval = (double[]) tuple.getObjectValue( icol );
                    return dval;
                }
            };
        }
        else {

            /* Single precision implementation. */
            final float[] f0 = new float[ 0 ];
            return new FloatingArrayCoord( meta, isRequired, false ) {
                public Function<Object[],float[]>
                        inputStorage( ValueInfo[] infos, DomainMapper[] dms ) {
                    Class<?> clazz = infos[ 0 ].getContentClass();
                    if ( float[].class.equals( clazz ) ) {
                        return values -> {
                            Object array = values[ 0 ];
                            return array instanceof float[]
                                 ? (float[]) array
                                 : f0;
                        };
                    }
                    else if ( double[].class.equals( clazz ) ) {
                        return values -> {
                            Object array = values[ 0 ];
                            if ( array instanceof double[] ) {
                                double[] da = (double[]) array;
                                int n = da.length;
                                float[] fa = new float[ n ];
                                for ( int i = 0; i < n; i++ ) {
                                    fa[ i ] = (float) da[ i ];
                                }
                                return fa;
                            }
                            else {
                                return f0;
                            }
                        };
                    }
                    else if ( long[].class.equals( clazz ) ) {
                        return values -> {
                            Object array = values[ 0 ];
                            if ( array instanceof long[] ) {
                                long[] la = (long[]) array;
                                int n = la.length;
                                float[] fa = new float[ n ];
                                for ( int i = 0; i < n; i++ ) {
                                    fa[ i ] = (float) la[ i ];
                                }
                                return fa;
                            }
                            else {
                                return f0;
                            }
                        };
                    }
                    else if ( int[].class.equals( clazz ) ) {
                        return values -> {
                            Object array = values[ 0 ];
                            if ( array instanceof int[] ) {
                                int[] ia = (int[]) array;
                                int n = ia.length;
                                float[] fa = new float[ n ];
                                for ( int i = 0; i < n; i++ ) {
                                    fa[ i ] = (float) ia[ i ];
                                }
                                return fa;
                            }
                            else {
                                return f0;
                            }
                        };
                    }
                    else if ( short[].class.equals( clazz ) ) {
                        return values -> {
                            Object array = values[ 0 ];
                            if ( array instanceof short[] ) {
                                short[] sa = (short[]) array;
                                int n = sa.length;
                                float[] fa = new float[ n ];
                                for ( int i = 0; i < n; i++ ) {
                                    fa[ i ] = sa[ i ];
                                }
                                return fa;
                            }
                            else {
                                return f0;
                            }
                        };
                    }
                    else if ( byte[].class.equals( clazz ) ) {
                        return values -> {
                            Object array = values[ 0 ];
                            if ( array instanceof byte[] ) {
                                byte[] ba = (byte[]) array;
                                int n = ba.length;
                                float[] fa = new float[ n ];
                                for ( int i = 0; i < n; i++ ) {
                                    fa[ i ] = ba[ i ];
                                }
                                return fa;
                            }
                            else {
                                return f0;
                            }
                        };
                    }
                    else {
                        assert false;
                        return values -> f0;
                    }
                }

                public int getArrayCoordLength( Tuple tuple, int icol ) {
                    return ((float[]) tuple.getObjectValue( icol )).length;
                }

                public double[] readArrayCoord( Tuple tuple, int icol ) {
                    float[] fval = (float[]) tuple.getObjectValue( icol );
                    int n = fval.length;
                    double[] dval = new double[ n ];
                    for ( int i = 0; i < n; i++ ) {
                        dval[ i ] = (double) fval[ i ];
                    }
                    return dval;
                }
            };
        }
    }

    /**
     * Domain for numeric array values.
     */
    private static class ArrayDomain implements Domain<ArrayMapper> {

        /** Singleton instance. */
        static final ArrayDomain INSTANCE = new ArrayDomain();

        public String getDomainName() {
            return "Array";
        }

        public ArrayMapper[] getMappers() {
            return new ArrayMapper[] { ArrayMapper.INSTANCE };
        }

        public ArrayMapper getProbableMapper( ValueInfo info ) {
            Class<?> clazz = info.getContentClass();
            for ( Class<?> c : getAcceptableClasses() ) {
                if ( c.equals( clazz ) ) {
                    return ArrayMapper.INSTANCE;
                }
            }
            return null;
        }

        public ArrayMapper getPossibleMapper( ValueInfo info ) {
            return getProbableMapper( info );
        }
    }

    /**
     * Default mapper for ArrayDomain.
     */
    private static class ArrayMapper implements DomainMapper {

        /** Singleton instance. */
        static final ArrayMapper INSTANCE = new ArrayMapper();

        public ArrayDomain getTargetDomain() {
            return ArrayDomain.INSTANCE;
        }

        public Class<?> getSourceClass() {
            return Object.class;
        }

        public String getSourceName() {
            return "array";
        }

        public String getSourceDescription() {
            return "array-valued quantity";
        }
    }
}
