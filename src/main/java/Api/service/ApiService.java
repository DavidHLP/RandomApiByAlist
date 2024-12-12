package Api.service;

import Api.util.ApiQueue;
import Api.util.ApiUtil;
import Api.util.RedisCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {

    @Value("${alist.queue.size}")
    private final int sizeOfQueue = 10;
    @Value("#{'${alist.dirs}'.split(',')}")
    private List<String> alistDirs = Collections.emptyList();
    private final RedisCache redisCache;
    private final ApiUtil apiUtil;
    private final ApiQueue<String> accessQueue;

    public String getUrlByDirNameRandom(String name , String ip) {

        name = getRandomDirName(name);

        log.info("getUrlByDirNameRandom, name:{}, ip:{}", name, ip);

        // 校验目录是否存在
        if (!apiUtil.isInAlist(name)) {
            throw new RuntimeException("No Dir found for the given name: " + name);
        }

        if (!redisCache.hasKey(name)) {
            redisCache.setCacheObject(name, getUrlListByDirName(name), 24, TimeUnit.HOURS);
        }

        // 检查队列是否为空
        if (accessQueue.isEmpty()) {
            // 从 Redis 缓存中获取 URL 列表
            List<String> urlList = redisCache.getCacheObject(name);

            if (urlList == null || urlList.isEmpty()) {
                throw new RuntimeException("No URLs found for the given name: " + name);
            }

            // 随机打乱 URL 列表
            Collections.shuffle(urlList, new Random(System.nanoTime()));

            // 重新填充队列，确保不会超过队列容量
            for (String url : urlList) {
                if (accessQueue.size() >= sizeOfQueue) {
                    break; // 队列已满，停止填充
                }
                try {
                    accessQueue.put(url); // 阻塞直到有空间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Failed to refill URL queue", e);
                }
            }
        }

        // 从队列中取出 URL
        try {
            return accessQueue.take(); // 阻塞直到有可用资源
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to retrieve URL from the queue", e);
        }
    }

    private String GetToken() {
        String token;
        if (!redisCache.hasKey("RandomApiByAlist")) {
            token = apiUtil.login();
            redisCache.setCacheObject("RandomApiByAlist", token, 24, TimeUnit.HOURS);
            return token;
        }
        return redisCache.getCacheObject("RandomApiByAlist");
    }


    @Async
    @Scheduled(fixedRate = 600000) // 每10分钟执行一次
    protected void cacheUrlByDirName() {
        for (String dirName : alistDirs) {
            redisCache.setCacheObject(dirName, getUrlListByDirName(dirName), 1, TimeUnit.HOURS);
        }
    }

    private List<String> getUrlListByDirName(String name) {
        return apiUtil.fetchFileList(name,GetToken());
    }

    private String getRandomDirName(String dirName) {
        assert alistDirs != null;
        Collections.shuffle(alistDirs, new Random(System.nanoTime()));
        return dirName == null || dirName.isEmpty() ? alistDirs.get(0) : dirName;
    }

}