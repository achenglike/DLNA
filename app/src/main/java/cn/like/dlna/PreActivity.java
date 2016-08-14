package cn.like.dlna;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PreActivity extends AppCompatActivity {
    EditText etOnline;
    EditText etLocal;
    Button btOnline;
    Button btLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre);
        etOnline = (EditText) findViewById(R.id.et_online);
        etLocal = (EditText) findViewById(R.id.et_local);
        btOnline = (Button) findViewById(R.id.bt_online);
        btLocal = (Button) findViewById(R.id.bt_local);

        btOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = etOnline.getText().toString();
                if (TextUtils.isEmpty(url)) {
                    Toast.makeText(PreActivity.this, "please input online media url", Toast.LENGTH_SHORT).show();
                    return;
                }
                MainActivity.playOnLineUrl(PreActivity.this, url);
            }
        });

        btLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = etLocal.getText().toString();
                if (TextUtils.isEmpty(url)) {
                    Toast.makeText(PreActivity.this, "please input local media path", Toast.LENGTH_SHORT).show();
                    return;
                }
                MainActivity.playLocalData(PreActivity.this, url);
            }
        });
    }
}
