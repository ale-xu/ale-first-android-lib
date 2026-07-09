package org.auleck.first_dialogs.alerts;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import org.auleck.first_dialogs.R;

public class TextPopupFirstAlert extends Dialog {
    public TextPopupFirstAlert(@NonNull Context context) {
        super(context);
    }

    public TextPopupFirstAlert(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public void open() {
        if (!isShowing()){
            show();
        }
    }

    public void close() {
        if (isShowing()){
            dismiss();
        }
    }

    public interface TextPopupFirstAlertImpl{
        void onCancelClick() ;
    }

    public static class Builder{
        private Context context ;
        private String alertTitle ;
        private String alertContent ;
        private boolean autoCancel = true ;
        private TextPopupFirstAlertImpl alertImpl ;


        public Builder(Context context){
            this.context = context ;
        }

        public Builder titleText(String title){
            this.alertTitle = title ;
            return this ;
        }

        public Builder contentText(String content){
            this.alertContent = content ;
            return this ;
        }

        public Builder autoCancel(Boolean autoCancel){
            this.autoCancel = autoCancel ;
            return this;
        }

        public Builder onCancelClick(TextPopupFirstAlertImpl impl){
            this.alertImpl = impl ;
            return this ;
        }

        public TextPopupFirstAlert create(){
            final TextPopupFirstAlert alert = new TextPopupFirstAlert(context);

            View view = LayoutInflater.from(context).inflate(R.layout.text_popup_first_alert, null);

            TextView contentTv = view.findViewById(R.id.text_popup_first_alert_contentTv) ;
            CardView cancelBtn = view.findViewById(R.id.text_popup_first_alert_cancelCard) ;
            TextView titleTv = view.findViewById(R.id.text_popup_first_alert_titleTv) ;

            if (alertTitle.trim().isEmpty()){
                titleTv.setVisibility(View.GONE);
            }else {
                titleTv.setText(alertTitle);
            }

            contentTv.setText(alertContent);
            cancelBtn.setOnClickListener(v -> {
                alertImpl.onCancelClick();
                alert.dismiss();});



            alert.setContentView(view);
            alert.setCancelable(autoCancel);


            // 去除系统默认白色背景，让自定义背景生效
            Window window = alert.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.argb(35, 255, 255, 255)));
            }

            return alert;
        }

        public TextPopupFirstAlert show(){
            TextPopupFirstAlert alert = create();
            alert.show();
            return alert;
        }
    }
}
