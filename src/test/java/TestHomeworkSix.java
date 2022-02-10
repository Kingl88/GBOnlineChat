import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import otherHomeworksJavaPartThree.HomeworkSix;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TestHomeworkSix {
    @ParameterizedTest
    @MethodSource("dataForTestMethodOne")
    public void methodOneTest(int[] inData, int[] result) {
        Assertions.assertArrayEquals(result, HomeworkSix.method_one(inData));
    }

    public static Stream<Arguments> dataForTestMethodOne() {
        List<Arguments> list = new ArrayList<>();
        list.add(Arguments.arguments(new int[]{1, 2, 4, 4, 2, 3, 4, 1, 7}, new int[]{1, 7}));
        list.add(Arguments.arguments(new int[]{1, 2, 4, 4, 2, 3, 1, 7}, new int[]{2, 3, 1, 7}));
        list.add(Arguments.arguments(new int[]{4}, new int[]{}));
        list.add(Arguments.arguments(new int[]{4, 4, 4}, new int[]{}));
        return list.stream();
    }

    @Test
    public void methodOneTestException() {
        try {
            HomeworkSix.method_one(new int[]{1, 2, 2, 3, 1, 7});
            Assertions.fail();
        } catch (Exception ignored) {

        }
    }

    @ParameterizedTest
    @MethodSource("dataForTestMethodTwo")
    public void methodTwoTest(Integer[] inData, boolean result) {
        Assertions.assertEquals(result, HomeworkSix.method_two(inData));
    }

    public static Stream<Arguments> dataForTestMethodTwo() {
        List<Arguments> list = new ArrayList<>();
        list.add(Arguments.arguments(new Integer[]{1, 2, 4, 4, 2, 3, 4, 1, 7}, false));
        list.add(Arguments.arguments(new Integer[]{},false));
        list.add(Arguments.arguments(new Integer[]{4}, false));
        list.add(Arguments.arguments(new Integer[]{4, 4, 1}, true));
        list.add(Arguments.arguments(new Integer[]{1, 1, 1}, false));
        return list.stream();
    }
}
