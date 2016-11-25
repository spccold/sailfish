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
package sailfish.remoting.processors;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author spccold
 * @version $Id: Processors.java, v 0.1 2016年11月25日 上午11:54:17 spccold Exp $
 */
public class Processors {
	
	private static final Logger logger = LoggerFactory.getLogger(Processors.class);
	
	private final ConcurrentMap<Integer, RequestProcessor> processors = new ConcurrentHashMap<>();
	
	public void registerProcessor(int opcode, RequestProcessor processor){
		if(null != processors.putIfAbsent(opcode, processor)){
			logger.warn("repeat register request processor, opcode[{}], processor[{}]", opcode, processor);
		}
	}
	
	public RequestProcessor findProcessor(int opcode){
		return processors.get(opcode);
	}
}
