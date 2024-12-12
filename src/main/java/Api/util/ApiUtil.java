package Api.util;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class ApiUtil {

    @Value("#{'${alist.dirs}'.split(',')}")
    private List<String> alistDirs = Collections.emptyList();

    @Value("${alist.targetUrl}")
    private String targetUrl;

    @Value("${alist.redirectUrl}")
    private String redirectUrl;

    @Value("${alist.getTokenApi}")
    private String getTokenApi;

    @Value("${alist.getDirDocument}")
    private String getDirDocument;

    @Value("${alist.username}")
    private String username;

    @Value("${alist.password}")
    private String password;

    @Value("${alist.Host}")
    private String Host;

    public String login() {
        // 构建完整的登录 URL
        String loginUrl = targetUrl + getTokenApi;

        // 发送 POST 请求
        HttpResponse<String> response = Unirest.post(loginUrl)
                .header("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                .header("Accept", "*/*")
                .header("Host", Host)
                .header("Connection", "keep-alive")
                .multiPartContent()
                .field("Username", username) // 从配置文件加载用户名
                .field("Password", password) // 从配置文件加载密码
                .asString();

        // 检查响应状态
        if (response.getStatus() == 200) {
            // 解析 JSON 响应提取 token
            JSONObject responseBody = new JSONObject(response.getBody());
            if (responseBody.getInt("code") == 200) {
                JSONObject data = responseBody.getJSONObject("data");
                String token = data.getString("token");
                return token; // 返回 token
            } else {
                throw new RuntimeException("Login failed with message: " + responseBody.getString("message"));
            }
        } else {
            throw new RuntimeException("Login failed: " + response.getStatus());
        }
    }

    /**
     * 获取文件列表
     *
     * @param path 文件路径
     * @return 文件列表
     */
    public List<String> fetchFileList(String path, String token) {
        // 构建完整的文件列表 API URL
        String listUrl = targetUrl + getDirDocument;

        // 发送 POST 请求获取文件列表
        HttpResponse<String> response = Unirest.post(listUrl)
                .header("Authorization", token)
                .header("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                .header("Accept", "*/*")
                .header("Host", Host)
                .header("Connection", "keep-alive")
                .multiPartContent() // 自动处理 multipart 格式
                .field("path", path) // 动态设置路径
                .asString();
        // 检查响应状态
        if (response.getStatus() == 200) {
            // 解析响应 JSON 数据
            JSONObject responseBody = new JSONObject(response.getBody());

            // 检查业务状态码
            if (responseBody.getInt("code") == 200) {
                // 提取文件名列表
                JSONObject data = responseBody.getJSONObject("data");
                JSONArray contentArray = data.getJSONArray("content");
                List<String> fileNames = new ArrayList<>();
                String fileName;
                for (int i = 0; i < contentArray.length(); i++) {
                    JSONObject file = contentArray.getJSONObject(i);
                    fileName = redirectUrl+path+"/"+file.getString("name");
                    fileNames.add(fileName);
                }
                return fileNames; // 返回文件名列表
            } else {
                throw new RuntimeException("Failed to fetch file list with message: " + responseBody.getString("message"));
            }
        } else {
            throw new RuntimeException("Failed to fetch file list: HTTP " + response.getStatus());
        }
    }

    public Boolean isInAlist(String path) {
        return alistDirs.contains(path);
    }

}
