package org.auleck.first_dialogs.dialogs.slide;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.auleck.first_dialogs.R;

public class TestSlideDialog extends BaseSlideAaDialog {

    public TestSlideDialog(Context context) {
        super(context, new Builder()
                .setDirection(BaseSlideAaDialog.DIRECTION_LEFT) // 滑出方向
                .setWidth(0.66f) // 宽度权重
                .setHeight(1f)  // 高度权重
                .setBackground(new ColorDrawable(Color.WHITE)) // 背景色
                .setAnimDuration(160) // 动画时间
                .setCancelableOutside(true) // 是否允许点击空白取消
        );
    }

    @Override
    protected View getContentView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.first_side_dialog_base, null);
        Button btnClose = view.findViewById(R.id.fsdab_test_btn);
        btnClose.setOnClickListener(v -> {
            Toast.makeText(getContext(), "点击了弹窗内容", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        return view;
    }
}

/*
*
* 使用方法，在Activity或Fragment中进行调用
* TestSlideDialog dialog = new TestSlideDialog(this);
* dialog.show();
*
* */
