package com.app.tvrecyclerview;

import java.util.Random;

class ContantUtil {
    private static int[] mColorIds = {R.color.amber, R.color.brown, R.color.cyan,
            R.color.deepPurple, R.color.green, R.color.lightBlue, R.color.lightGreen,
            R.color.lime, R.color.orange, R.color.pink, R.color.cyan, R.color.deepPurple};

    private static int[] mImgIds= {R.drawable.pic0, R.drawable.pic1, R.drawable.pic2,
            R.drawable.pic3, R.drawable.pic4, R.drawable.pic5, R.drawable.pic6, R.drawable.pic7,
            R.drawable.pic8};

    static String[] TEST_DATAS = {"A", "B", "C", "D", "E", "F", "G",
    "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R"};

    static int getRandColor() {
        Random random = new Random();
        int pos = random.nextInt(mColorIds.length);
        return mColorIds[pos];
    }

    static int getImgResourceId(int position) {
        int pos;
        if (position < mImgIds.length) {
            pos = position;
        } else {
            pos = position - mImgIds.length;
        }
        return mImgIds[pos];
    }
}
