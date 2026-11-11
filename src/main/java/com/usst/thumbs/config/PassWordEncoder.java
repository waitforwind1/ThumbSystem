package com.usst.thumbs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PassWordEncoder {

    /**
     * 自定义加密算法配置
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(
                /**
                 * 随机盐长度 每个密码加的盐随机
                 * 输出的哈希长度
                 * 通道长度
                 * 占用内存大小
                 * 迭代次数 越大越慢 攻击成本越高
                 */
                16,
                32,
                1,
                19456,
                2
        );
    }
}
