package com.awe.foundation.module.storage.provider.s3;

import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.common.util.JsonUtils;
import com.awe.foundation.module.storage.domain.dto.S3CredentialDTO;
import com.awe.foundation.module.storage.domain.entity.StorageProviderConfig;
import com.awe.foundation.module.storage.service.StorageCredentialCipher;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * S3 兼容协议与访问地址测试
 */
class S3StorageProviderTest {

    private final S3StorageProvider provider = new S3StorageProvider();
    private final StorageProviderConfig config = new StorageProviderConfig();
    private final AtomicReference<String> requestMethod = new AtomicReference<>();
    private final AtomicReference<String> requestPath = new AtomicReference<>();
    private final AtomicReference<String> requestAuthorization = new AtomicReference<>();
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private HttpServer storageServer;

    /**
     * 启动本地 S3 协议端点
     *
     * @throws Exception 服务启动失败
     */
    @BeforeEach
    void prepareStorageServer() throws Exception {
        storageServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        storageServer.createContext("/", exchange -> {
            requestMethod.set(exchange.getRequestMethod());
            requestPath.set(exchange.getRequestURI().getPath());
            requestAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        storageServer.start();
        StorageCredentialCipher cipher = new StorageCredentialCipher("test-storage-key");
        ReflectionTestUtils.setField(provider, "credentialCipher", cipher);
        config.setEndpoint("http://127.0.0.1:" + storageServer.getAddress().getPort());
        config.setServiceName("test-bucket");
        config.setRegion("us-east-1");
        config.setCredentialConfig(cipher.encrypt(JsonUtils.toJsonString(new S3CredentialDTO("test-access-key", "test-secret-key"))));
        config.setPrivateBucket(true);

    }

    /**
     * 关闭本地存储端点
     */
    @AfterEach
    void stopStorageServer() {
        storageServer.stop(0);

    }

    /**
     * 验证上传内容、路径寻址和签名认证
     *
     * @throws Exception 上传失败
     */
    @Test
    void uploadUsesBucketPathAndSignedCredentials() throws Exception {
        provider.upload(config, new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)), "/folder/hello.txt");
        assertEquals("PUT", requestMethod.get());
        assertEquals("/test-bucket/folder/hello.txt", requestPath.get());
        assertEquals("hello", requestBody.get());
        assertTrue(requestAuthorization.get().contains("Credential=test-access-key/"));
        assertFalse(requestAuthorization.get().contains("test-secret-key"));

    }

    /**
     * 验证删除与空间权限测试使用正确的 S3 请求
     */
    @Test
    void deleteAndConnectionTestUseS3Requests() {
        provider.delete(config, "/folder/hello.txt");
        assertEquals("DELETE", requestMethod.get());
        assertEquals("/test-bucket/folder/hello.txt", requestPath.get());
        provider.test(config);
        assertEquals("HEAD", requestMethod.get());
        assertTrue(requestPath.get().startsWith("/test-bucket"));

    }

    /**
     * 验证私有链接签名及有效期
     */
    @Test
    void privateUrlHasExpiringSignature() {
        String accessUrl = provider.accessUrl(config, "/folder/hello.txt");
        assertTrue(accessUrl.startsWith(config.getEndpoint() + "/test-bucket/folder/hello.txt?"));
        assertTrue(accessUrl.contains("X-Amz-Expires=900"));
        assertTrue(accessUrl.contains("X-Amz-Signature="));
        assertFalse(accessUrl.contains("test-secret-key"));

    }

    /**
     * 验证公开域名拼接与特殊字符编码
     */
    @Test
    void publicUrlEncodesObjectPath() {
        config.setPrivateBucket(false);
        config.setAccessDomain("https://cdn.example.com/");
        assertEquals("https://cdn.example.com/folder/hello%20world%23.txt", provider.accessUrl(config, "/folder/hello world#.txt"));

    }

    /**
     * 验证服务端拒绝请求时返回业务异常
     */
    @Test
    void rejectedConnectionRaisesBusinessError() {
        storageServer.removeContext("/");
        storageServer.createContext("/", exchange -> {
            exchange.sendResponseHeaders(403, -1);
            exchange.close();
        });
        assertThrows(BusinessException.class, () -> provider.test(config));

    }

}
