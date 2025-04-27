package de.restaurant_booking_app.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.config.RequestConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Настройка таймаутов
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000, TimeUnit.MILLISECONDS)
                .setResponseTimeout(15000, TimeUnit.MILLISECONDS)
                .build();

        // Создаем пул соединений
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(20);

        // Создаем HTTP клиент
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .build();

        // Создаем фабрику запросов
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        // Возвращаем настроенный RestTemplate
        return new RestTemplate(requestFactory);
    }
}