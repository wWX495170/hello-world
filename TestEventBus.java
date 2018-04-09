/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
/*
 * 测试guava16.0.1和19.0版本EventBus的性能区别，使用了四个线程池来执行任务
 */
public class TestSimpleEventBus {
//  private SimpleEventBus eventBus = new SimpleEventBus();
  private EventBus eventBus = new EventBus();

  private List<Object> events = new ArrayList<>();

  class SubscriberForTest {
    @Subscribe
    @AllowConcurrentEvents
    public void s1(Integer event) {
      events.add(event);
    }

    @Subscribe
    public void s2(String event) {
      events.add(event);
    }
  }

  @Test
  public void unregister() {
    Object obj = new SubscriberForTest();

    // unregister not exist obj, should no problem
    eventBus.unregister(obj);

    eventBus.register(obj);
    eventBus.unregister(obj);

    // unregister again, should no problem
    eventBus.unregister(obj);
  }

  @Test
  public void oneSubscriber() {
    Object obj = new SubscriberForTest();

    eventBus.register(obj);

    eventBus.post(0.1);
    eventBus.post(1);
    eventBus.post("str");
    Assert.assertThat(events, Matchers.contains(1, "str"));

    eventBus.unregister(obj);

    events.clear();
    eventBus.post(0.1);
    eventBus.post(1);
    eventBus.post("str");
    Assert.assertThat(events, Matchers.empty());
  }

  @Test
  public void twoSubscriber() throws InterruptedException {
    CountDownLatch waiter = new CountDownLatch(4);
    ExecutorService executorService = Executors.newFixedThreadPool(4);

    Object obj1 = new SubscriberForTest();
    Object obj2 = new SubscriberForTest();
    eventBus.register(obj1);
    eventBus.register(obj2);

    long time1 = System.nanoTime();
    for (int n = 0; n < 4; n++) {
      executorService.execute(() -> {
        for (int i = 0; i < 100000; i++) {
          eventBus.post(1);
          eventBus.post("str");
        }
        waiter.countDown();
      });
    }
    waiter.await();
    long time2 = System.nanoTime();
    long times = time2 - time1;
    System.out.println("The interval is: " + times + " ns");
    executorService.shutdown();

//    Object obj1 = new SubscriberForTest();
//    Object obj2 = new SubscriberForTest();
//    eventBus.register(obj1);
//    eventBus.register(obj2);
//    eventBus.post(0.1);
//    eventBus.post(1);
//    eventBus.post("str");
//    Assert.assertThat(events, Matchers.contains(1, 1, "str", "str"));
//
//    events.clear();
//    eventBus.unregister(obj1);
//    eventBus.post(0.1);
//    eventBus.post(1);
//    eventBus.post("str");
//    Assert.assertThat(events, Matchers.contains(1, "str"));


////  SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
////  Date date1 = new Date();
////  System.out.println("start time: " + date.format(date1));
//    
////    Date date2 = new Date();
////    System.out.println("end time: " + date.format(date2));

  }
}
