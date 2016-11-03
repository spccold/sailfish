package sailfish.remoting.constants;

public interface CompressType {
    //TODO compressType with multiple mode, needs to perfect in future
    byte NON_COMPRESS     = 0;
    byte LZ4_COMPRESS     = 1;
    byte GZIP_COMPRESS    = 2;
    byte DEFLATE_COMPRESS = 3;
    byte SNAPPY_COMPRESS  = 4;
}