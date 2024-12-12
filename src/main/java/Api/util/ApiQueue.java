package Api.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class ApiQueue<T> {

    private final BlockingQueue<T> queue;

    @Value("${alist.queue.delay}")
    private final int blocktime = 10;

    @Value("${alist.queue.size}")
    private final int sizeOfQueue = 10;

    // 构造函数，允许设置队列大小
    public ApiQueue(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    // 默认构造函数
    public ApiQueue() {
        this.queue = new LinkedBlockingQueue<>(sizeOfQueue);
    }

    /**
     * 将元素放入队列中（线程安全）
     * 如果队列已满，将阻塞直到有空间
     *
     * @param item 要放入队列的元素
     * @throws InterruptedException 如果线程在等待时被中断
     */
    public void put(T item) throws InterruptedException {
        queue.put(item);
    }

    /**
     * 尝试将元素放入队列中（线程安全）
     * 如果队列已满，将等待指定时间
     *
     * @param item 要放入队列的元素
     * @param timeout 等待的时间
     * @param unit 时间单位
     * @return 如果成功放入返回true，否则返回false
     * @throws InterruptedException 如果线程在等待时被中断
     */
    public boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException {
        return queue.offer(item, timeout, unit);
    }

    /**
     * 从队列中取出元素（线程安全）
     * 如果队列为空，将阻塞直到有元素可用
     *
     * @return 队列中的元素
     * @throws InterruptedException 如果线程在等待时被中断
     */
    public T take() throws InterruptedException {
        Thread.sleep(blocktime);
        return queue.take();
    }

    /**
     * 尝试从队列中取出元素（线程安全）
     * 如果队列为空，将等待指定时间
     *
     * @param timeout 等待的时间
     * @param unit 时间单位
     * @return 如果成功取出返回元素，否则返回null
     * @throws InterruptedException 如果线程在等待时被中断
     */
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    /**
     * 获取队列当前大小
     *
     * @return 队列中的元素数量
     */
    public int size() {
        return queue.size();
    }

    /**
     * 检查队列是否为空
     *
     * @return 如果队列为空返回true，否则返回false
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}