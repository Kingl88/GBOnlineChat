package otherHomeworksJavaPartThree;

import java.util.Arrays;

public class HomeworkSix {

    public static int[] method_one(int[] array) {
        boolean flag = false;
        int position = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 4) {
                position = i + 1;
                flag = true;
            }
        }
        int[] newArray = new int[array.length-position];
        for (int i = position, j = 0; i < array.length; i++, j++) {
            newArray[j] = array[i];
        }
        if (flag) {
            return newArray;
        } else {
            throw new RuntimeException("Массив не содержит цифры 4");
        }
    }

    public static boolean method_two(Integer[] arr){
        if(Arrays.asList(arr).contains(1) && Arrays.asList(arr).contains(4)){
            for (Integer integer : arr) {
                if (integer != 1 && integer != 4) {
                    return false;
                }
            }
            return true;
        } else{
            return false;
        }
    }
}