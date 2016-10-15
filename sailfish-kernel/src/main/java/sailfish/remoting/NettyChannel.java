package sailfish.remoting;

public class NettyChannel implements Channel {
    private int channelIndex = 0;
    private final io.netty.channel.Channel[] channels;
    public NettyChannel(int connections) {
        channels = new io.netty.channel.Channel[connections];
    }

    @Override
    public void addChannel(io.netty.channel.Channel channel) {
        channels[++channelIndex] = channel;
    }
}
