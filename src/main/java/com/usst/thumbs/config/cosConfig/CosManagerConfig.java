package com.usst.thumbs.config.cosConfig;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosManagerConfig {

    @Resource
    private CosClientConfig cosClientConfig;

    @Bean
    public COSClient cosClient(){
        COSCredentials credentials = new BasicCOSCredentials(cosClientConfig.getSecretId(), cosClientConfig.getSecretKey());
        ClientConfig config = new ClientConfig(cosClientConfig.getRegion());
        return new COSClient(credentials,config);
    }

}
