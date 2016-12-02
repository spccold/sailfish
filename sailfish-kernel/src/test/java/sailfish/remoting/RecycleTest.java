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

import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;

import io.netty.util.Recycler;
import io.netty.util.internal.SystemPropertyUtil;

/**
 * @author spccold
 * @version $Id: RecycleCheckTest.java, v 0.1 2016年12月1日 下午11:55:23 spccold Exp $
 */
public class RecycleTest {
	
	@Test
	public void testRecycleInSyncThread() throws Exception{
		//recycle
		Resource2 resource1 = Resource2.newInstance();
		resource1.recycle();
		
		//don't recycle
		resource1 = Resource2.newInstance();
		
		Resource2 temp =null;
		// By default we allow one push to a Recycler for each 8th try on handles that were never recycled before.
        // This should help to slowly increase the capacity of the recycler while not be too sensitive to allocation
        // bursts.
		int stackDefaultRatioMask = SystemPropertyUtil.getInt("io.netty.recycler.ratio", 8) - 1;
		for(int i =0; i< stackDefaultRatioMask; i++){
			temp = Resource2.newInstance();
			temp.recycle();
			Assert.assertTrue(temp != Resource2.newInstance());
		}
		
		temp = Resource2.newInstance();
		temp.recycle();
		Assert.assertTrue(temp == Resource2.newInstance());
	}
	
	/**
	 * 异步线程recycle测试
	 * <pre>
	 * 	每个异步recycle的线程都会产生一个WeakOrderQueue, 多个线程产的的WeakOrderQueue会形成链, 如下所示
	 * 		Recycler.Stack.head -> WeakOrderQueue@2(thread2产生) -> WeakOrderQueue@1(thread1产生) -> null
	 * 		Recycler.Stack.cursor最初指向Recycler.Stack.head(例如最初指向WeakOrderQueue@1), 但是当产生新的WeakOrderQueue时
	 * 		(例如WeakOrderQueue@2)时，Recycler.Stack.cursor指针并不会及时调整, 知道此次Recycler.Stack.pop结束，Recycler.Stack.cursor
	 * 		才会重新指向Recycler.Stack.head, 即指向WeakOrderQueue链的头
	 * </pre>
	 * @throws Exception
	 */
	@Test
	public void testRecycleInAsyncThread() throws Exception{
		final CountDownLatch latch = new CountDownLatch(1);
		final Resource resource1 = Resource.newInstance();
		//recycle in thread1
		new Thread(){
			@Override
			public void run() {
				resource1.recycle();
				latch.countDown();
			}
		}.start();
		
		latch.await();
		Resource resource2 = Resource.newInstance();
		Assert.assertTrue(resource1 == resource2);
		resource2.recycle();
		
		final CountDownLatch latch2 = new CountDownLatch(1);
		final Resource resource3 = Resource.newInstance();
		//recycle in thread2
		new Thread(){
			@Override
			public void run() {
				resource3.recycle();
				latch2.countDown();
			}
		}.start();
		
		latch2.await();
		Resource resource4 = Resource.newInstance();
		//thread2中recycle时并没有调整Recycler.Stack.cursor, 此时Recycler.Stack.cursor还指向thread1产生的WeakOrderQueue1
		//此时Recycler.Stack.pop只能从WeakOrderQueue1开始遍历，但resource3 recycle在thread2产生的WeakOrderQueue2，所有pop为null，会创建新的对象
		Assert.assertTrue(resource4 != resource3);
		//上次Resource.newInstance()已经让Recycler.Stack.cursor重新指向WeakOrderQueue的头，所以这次可以获取到thread2中recycle的resource3
		Assert.assertTrue(Resource.newInstance() == resource3);
	}
	
	static final class Resource{

		private static final Recycler<Resource> RECYCLER = new Recycler<RecycleTest.Resource>(){
			@Override
			protected Resource newObject(Recycler.Handle<Resource> handle) {
				return new Resource(handle);
			}
		};
		
		public static Resource newInstance(){
			return RECYCLER.get();
		}
		
		private final Recycler.Handle<Resource> handle;
		private Resource(Recycler.Handle<Resource> handle) {
			this.handle = handle;
		}

		public void recycle() {
			handle.recycle(this);
		}
	}
	
	static final class Resource2{

		private static final Recycler<Resource2> RECYCLER = new Recycler<RecycleTest.Resource2>(){
			@Override
			protected Resource2 newObject(Recycler.Handle<Resource2> handle) {
				return new Resource2(handle);
			}
		};
		
		public static Resource2 newInstance(){
			return RECYCLER.get();
		}
		
		private final Recycler.Handle<Resource2> handle;
		private Resource2(Recycler.Handle<Resource2> handle) {
			this.handle = handle;
		}

		public void recycle() {
			handle.recycle(this);
		}
	}
}
