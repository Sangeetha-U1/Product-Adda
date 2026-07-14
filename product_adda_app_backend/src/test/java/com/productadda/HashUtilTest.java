package com.productadda;

import com.productadda.util.HashUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HashUtilTest {

    @Autowired
    private HashUtil hashUtil;

    @Test
    void printMyHash() {
        String result = hashUtil.hashSha256("123456");
        System.out.println("\n=====================================");
        System.out.println("YOUR HMAC-SHA256 HASH IS: " + result);
        System.out.println("=====================================\n");
    }
}