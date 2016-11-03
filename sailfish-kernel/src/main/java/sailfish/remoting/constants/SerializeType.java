package sailfish.remoting.constants;

public interface SerializeType {
    //pure bytes, no need any serialize and deserialize
    byte NON_SERIALIZE        = 0;
    //java platform
    byte JDK_SERIALIZE        = 1;
    //java platform with high performance
    byte KRYO_SERIALIZE       = 2;
    byte FST_SERIALIZE        = 3;
    //cross-platform
    byte JSON_SERIALIZE       = 4;
    //cross-platform with high performance
    byte HESSIAN_SERIALIZE    = 5;
    byte AVRO_SERIALIZE       = 6;
    byte THRIFT_SERIALIZE     = 7;
    byte PROTOBUF_SERIALIZE   = 8;
    byte FLATBUFFER_SERIALIZE = 9;
}
