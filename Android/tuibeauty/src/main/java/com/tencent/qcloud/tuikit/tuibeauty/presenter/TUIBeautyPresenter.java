package com.tencent.qcloud.tuikit.tuibeauty.presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.StringRes;

import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.qcloud.tuikit.tuibeauty.R;
import com.tencent.qcloud.tuikit.tuibeauty.model.TUIBeautyConstants;
import com.tencent.qcloud.tuikit.tuibeauty.model.TUIBeautyInfo;
import com.tencent.qcloud.tuikit.tuibeauty.model.TUIBeautyItemInfo;
import com.tencent.qcloud.tuikit.tuibeauty.model.TUIBeautyParams;
import com.tencent.qcloud.tuikit.tuibeauty.model.TUIBeautyTabInfo;
import com.tencent.qcloud.tuikit.tuibeauty.model.download.DownloadListener;
import com.tencent.qcloud.tuikit.tuibeauty.model.download.MaterialDownloader;
import com.tencent.qcloud.tuikit.tuibeauty.view.internal.ProgressDialog;
import com.tencent.qcloud.tuikit.tuibeauty.view.utils.ResourceUtils;
import com.tencent.qcloud.tuikit.tuibeauty.view.utils.SPUtils;
import com.tencent.qcloud.tuikit.tuibeauty.view.utils.TUIBeautyResourceParse;

import java.util.List;

public class TUIBeautyPresenter implements ITUIBeautyPresenter {

    private static final String TAG = "TUIBeautyPresenter";

    private Context                mContext;
    private TUIBeautyParams        mTUIBeautyParams;
    private TXBeautyManager        mBeautyManager;
    private OnFilterChangeListener mOnFilterChangeListener;

    public TUIBeautyPresenter(Context context, TXBeautyManager beautyManager) {
        mContext = context;
        mTUIBeautyParams = new TUIBeautyParams();
        mBeautyManager = beautyManager;
    }

    @Override
    public void setBeautyManager(TXBeautyManager beautyManager) {
        mBeautyManager = beautyManager;
    }

    @Override
    public void setBeautySpecialEffects(TUIBeautyTabInfo tabInfo, @IntRange(from = 0) int tabPosition,
                                        TUIBeautyItemInfo itemInfo, @IntRange(from = 0) int itemPosition) {
        dispatchEffects(tabInfo, tabPosition, itemInfo, itemPosition);
    }

    @Override
    public void setBeautyStyleAndLevel(int style, int level) {
        if (mBeautyManager != null) {
            setBeautyStyle(style);
            setBeautyLevel(level);
        }
    }

    /**
     * 设置指定素材滤镜特效
     *
     * @param filterImage 指定素材，即颜色查找表图片。必须使用 png 格式
     * @param index
     */
    private void setFilter(Bitmap filterImage, int index) {
        mTUIBeautyParams.mFilterBmp = filterImage;
        mTUIBeautyParams.mFilterIndex = index;
        if (mBeautyManager != null) {
            mBeautyManager.setFilter(filterImage);
            if (mOnFilterChangeListener != null) {
                mOnFilterChangeListener.onChanged(filterImage, index);
            }
        }
    }

    /**
     * 设置滤镜浓度
     *
     * @param strength 从0到1，越大滤镜效果越明显，默认值为0.5
     */
    private void setFilterStrength(float strength) {
        mTUIBeautyParams.mFilterMixLevel = strength;
        if (mBeautyManager != null) {
            mBeautyManager.setFilterStrength(strength);
        }
    }

    /**
     * 设置绿幕文件
     *
     * @param path 视频文件路径。支持 MP4；null 表示关闭特效
     */
    private void setGreenScreenFile(String path) {
        mTUIBeautyParams.mGreenFile = path;
        if (mBeautyManager != null) {
            mBeautyManager.setGreenScreenFile(path);
        }
    }

    /**
     * 设置美颜类型
     *
     * @param beautyStyle 美颜风格.三种美颜风格：0 ：光滑 1：自然 2：朦胧
     */
    private void setBeautyStyle(int beautyStyle) {
        if (beautyStyle >= 3 || beautyStyle < 0) {
            return;
        }
        mTUIBeautyParams.mBeautyStyle = beautyStyle;
        if (mBeautyManager != null) {
            mBeautyManager.setBeautyStyle(beautyStyle);
        }
    }

    /**
     * 设置美颜级别
     *
     * @param beautyLevel 美颜级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setBeautyLevel(int beautyLevel) {
        mTUIBeautyParams.mBeautyLevel = beautyLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setBeautyLevel(beautyLevel);
        }
    }

    /**
     * 设置美白级别
     *
     * @param whitenessLevel 美白级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setWhitenessLevel(int whitenessLevel) {
        mTUIBeautyParams.mWhiteLevel = whitenessLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setWhitenessLevel(whitenessLevel);
        }
    }

    /**
     * 设置红润级别
     *
     * @param ruddyLevel 红润级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setRuddyLevel(int ruddyLevel) {
        mTUIBeautyParams.mRuddyLevel = ruddyLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setRuddyLevel(ruddyLevel);
        }
    }

    /**
     * 设置大眼级别
     *
     * @param eyeScaleLevel 大眼级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setEyeScaleLevel(int eyeScaleLevel) {
        mTUIBeautyParams.mBigEyeLevel = eyeScaleLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setEyeScaleLevel(eyeScaleLevel);
        }
    }

    /**
     * 设置瘦脸级别
     *
     * @param faceSlimLevel 瘦脸级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setFaceSlimLevel(int faceSlimLevel) {
        mTUIBeautyParams.mFaceSlimLevel = faceSlimLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setFaceSlimLevel(faceSlimLevel);
        }
    }

    /**
     * 设置 V 脸级别
     *
     * @param faceVLevel V脸级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setFaceVLevel(int faceVLevel) {
        mTUIBeautyParams.mFaceVLevel = faceVLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setFaceVLevel(faceVLevel);
        }
    }

    /**
     * 设置下巴拉伸或收缩
     *
     * @param chinLevel 下巴拉伸或收缩级别，取值范围 -9 - 9；0 表示关闭，小于0表示收缩，大于0表示拉伸
     */
    private void setChinLevel(int chinLevel) {
        mTUIBeautyParams.mChinSlimLevel = chinLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setChinLevel(chinLevel);
        }
    }

    /**
     * 设置短脸级别
     *
     * @param faceShortLevel 短脸级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setFaceShortLevel(int faceShortLevel) {
        mTUIBeautyParams.mFaceShortLevel = faceShortLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setFaceShortLevel(faceShortLevel);
        }
    }

    /**
     * 设置瘦鼻级别
     *
     * @param noseSlimLevel 瘦鼻级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setNoseSlimLevel(int noseSlimLevel) {
        mTUIBeautyParams.mNoseSlimLevel = noseSlimLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setNoseSlimLevel(noseSlimLevel);
        }
    }

    /**
     * 设置亮眼
     *
     * @param eyeLightenLevel 亮眼级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setEyeLightenLevel(int eyeLightenLevel) {
        mTUIBeautyParams.mEyeLightenLevel = eyeLightenLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setEyeLightenLevel(eyeLightenLevel);
        }
    }

    /**
     * 设置白牙
     *
     * @param toothWhitenLevel 亮眼级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setToothWhitenLevel(int toothWhitenLevel) {
        mTUIBeautyParams.mToothWhitenLevel = toothWhitenLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setToothWhitenLevel(toothWhitenLevel);
        }
    }

    /**
     * 设置祛皱
     *
     * @param wrinkleRemoveLevel 祛皱级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setWrinkleRemoveLevel(int wrinkleRemoveLevel) {
        mTUIBeautyParams.mWrinkleRemoveLevel = wrinkleRemoveLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setWrinkleRemoveLevel(wrinkleRemoveLevel);
        }
    }

    /**
     * 设置祛眼袋
     *
     * @param pounchRemoveLevel 祛眼袋级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setPounchRemoveLevel(int pounchRemoveLevel) {
        mTUIBeautyParams.mPounchRemoveLevel = pounchRemoveLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setPounchRemoveLevel(pounchRemoveLevel);
        }
    }

    /**
     * 设置祛法令纹
     *
     * @param smileLinesRemoveLevel 祛法令纹级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setSmileLinesRemoveLevel(int smileLinesRemoveLevel) {
        mTUIBeautyParams.mSmileLinesRemoveLevel = smileLinesRemoveLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setSmileLinesRemoveLevel(smileLinesRemoveLevel);
        }
    }

    /**
     * 设置发际线
     *
     * @param foreheadLevel 发际线级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setForeheadLevel(int foreheadLevel) {
        mTUIBeautyParams.mForeheadLevel = foreheadLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setForeheadLevel(foreheadLevel);
        }
    }

    /**
     * 设置眼距
     *
     * @param eyeDistanceLevel 眼距级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setEyeDistanceLevel(int eyeDistanceLevel) {
        mTUIBeautyParams.mEyeDistanceLevel = eyeDistanceLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setEyeDistanceLevel(eyeDistanceLevel);
        }
    }

    /**
     * 设置眼角
     *
     * @param eyeAngleLevel 眼角级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setEyeAngleLevel(int eyeAngleLevel) {
        mTUIBeautyParams.mEyeAngleLevel = eyeAngleLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setEyeAngleLevel(eyeAngleLevel);
        }
    }

    /**
     * 设置嘴型
     *
     * @param mouthShapeLevel 嘴型级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setMouthShapeLevel(int mouthShapeLevel) {
        mTUIBeautyParams.mMouthShapeLevel = mouthShapeLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setMouthShapeLevel(mouthShapeLevel);
        }
    }

    private void setNoseWingLevel(int noseWingLevel) {
        mTUIBeautyParams.mNoseWingLevel = noseWingLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setNoseWingLevel(noseWingLevel);
        }
    }

    /**
     * 设置鼻翼
     *
     * @param nosePositionLevel 鼻翼级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setNosePositionLevel(int nosePositionLevel) {
        mTUIBeautyParams.mNosePositionLevel = nosePositionLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setNosePositionLevel(nosePositionLevel);
        }
    }

    /**
     * 设置嘴唇厚度
     *
     * @param lipsThicknessLevel 嘴唇厚度级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setLipsThicknessLevel(int lipsThicknessLevel) {
        mTUIBeautyParams.mMouthShapeLevel = lipsThicknessLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setLipsThicknessLevel(lipsThicknessLevel);
        }
    }

    /**
     * 设置脸型
     *
     * @param faceBeautyLevel 脸型级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显
     */
    private void setFaceBeautyLevel(int faceBeautyLevel) {
        mTUIBeautyParams.mFaceBeautyLevel = faceBeautyLevel;
        if (mBeautyManager != null) {
            mBeautyManager.setFaceBeautyLevel(faceBeautyLevel);
        }
    }

    /**
     * 选择使用哪一款 AI 动效挂件
     *
     * @param tmplPath
     */
    private void setMotionTmpl(String tmplPath) {
        mTUIBeautyParams.mMotionTmplPath = tmplPath;
        if (mBeautyManager != null) {
            mBeautyManager.setMotionTmpl(tmplPath);
        }
    }

    @Override
    public void setMotionTmplEnable(boolean enable) {
        if (mBeautyManager != null) {
            if (enable) {
                mBeautyManager.setMotionTmpl(null);
            } else {
                mBeautyManager.setMotionTmpl(mTUIBeautyParams.mMotionTmplPath);
            }
        }
    }

    @Override
    public void fillingMaterialPath(TUIBeautyInfo beautyInfo) {
        for (TUIBeautyTabInfo tabInfo : beautyInfo.getBeautyTabList()) {
            List<TUIBeautyItemInfo> tabItemList = tabInfo.getTabItemList();
            for (TUIBeautyItemInfo itemInfo : tabItemList) {
                itemInfo.setItemMaterialPath(SPUtils.get().getString(getMaterialPathKey(itemInfo)));
            }
        }
    }

    /**
     * 清空美颜配置，如果SDK是新创建的则不需要最后清理，如果SDK是单例，需要调用此方法清空上次设置的美颜参数<br/>
     * 示例：TXUGCRecord是单例，需要调用，TXLivePusher每次创建新的，不需要调用
     */
    @Override
    public void clear() {
        mTUIBeautyParams = new TUIBeautyParams();
        if (mBeautyManager != null) {
            mBeautyManager.setFilter(mTUIBeautyParams.mFilterBmp);
            mBeautyManager.setFilterStrength(mTUIBeautyParams.mFilterMixLevel);
            mBeautyManager.setGreenScreenFile(mTUIBeautyParams.mGreenFile);
            mBeautyManager.setBeautyStyle(mTUIBeautyParams.mBeautyStyle);
            mBeautyManager.setBeautyLevel(mTUIBeautyParams.mBeautyLevel);
            mBeautyManager.setWhitenessLevel(mTUIBeautyParams.mWhiteLevel);
            mBeautyManager.setRuddyLevel(mTUIBeautyParams.mRuddyLevel);
            mBeautyManager.setEyeScaleLevel(mTUIBeautyParams.mBigEyeLevel);
            mBeautyManager.setFaceSlimLevel(mTUIBeautyParams.mFaceSlimLevel);
            mBeautyManager.setFaceVLevel(mTUIBeautyParams.mFaceVLevel);
            mBeautyManager.setChinLevel(mTUIBeautyParams.mChinSlimLevel);
            mBeautyManager.setFaceShortLevel(mTUIBeautyParams.mFaceShortLevel);
            mBeautyManager.setNoseSlimLevel(mTUIBeautyParams.mNoseSlimLevel);
            mBeautyManager.setEyeLightenLevel(mTUIBeautyParams.mEyeLightenLevel);
            mBeautyManager.setToothWhitenLevel(mTUIBeautyParams.mToothWhitenLevel);
            mBeautyManager.setWrinkleRemoveLevel(mTUIBeautyParams.mWrinkleRemoveLevel);
            mBeautyManager.setPounchRemoveLevel(mTUIBeautyParams.mPounchRemoveLevel);
            mBeautyManager.setSmileLinesRemoveLevel(mTUIBeautyParams.mSmileLinesRemoveLevel);
            mBeautyManager.setForeheadLevel(mTUIBeautyParams.mForeheadLevel);
            mBeautyManager.setEyeDistanceLevel(mTUIBeautyParams.mEyeDistanceLevel);
            mBeautyManager.setEyeAngleLevel(mTUIBeautyParams.mEyeAngleLevel);
            mBeautyManager.setMouthShapeLevel(mTUIBeautyParams.mMouthShapeLevel);
            mBeautyManager.setNoseWingLevel(mTUIBeautyParams.mNoseWingLevel);
            mBeautyManager.setNosePositionLevel(mTUIBeautyParams.mNosePositionLevel);
            mBeautyManager.setLipsThicknessLevel(mTUIBeautyParams.mLipsThicknessLevel);
            mBeautyManager.setFaceBeautyLevel(mTUIBeautyParams.mFaceBeautyLevel);
            mBeautyManager.setMotionTmpl(mTUIBeautyParams.mMotionTmplPath);
        }
    }

    @Override
    public TUIBeautyItemInfo getFilterItemInfo(TUIBeautyInfo beautyInfo, int index) {
        for (TUIBeautyTabInfo tabInfo : beautyInfo.getBeautyTabList()) {
            if (tabInfo.getTabType() == TUIBeautyConstants.TAB_TYPE_FILTER) {
                return tabInfo.getTabItemList().get(index);
            }
        }
        return null;
    }

    @Override
    public TUIBeautyInfo getDefaultBeauty() {
        return TUIBeautyResourceParse.getDefaultBeautyInfo();
    }

    private void dispatchEffects(TUIBeautyTabInfo tabInfo, @IntRange(from = 0) int tabPosition, TUIBeautyItemInfo itemInfo, @IntRange(from = 0) int itemPosition) {
        int tabType = tabInfo.getTabType();
        switch (tabType) {
            case TUIBeautyConstants.TAB_TYPE_BEAUTY:
                dispatchBeautyEffects(itemInfo);
                break;
            case TUIBeautyConstants.TAB_TYPE_FILTER:
                dispatchFilterEffects(itemInfo, itemPosition);
                break;
            case TUIBeautyConstants.TAB_TYPE_MOTION:
            case TUIBeautyConstants.TAB_TYPE_BEAUTY_FACE:
            case TUIBeautyConstants.TAB_TYPE_GESTURE:
            case TUIBeautyConstants.TAB_TYPE_CUTOUT_BACKGROUND:
                setMaterialEffects(tabInfo, itemInfo);
                break;
            case TUIBeautyConstants.TAB_TYPE_GREEN:
                String file = "";
                if (itemInfo.getItemType() == TUIBeautyConstants.ITEM_TYPE_GREEN_GOOD_LUCK) {
                    file = "green_1.mp4";
                }
                setGreenScreenFile(file);
                break;
            default:
                break;
        }
    }

    private void dispatchBeautyEffects(TUIBeautyItemInfo itemInfo) {
        int itemType = itemInfo.getItemType();
        int level = itemInfo.getItemLevel();
        switch (itemType) {
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_SMOOTH:           // 光滑
                setBeautyStyleAndLevel(0, level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_NATURAL:          // 自然
                setBeautyStyleAndLevel(1, level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_PITU:             // 天天p图
                setBeautyStyleAndLevel(2, level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_WHITE:            // 美白
                setWhitenessLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_RUDDY:            // 红润
                setRuddyLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_BIG_EYE:          // 大眼
                setEyeScaleLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_FACES_LIM:        // 瘦脸
                setFaceSlimLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_FACEV:            // V脸
                setFaceVLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_CHIN:             // 下巴
                setChinLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_FACE_SHORT:       // 短脸
                setFaceShortLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_NOSES_LIM:        // 瘦鼻
                setNoseSlimLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_EYE_BRIGHT:       // 亮眼
                setEyeLightenLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_TOOTH_WHITE:      // 白牙
                setToothWhitenLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_WRINKLE_REMOVE:   // 祛皱
                setWrinkleRemoveLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_POUCH_REMOVE:     // 祛眼袋
                setPounchRemoveLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_SMILE_LINES:      // 袪法令纹
                setSmileLinesRemoveLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_FOREHEAD:         // 发际线
                setForeheadLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_EYE_DISTANCE:     // 眼距
                setEyeDistanceLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_EYE_ANGLE:        // 眼角
                setEyeAngleLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_MOUTH_SHAPE:      // 嘴型
                setMouthShapeLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_NOSEWING:         // 鼻翼
                setNoseWingLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_NOSE_POSITION:    // 鼻子位置
                setNosePositionLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_MOUSE_WIDTH:      // 嘴唇厚度
                setLipsThicknessLevel(level);
                break;
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_FACE_SHAPE:       // 脸型
                setFaceBeautyLevel(level);
                break;
        }
    }

    private void dispatchFilterEffects(TUIBeautyItemInfo itemInfo, int position) {
        Bitmap bitmap = decodeFilterResource(itemInfo);
        mTUIBeautyParams.mFilterBmp = bitmap;
        mTUIBeautyParams.mFilterIndex = position;
        setFilter(bitmap, position);
        setFilterStrength(itemInfo.getItemLevel() / 10.0f);
    }

    private void setMaterialEffects(final TUIBeautyTabInfo tabInfo, final TUIBeautyItemInfo itemInfo) {
        String itemMaterialPath = itemInfo.getItemMaterialPath();
        if (!TextUtils.isEmpty(itemMaterialPath)) {
            if (tabInfo.getTabType() == TUIBeautyConstants.TAB_TYPE_GESTURE
                    && itemInfo.getItemId() == 2) { // 皮卡丘 item 特殊逻辑
                showToast(ResourceUtils.getString(R.string.tuibeauty_palm_out));
            }
            setMotionTmpl(itemMaterialPath);
            return;
        }
        int itemType = itemInfo.getItemType();
        switch (itemType) {
            case TUIBeautyConstants.ITEM_TYPE_MOTION_NONE:
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_FACE_NONE:
            case TUIBeautyConstants.ITEM_TYPE_GESTURE_NONE:
            case TUIBeautyConstants.ITEM_TYPE_CUTOUT_BACKGROUND_NONE:
                setMotionTmpl("");
                break;
            case TUIBeautyConstants.ITEM_TYPE_MOTION_MATERIAL:
            case TUIBeautyConstants.ITEM_TYPE_BEAUTY_FACE_MATERIAL:
            case TUIBeautyConstants.ITEM_TYPE_GESTURE_MATERIAL:
            case TUIBeautyConstants.ITEM_TYPE_CUTOUT_BACKGROUND_MATERIAL:
                downloadVideoMaterial(tabInfo, itemInfo);
                break;
        }
    }

    private void downloadVideoMaterial(final TUIBeautyTabInfo tabInfo, final TUIBeautyItemInfo itemInfo) {
        MaterialDownloader materialDownloader = new MaterialDownloader(mContext, ResourceUtils.getString(itemInfo.getItemName()), itemInfo.getItemMaterialUrl());
        materialDownloader.start(new DownloadListener() {

            private ProgressDialog mProgressDialog;

            @Override
            public void onDownloadFail(final String errorMsg) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }
                        Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onDownloadProgress(final int progress) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "onDownloadProgress, progress = " + progress);
                        if (mProgressDialog == null) {
                            mProgressDialog = new ProgressDialog();
                            mProgressDialog.createLoadingDialog(mContext);
                            mProgressDialog.setCancelable(false);               // 设置是否可以通过点击Back键取消
                            mProgressDialog.setCanceledOnTouchOutside(false);   // 设置在点击Dialog外是否取消Dialog进度条
                            mProgressDialog.show();
                        }
                        mProgressDialog.setMsg(progress + "%");
                    }
                });
            }

            @Override
            public void onDownloadSuccess(String filePath) {
                itemInfo.setItemMaterialPath(filePath);
                SPUtils.get().put(getMaterialPathKey(itemInfo), filePath);

                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        setMaterialEffects(tabInfo, itemInfo);
                    }
                });
            }
        });
    }

    private Bitmap decodeFilterResource(TUIBeautyItemInfo itemInfo) {
        int itemType = itemInfo.getItemType();
        int resId = 0;
        switch (itemType) {
            case TUIBeautyConstants.ITEM_TYPE_FILTER_FACE_SHAPE:
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_STANDARD:
                resId = R.drawable.tuibeauty_filter_biaozhun;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_ZIRAN:
                resId = R.drawable.tuibeauty_filter_ziran;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_BAIXI:
                resId = R.drawable.tuibeauty_filter_baixi;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_CHEERY:
                resId = R.drawable.tuibeauty_filter_yinghong;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_CLOUD:
                resId = R.drawable.tuibeauty_filter_yunshang;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_PURE:
                resId = R.drawable.tuibeauty_filter_chunzhen;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_ORCHID:
                resId = R.drawable.tuibeauty_filter_bailan;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_VITALITY:
                resId = R.drawable.tuibeauty_filter_yuanqi;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_SUPER:
                resId = R.drawable.tuibeauty_filter_chaotuo;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_FRAGRANCE:
                resId = R.drawable.tuibeauty_filter_xiangfen;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_WHITE:
                resId = R.drawable.tuibeauty_filter_white;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_ROMANTIC:
                resId = R.drawable.tuibeauty_filter_langman;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_FRESH:
                resId = R.drawable.tuibeauty_filter_qingxin;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_BEAUTIFUL:
                resId = R.drawable.tuibeauty_filter_weimei;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_PINK:
                resId = R.drawable.tuibeauty_filter_fennen;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_REMINISCENCE:
                resId = R.drawable.tuibeauty_filter_huaijiu;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_BLUES:
                resId = R.drawable.tuibeauty_filter_landiao;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_COOL:
                resId = R.drawable.tuibeauty_filter_qingliang;
                break;
            case TUIBeautyConstants.ITEM_TYPE_FILTER_JAPANESE:
                resId = R.drawable.tuibeauty_filter_rixi;
                break;
        }
        if (resId != 0) {
            return decodeResource(resId);
        } else {
            return null;
        }
    }

    private Bitmap decodeResource(int id) {
        TypedValue value = new TypedValue();
        ResourceUtils.getResources().openRawResource(id, value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = value.density;
        return BitmapFactory.decodeResource(ResourceUtils.getResources(), id, opts);
    }

    private String getMaterialPathKey(TUIBeautyItemInfo itemInfo) {
        return itemInfo.getItemId() + "-" + itemInfo.getItemType();
    }

    private void showToast(@StringRes int resId) {
        showToast(ResourceUtils.getString(resId));
    }

    private void showToast(String text) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }
}
