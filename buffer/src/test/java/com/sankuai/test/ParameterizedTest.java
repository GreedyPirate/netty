package com.sankuai.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * 0、1、1、2、3、5、8、13、21、34
 */
@RunWith(Parameterized.class)
public class ParameterizedTest {

    @Parameterized.Parameters(name = "{index}: fib[{0}]={1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{{0, 0}, {1, 1}, {2, 1}, {3, 2}, {4, 3}, {5, 5}, {6, 8}});
    }

    /**
     * 字段注入，必须是public
     */
//    @Parameterized.Parameter(value = 0)
    public int fInput;

//    @Parameterized.Parameter(value = 1)
    public int fExpected;

    /**
     * 表达式：fib[{0}]={1}，数据：data
     * 注入到构造方法的参数中
     */
    public ParameterizedTest(int input, int expected) {
        fInput = input;
        fExpected = expected;
    }

    @Test
    public void test() {
        assertEquals(fExpected, Fibonacci.compute(fInput));
    }
}

class Fibonacci {
    public static int compute(int n) {
        int result = 0;

        if (n <= 1) {
            result = n;
        } else {
            result = compute(n - 1) + compute(n - 2);
        }

        return result;
    }
}

