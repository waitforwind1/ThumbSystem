package com.usst.thumbs.config.cosConfig;

import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tencent.cos")
public class CosClientConfig {

    private String appId;
    private String secretKey;
    private String secretId;
    private Region region;
    private String bucket;
    private String baseUrl;
}
