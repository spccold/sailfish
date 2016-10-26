package sailfish.remoting;

import sailfish.remoting.protocol.Protocol;
import sailfish.remoting.utils.RemotingUtils;

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

    @Override
    public void close() {
        for(io.netty.channel.Channel channel : channels){
            RemotingUtils.closeChannel(channel);
        }
    }

    @Override
    public void send(Protocol protocol) {
        channels[0].writeAndFlush(protocol);
    }
}
