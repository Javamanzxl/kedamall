package com.zxl.gulimall.thirdParty;

import com.zxl.gulimall.thirdParty.component.SmsComponent;
import com.zxl.common.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class GulimallThirdPartyApplicationTests {
    @Resource
    private SmsComponent smsComponent;

    @Test
    void contextLoads() {
    }
    @Test
    public void sendCode(){
        smsComponent.sendSms("13864339756","abcde");
    }

}
