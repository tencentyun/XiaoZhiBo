package com.tencent.liteav.demo.aboutme;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.common.view.ShowTipDialogFragment;
import com.tencent.liteav.login.ui.view.ModifyUserAvatarDialog;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfoFragment extends Fragment implements View.OnClickListener {
    private CircleImageView mImageAvatar;
    private TextView        mTextNickName;
    private TextView        mTextUserId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.app_fragment_my_info, container, false);
        mImageAvatar = (CircleImageView) rootView.findViewById(R.id.iv_avatar);
        mTextNickName = (TextView) rootView.findViewById(R.id.tv_show_name);
        mTextUserId = (TextView) rootView.findViewById(R.id.tv_userid);
        String userId = UserModelManager.getInstance().getUserModel().userId;
        String userName = UserModelManager.getInstance().getUserModel().userName;
        String userAvatar = UserModelManager.getInstance().getUserModel().userAvatar;
        ImageLoader.loadImage(getActivity(), mImageAvatar, userAvatar, R.drawable.app_bg_cover);
        mTextNickName.setText(userName);
        mTextUserId.setText(userId);
        rootView.findViewById(R.id.img_show_name).setOnClickListener(this);
        rootView.findViewById(R.id.iv_avatar).setOnClickListener(this);
        rootView.findViewById(R.id.tv_about).setOnClickListener(this);
        rootView.findViewById(R.id.tv_privacy_statement).setOnClickListener(this);
        rootView.findViewById(R.id.tv_user_agreement).setOnClickListener(this);
        rootView.findViewById(R.id.tv_statement).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onResume() {
        String userAvatar = UserModelManager.getInstance().getUserModel().userAvatar;
        ImageLoader.loadImage(getContext(), mImageAvatar, userAvatar, R.drawable.app_bg_cover);
        super.onResume();
    }

    private void showStatementDialog() {
        final ShowTipDialogFragment dialog = new ShowTipDialogFragment();
        dialog.setMessage(getString(R.string.app_statement_detail));
        dialog.show(getActivity().getFragmentManager(), "confirm_fragment");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_show_name:
                ModifyUserNameDialog modifyUserNameDialog = new ModifyUserNameDialog(getActivity(),
                        new ModifyUserNameDialog.ModifySuccessListener() {
                            @Override
                            public void onSuccess() {
                                String userName = UserModelManager.getInstance().getUserModel().userName;
                                mTextNickName.setText(userName);
                            }
                        });
                modifyUserNameDialog.show();
                break;
            case R.id.iv_avatar:
                ModifyUserAvatarDialog modifyUserAvatarDialog = new ModifyUserAvatarDialog(getActivity(),
                        new ModifyUserAvatarDialog.ModifySuccessListener() {
                            @Override
                            public void onSuccess() {
                                String userAvatar = UserModelManager.getInstance().getUserModel().userAvatar;
                                ImageLoader.loadImage(getContext(), mImageAvatar, userAvatar, R.drawable.app_bg_cover);
                            }
                        });
                modifyUserAvatarDialog.show();
                break;
            case R.id.tv_about:
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_privacy_statement:
                Intent privacyIntent = new Intent();
                privacyIntent.addCategory("android.intent.category.DEFAULT");
                privacyIntent.setAction("com.tencent.liteav.action.webview");
                privacyIntent.setPackage(getActivity().getPackageName());
                privacyIntent.putExtra("title", getString(R.string.app_privacy_statement));
                privacyIntent.putExtra("url", "https://web.sdk.qcloud.com/document/Tencent-MLVB-Privacy-Protection-Guidelines.html");
                startActivity(privacyIntent);
                break;
            case R.id.tv_user_agreement:
                Intent agreementIntent = new Intent();
                agreementIntent.addCategory("android.intent.category.DEFAULT");
                agreementIntent.setAction("com.tencent.liteav.action.webview");
                agreementIntent.setPackage(getActivity().getPackageName());
                agreementIntent.putExtra("title", getString(R.string.app_user_agreement));
                agreementIntent.putExtra("url", "https://web.sdk.qcloud.com/document/Tencent-MLVB-User-Agreement.html");
                startActivity(agreementIntent);
                break;
            case R.id.tv_statement:
                showStatementDialog();
                break;
        }

    }
}
