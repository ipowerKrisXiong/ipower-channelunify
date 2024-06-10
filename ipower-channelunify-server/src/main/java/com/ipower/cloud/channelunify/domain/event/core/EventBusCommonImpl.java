//package com.ipower.cloud.channelunify.domain.event.core;
//
//import com.alibaba.ttl.threadpool.TtlExecutors;
//import com.mars.ddd.core.event.Event;
//import com.mars.ddd.core.event.EventBus;
//import com.mars.ddd.core.event.EventReceiver;
//import com.mars.ddd.core.event.NamedThreadFactory;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//import java.util.concurrent.*;
//
//
///**
// * 事件总线接口的基础实现类
// * 可以用其它框架实现更高级的事件总线实现，比如com.lmax.disruptor
// */
//@Component("eventBus")
//@Slf4j
//public class EventBusCommonImpl implements EventBus {
//
//	public static EventBusCommonImpl INSTANCE;
//
//	/** 注册的事件接收者 */
//	private ConcurrentHashMap<String, CopyOnWriteArraySet<EventReceiver<?>>> receivers = new ConcurrentHashMap<String, CopyOnWriteArraySet<EventReceiver<?>>>();
//
//	@Value("${mars.eventbus.event_queue_size:32768}")
//	private Integer queueSize = 32768;
//
//	@Value("${mars.eventbus.event_pool_size:5}")
//	private Integer poolSize = 5;
//
//	@Value("${mars.eventbus.event_pool_max_size:10}")
//	private Integer poolMaxSize = 10;
//
//	@Value("${mars.eventbus.event_pool_alive_time:60}")
//	private Integer poolKeepAlive = 60;
//
//	@Qualifier("${mars.eventbus.event_pool_await_time:60}")
//	private Integer poolAwaitTime = 60;
//
//	private ExecutorService pool;
//
//	/**
//	 * 根据配置初始化
//	 */
//	@PostConstruct
//	protected void initialize() {
//		ThreadGroup threadGroup = new ThreadGroup("event事件模块");
//		NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "event事件处理");
//		pool = new ThreadPoolExecutor(poolSize, poolMaxSize, poolKeepAlive, TimeUnit.SECONDS,
//				new LinkedBlockingQueue<Runnable>(queueSize), threadFactory);
//
//		// TtlExecutors修饰executorService,保证threadLocal得异步传递
//		// 使用参考https://github.com/alibaba/transmittable-thread-local#2-%E4%BF%9D%E8%AF%81%E7%BA%BF%E7%A8%8B%E6%B1%A0%E4%B8%AD%E4%BC%A0%E9%80%92%E5%80%BC
//		//必须要做这个包装，要不就要在submit task的时候做包装，否则只是用TransmittableThreadLocal还是无法做到正确传递，只能第一次传递成功，后面就无法传递成功
//		pool = TtlExecutors.getTtlExecutorService(pool);
//
//		//单例使用
//		INSTANCE = this;
//
//		log.info("初始化eventBus线程池成功！");
//
//	}
//
//	/** 销毁方法 */
//	@PreDestroy
//	public void destory() {
//		shutdown();
//	}
//
//	/** 停止状态 */
//	private volatile boolean stop;
//
//	/**
//	 * 关闭事件总线，阻塞方法会等待总线中的全部事件都发送完后再返回
//	 */
//	public void shutdown() {
//		if (isStop()) {
//			return;
//		}
//		stop = true;
//		ThreadPoolExecutor executor = ((ThreadPoolExecutor) pool);
//		for (;;) {
//			if (executor.getQueue().isEmpty()) {
//				break;
//			}
//			Thread.yield();
//		}
//		// 等待线程池关闭
//		pool.shutdown();
//		log.warn("开始关闭事件总线线程池");
//		try {
//			if (!pool.awaitTermination(poolAwaitTime, TimeUnit.SECONDS)) {
//				log.error("无法在预计时间内完成事件总线线程池关闭,尝试强行关闭");
//				pool.shutdownNow();
//				if (!pool.awaitTermination(poolAwaitTime, TimeUnit.SECONDS)) {
//					log.error("事件总线线程池无法完成关闭");
//				}
//			}
//		} catch (InterruptedException e) {
//			log.error("事件总线线程池关闭时线程被打断,强制关闭事件总线线程池");
//			pool.shutdownNow();
//		}
//	}
//
//	/**
//	 * 检查该事件总线是否已经停止服务
//	 * @return
//	 */
//	public boolean isStop() {
//		return stop;
//	}
//
//	@Override
//	public void dispatchAsync(Event<?> event) {
//		if (event == null) {
//			throw new IllegalArgumentException("事件对象不能为空");
//		}
//		if (stop) {
//			throw new IllegalStateException("事件总线已经停止，不能再接收事件");
//		}
//		String name = event.getName();
//		if (!receivers.containsKey(name)) {
//			log.warn("事件[{}]没有对应的接收器", name);
//			return;
//		}
//		for (EventReceiver<?> receiver : receivers.get(name)) {
//			Runnable runner = createRunner(receiver, event);
//			try {
//				pool.submit(runner);
//			} catch (RejectedExecutionException e) {
//				log.error("事件线程池已满，请尽快调整配置参数");
//				onRejected(receiver, event);
//			}
//		}
//	}
//
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	@Override
//	public void dispatchSync(Event<?> event) {
//		String name = event.getName();
//		if (!receivers.containsKey(name)) {
//			log.warn("事件'{}'没有对应的接收器", name);
//			return;
//		}
//		for (EventReceiver receiver : receivers.get(name)) {
//			try {
//				receiver.onEvent(event);
//			} catch (Exception e) {
//				log.error("事件[" + event.getName() + "]处理时发生异常", e);
////				这里因为是当前线程处理，这里必须把异常同时往外抛出，不然可能影响事务
//				throw e;
//			}
//		}
//	}
//
//	@Override
//	public void register(String eventName, EventReceiver<?> receiver) {
//		if (eventName == null || receiver == null) {
//			throw new IllegalArgumentException("事件名和接收者均不能为空");
//		}
//
//		CopyOnWriteArraySet<EventReceiver<?>> set = receivers.get(eventName);
//		if (set == null) {
//			set = new CopyOnWriteArraySet<EventReceiver<?>>();
//			CopyOnWriteArraySet<EventReceiver<?>> prev = receivers.putIfAbsent(eventName, set);
//			set = prev != null ? prev : set;
//		}
//
//		set.add(receiver);
//	}
//
//	@Override
//	public void unregister(String eventName, EventReceiver<?> receiver) {
//		if (eventName == null || receiver == null) {
//			throw new IllegalArgumentException("事件名和接收者均不能为空");
//		}
//
//		CopyOnWriteArraySet<EventReceiver<?>> set = receivers.get(eventName);
//		if (set != null) {
//			set.remove(receiver);
//		}
//	}
//
//	/**拒绝后退化到同步线程处理*/
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	private void onRejected(EventReceiver receiver, Event event) {
//		try {
//			receiver.onEvent(event);
//		} catch (ClassCastException e) {
//			log.error("事件[" + event.getName() + "]对象类型不符合接收器声明", e);
//		} catch (Throwable t) {
//			log.error("事件[" + event.getName() + "]处理时发生异常", t);
//		}
//	}
//
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	private Runnable createRunner(final EventReceiver receiver, final Event event) {
//		return new Runnable() {
//			@Override
//			public void run() {
//				try {
//					receiver.onEvent(event);
//				} catch (ClassCastException e) {
//					log.error("事件[" + event.getName() + "]对象类型不符合接收器[" + receiver.getClass() + "]声明", e);
//				} catch (Throwable t) {
//					log.error("事件[" + event.getName() + "]处理器[" + receiver.getClass() + "]运行时发生异常", t);
//				}
//			}
//		};
//	}
//
//	// JMX管理接口的实现方法
//
//	@Override
//	public int getEventQueueSize() {
//		return ((ThreadPoolExecutor) pool).getQueue().size();
//	}
//
//	@Override
//	public int getPoolActiveCount() {
//		return ((ThreadPoolExecutor) pool).getActiveCount();
//	}
//}