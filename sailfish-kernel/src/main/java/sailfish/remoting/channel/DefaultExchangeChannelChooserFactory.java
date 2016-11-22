/**
 *
 *	Copyright 2016-2016 spccold
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 *
 */
package sailfish.remoting.channel;

import java.util.concurrent.atomic.AtomicInteger;

import sailfish.remoting.exceptions.ExceptionCode;
import sailfish.remoting.exceptions.SailfishException;

/**
 * Default implementation which uses simple round-robin to choose next
 * {@link ExchangeChannel} until which {@link ExchangeChannel#isAvailable()}
 * return true or all children has been chosen.
 * 
 * @author spccold
 * @version $Id: DefaultExchangeChannelChooserFactory.java, v 0.1 2016年11月22日
 *          下午4:40:01 spccold Exp $
 */
public class DefaultExchangeChannelChooserFactory implements ExchangeChannelChooserFactory {

	public static final DefaultExchangeChannelChooserFactory INSTANCE = new DefaultExchangeChannelChooserFactory();

	private DefaultExchangeChannelChooserFactory() { }

	@Override
	public ExchangeChannelChooser newChooser(ExchangeChannel[] channels, ExchangeChannel[] deadChannels) {
		if (isPowerOfTwo(channels.length)) {
			return new PowerOfTowExchangeChannelChooser(channels, deadChannels);
		} else {
			return new GenericExchangeChannelChooser(channels, deadChannels);
		}
	}

	private static boolean isPowerOfTwo(int val) {
		return (val & -val) == val;
	}

	private static final class PowerOfTowExchangeChannelChooser implements ExchangeChannelChooser {
		private final AtomicInteger idx = new AtomicInteger();
		private final ExchangeChannel[] channels;
		private final ExchangeChannel[] deadChannels;

		PowerOfTowExchangeChannelChooser(ExchangeChannel[] channels, ExchangeChannel[] deadChannels) {
			this.channels = channels;
			this.deadChannels = deadChannels;
		}

		@Override
		public ExchangeChannel next() throws SailfishException {
			int arrayIndex = 0;
			int currentIndex = idx.getAndIncrement();
			for (int i = 0; i < channels.length; i++) {
				ExchangeChannel currentChannel = channels[arrayIndex = ((currentIndex++) & channels.length - 1)];
				if (currentChannel.isAvailable()) {
					if (null != deadChannels[arrayIndex]) {
						deadChannels[arrayIndex] = null;
					}
					return currentChannel;
				}
				deadChannels[arrayIndex] = currentChannel;
			}
			throw new SailfishException(ExceptionCode.EXCHANGER_NOT_AVAILABLE, "exchanger is not available!");
		}
	}

	private static final class GenericExchangeChannelChooser implements ExchangeChannelChooser {
		private final AtomicInteger idx = new AtomicInteger();
		private final ExchangeChannel[] channels;
		private final ExchangeChannel[] deadChannels;

		GenericExchangeChannelChooser(ExchangeChannel[] channels, ExchangeChannel[] deadChannels) {
			this.channels = channels;
			this.deadChannels = deadChannels;
		}

		@Override
		public ExchangeChannel next() throws SailfishException {
			int arrayIndex = 0;
			int currentIndex = idx.getAndIncrement();
			for (int i = 0; i < channels.length; i++) {
				ExchangeChannel currentChannel = channels[arrayIndex = Math.abs((currentIndex++) % channels.length)];
				if (currentChannel.isAvailable()) {
					if (null != deadChannels[arrayIndex]) {
						deadChannels[arrayIndex] = null;
					}
					return currentChannel;
				}
				deadChannels[arrayIndex] = currentChannel;
			}
			throw new SailfishException(ExceptionCode.EXCHANGER_NOT_AVAILABLE, "exchanger is not available!");
		}
	}
}
