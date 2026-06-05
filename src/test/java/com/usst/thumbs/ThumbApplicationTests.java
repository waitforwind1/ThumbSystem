package com.usst.thumbs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.task.scheduling.enabled=false",
        "thumbs.jobs.interaction-event-publish.enabled=false"
})
class ThumbApplicationTests {

    @Test
    void contextLoads() {
    }



}
