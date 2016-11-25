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
package sailfish.remoting;

import org.junit.Assert;
import org.junit.Test;

import sailfish.remoting.channel.DefaultExchangeChannelChooserFactory;
import sailfish.remoting.channel.EmptyExchangeChannel;
import sailfish.remoting.channel.ExchangeChannelChooserFactory;
import sailfish.remoting.exceptions.SailfishException;

/**
 * @author spccold
 * @version $Id: ExchangeChannelChooserTest.java, v 0.1 2016年11月25日 下午8:35:52 spccold Exp $
 */
public class ExchangeChannelChooserTest {

	@Test
	public void testPowerOfTwo() throws Exception{
		int connections = 1;
		
		//test one connection
		MockExchangeChannel[] channels = new MockExchangeChannel[connections];
		initMockExchangeChannelArray(channels);
		MockExchangeChannel[] deadChannels = new MockExchangeChannel[connections];
		ExchangeChannelChooserFactory.ExchangeChannelChooser chooser = DefaultExchangeChannelChooserFactory.INSTANCE.newChooser(channels, deadChannels);
		Assert.assertNotNull(chooser.next());
		
		channels[0].setAvailable(false);
		try{
			chooser.next();
			Assert.assertFalse(true);
		}catch(SailfishException cause){
			Assert.assertTrue(true);
		}
		
		//test two connections
		connections = 2;
		channels = new MockExchangeChannel[connections];
		initMockExchangeChannelArray(channels);
		deadChannels = new MockExchangeChannel[connections];
		chooser = DefaultExchangeChannelChooserFactory.INSTANCE.newChooser(channels, deadChannels);
		
		//try three times(greater than connections)
		MockExchangeChannel mock = (MockExchangeChannel)chooser.next();
		Assert.assertNotNull(mock);
		Assert.assertTrue(mock.index() == 0);
		
		mock = (MockExchangeChannel)chooser.next();
		Assert.assertNotNull(mock);
		Assert.assertTrue(mock.index() == 1);
		
	    mock = (MockExchangeChannel)chooser.next();
		Assert.assertNotNull(mock);
		Assert.assertTrue(mock.index() == 0);

		mock = (MockExchangeChannel)chooser.next();
		Assert.assertNotNull(mock);
		Assert.assertTrue(mock.index() == 1);
		
		//try to let MockExchangeChannel(index:0) unavailable
		channels[0].setAvailable(false);
		
		mock = (MockExchangeChannel)chooser.next();
		Assert.assertNotNull(mock);
		Assert.assertTrue(mock.index() == 1);
		
		//try to let MockExchangeChannel(index:0) available, MockExchangeChannel(index:1) unavailable
		channels[0].setAvailable(true);
		channels[1].setAvailable(false);
		
		mock = (MockExchangeChannel)chooser.next();
		Assert.assertNotNull(mock);
		Assert.assertTrue(mock.index() == 0);
		
		//try to let all channels unavailable
		channels[0].setAvailable(false);
		try{
			chooser.next();
			Assert.assertFalse(true);
		}catch(SailfishException cause){
			Assert.assertTrue(true);
		}

		try{
			chooser.next();
			Assert.assertFalse(true);
		}catch(SailfishException cause){
			Assert.assertTrue(true);
		}
	}
	
	@Test
	public void testGeneric() throws Exception{
		int connections =3;
		//test one connection
		MockExchangeChannel[] channels = new MockExchangeChannel[connections];
		initMockExchangeChannelArray(channels);
		MockExchangeChannel[] deadChannels = new MockExchangeChannel[connections];
		ExchangeChannelChooserFactory.ExchangeChannelChooser chooser = DefaultExchangeChannelChooserFactory.INSTANCE.newChooser(channels, deadChannels);
		
		MockExchangeChannel mock = (MockExchangeChannel)chooser.next();
		Assert.assertNotNull(mock);
		Assert.assertTrue(mock.index() == 0);

	    mock = (MockExchangeChannel)chooser.next();
		Assert.assertNotNull(mock);
		Assert.assertTrue(mock.index() == 1);

		mock = (MockExchangeChannel)chooser.next();
		Assert.assertNotNull(mock);
		Assert.assertTrue(mock.index() == 2);

		mock = (MockExchangeChannel)chooser.next();
		Assert.assertNotNull(mock);
		Assert.assertTrue(mock.index() == 0);
		
		// let index:1, index:2 unavailable
		channels[1].setAvailable(false);
		channels[2].setAvailable(false);
		
		mock = (MockExchangeChannel)chooser.next();
		Assert.assertNotNull(mock);
		Assert.assertTrue(mock.index() == 0);
		
		//let index:0 unavailable
		channels[0].setAvailable(false);
		try{
			chooser.next();
			Assert.assertFalse(true);
		}catch(SailfishException cause){
			Assert.assertTrue(true);
		}
		
		//let index:2 available
		channels[2].setAvailable(true);
		Assert.assertNotNull(chooser.next());
	}
	
	private void initMockExchangeChannelArray(MockExchangeChannel[] channels){
		if(null == channels){
			throw new NullPointerException("channels");
		}
		for(int i = 0; i< channels.length; i++){
			channels[i] = new MockExchangeChannel(i);
		}
	}
	
	private static class MockExchangeChannel extends EmptyExchangeChannel{
		private int index;
		private boolean isAvailable = true;
		public MockExchangeChannel(int index) {
			this.index = index;
		}
		
		public int index(){
			return index;
		}

		public void setAvailable(boolean isAvailable){
			this.isAvailable = isAvailable;
		}
		
		@Override
		public boolean isAvailable() {
			return this.isAvailable;
		}
	}
}