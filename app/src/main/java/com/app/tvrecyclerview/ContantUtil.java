package com.app.tvrecyclerview;

import java.util.Random;

public class ContantUtil {
    private static int[] mColorIds = {R.color.amber, R.color.brown, R.color.cyan,
            R.color.deepPurple, R.color.green, R.color.lightBlue, R.color.lightGreen,
            R.color.lime, R.color.orange, R.color.pink, R.color.cyan, R.color.deepPurple};

    public static String[] TEST_DATAS = {"A", "B", "C", "D", "E", "F", "G",
    "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S"};

    public static int getRandColor() {
        Random random = new Random();
        int pos = random.nextInt(mColorIds.length);
        return mColorIds[pos];
    }

}
