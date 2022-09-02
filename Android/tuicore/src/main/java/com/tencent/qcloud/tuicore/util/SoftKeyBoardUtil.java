package com.tencent.qcloud.tuicore.util;

import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;

import com.tencent.qcloud.tuicore.TUIConfig;

public class SoftKeyBoardUtil {

    public static void hideKeyBoard(IBinder token) {
        InputMethodManager imm = (InputMethodManager) TUIConfig.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(token, 0);
        }
    }

}

