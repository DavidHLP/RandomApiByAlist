package Api.controller;

import Api.service.ApiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RedirectApiController {

    private final ApiService apiService;

    @GetMapping("/get")
    public void getRedirectUrl(
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "rand" , required = false) String rand,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        try {
            String url = apiService.getUrlByDirNameRandom(address, getClientIp(request));
            // 设置响应头，防止缓存
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            response.setHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:133.0) Gecko/20100101 Firefox/133.0, pan.baidu.com");
            response.sendRedirect(url);
        } catch (IOException e) {
            throw new RuntimeException("Failed to redirect to the URL", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
