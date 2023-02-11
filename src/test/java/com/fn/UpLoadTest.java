package com.fn;

import org.junit.jupiter.api.Test;

public class UpLoadTest {
    @Test
    public void loadTest() {
        String fileName = "dfcxqf.jpg";
        String[] upLoadFileName = fileName.split("\\.");
        String s = upLoadFileName[1];
        System.out.println(s);
    }

    @Test
    public void load2Test() {
        String fileName = "dfcxqf.png";
        String s = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(s);
    }
}
