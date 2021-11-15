package com.tencent.qcloud.tuikit.tuibeauty.presenter;

import android.graphics.Bitmap;

import androidx.annotation.IntRange;

import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.qcloud.tuikit.tuibeauty.model.TUIBeautyInfo;
import com.tencent.qcloud.tuikit.tuibeauty.model.TUIBeautyItemInfo;
import com.tencent.qcloud.tuikit.tuibeauty.model.TUIBeautyTabInfo;

public interface ITUIBeautyPresenter {
    /**
     * 设置美颜管理类
     *
     * @param beautyManager
     */
    void setBeautyManager(TXBeautyManager beautyManager);

    /**
     * 设置美颜效果
     *
     * @param tabInfo      tab信息,包括:美颜,滤镜,动效,美妆,手势,AI抠背,绿幕等
     * @param tabPosition  tab位置
     * @param itemInfo     每个分类里面的不同type,比如,美颜包括:光滑,自然,美白,红润等等
     * @param itemPosition item位置
     */
    void setBeautySpecialEffects(TUIBeautyTabInfo tabInfo, @IntRange(from = 0) int tabPosition, TUIBeautyItemInfo itemInfo, @IntRange(from = 0) int itemPosition);

    /**
     * 设置美颜类型
     *
     * @param style 美颜风格
     * @param level 强度等级
     */
    void setBeautyStyleAndLevel(int style, int level);

    /**
     * 是否支持高级美颜
     *
     * @param enable
     */
    void setMotionTmplEnable(boolean enable);

    /**
     * 美颜数据本地存储
     *
     * @param beautyInfo
     */
    void fillingMaterialPath(TUIBeautyInfo beautyInfo);

    /**
     * 清空美颜选项
     */
    void clear();

    /**
     * 返回美颜选择项
     *
     * @param beautyInfo
     * @param index
     */
    TUIBeautyItemInfo getFilterItemInfo(TUIBeautyInfo beautyInfo, @IntRange(from = 0) int index);

    /**
     * 获取美颜数据
     */
    TUIBeautyInfo getDefaultBeauty();

    /**
     * 美颜强度变化监听
     */
    interface OnFilterChangeListener {
        void onChanged(Bitmap filterImage, int index);
    }
}
