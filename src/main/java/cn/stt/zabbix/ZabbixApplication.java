package cn.stt.zabbix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @ClassName ZabbixApplication
 * @Description TODO
 * @Author shitt7
 * @Date 2019/11/14 16:55
 * @Version 1.0
 */
@SpringBootApplication
public class ZabbixApplication implements WebMvcConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZabbixApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ZabbixApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
        LOGGER.info("容器启动成功!");
    }

    /**
     * 解决跨域问题
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //设置允许跨域的路径
        registry.addMapping("/**")
                //设置允许跨域请求的域名
                .allowedOrigins("*")
                //是否允许证书 不再默认开启
                .allowCredentials(true)
                //设置允许的方法
                .allowedMethods("*")
                //跨域允许时间
                .maxAge(3600);
    }
}
