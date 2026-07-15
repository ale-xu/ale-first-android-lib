package org.auleck.firstbox;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.auleck.first_dialogs.dialogs.address.HsuAddressSelectorDialog;
import org.auleck.first_dialogs.dialogs.date.HsuDateSelectorDialog;
import org.auleck.first_dialogs.dialogs.slide.BaseSlideAaDialog;
import org.auleck.first_dialogs.dialogs.slide.TestSlideDialog;
import org.auleck.first_utils.mmkv.MMKVUtils;
import org.auleck.first_views.FirstViewsUtil;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button1 = findViewById(R.id.main_btn1) ;
        Button button2 = findViewById(R.id.main_btn2) ;
        Button button3 = findViewById(R.id.main_btn3) ;

        MMKVUtils.putString("USER_UID","15837079884");

        button1.setOnClickListener(v -> {
            HsuDateSelectorDialog dialog = new HsuDateSelectorDialog(this);
            dialog.setOnConfirmListener((year, month, day) -> {
                String dateStr = year + "-" + month + "-" + day;
                Toast.makeText(this, dateStr, Toast.LENGTH_SHORT).show();
            });

            dialog.show();
        });
        button2.setOnClickListener(v -> {
            Toast.makeText(this, FirstViewsUtil.ToastText, Toast.LENGTH_SHORT).show();
        });
        button3.setOnClickListener(v -> {

            Toast.makeText(this, MMKVUtils.getString("USER_UID"), Toast.LENGTH_SHORT).show();
        });
    }


}