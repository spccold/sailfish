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

import sailfish.remoting.exceptions.SailfishException;

/**
 * Factory that creates new {@link ExchangeChannelChooser}s.
 * 
 * @author spccold
 * @version $Id: ExchangeChannelChooserFactory.java, v 0.1 2016年11月22日 下午4:36:58 spccold Exp $
 */
public interface ExchangeChannelChooserFactory {
	/**
     * Returns a new {@link ExchangeChannelChooser}.
     */
	ExchangeChannelChooser newChooser(ExchangeChannel[] channels, ExchangeChannel[] deadChannels);

    /**
     * Chooses the next {@link ExchangeChannel} to use.
     */
    interface ExchangeChannelChooser {

        /**
         * Returns the new {@link ExchangeChannel} to use.
         */
        ExchangeChannel next() throws SailfishException;
    }
}
