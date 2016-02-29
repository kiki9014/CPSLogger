package cpslab.inhwan.cpslogger_v02;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class textInput extends AppCompatActivity {
    Button okButt;
    TextView txt;
    EditText ans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_input);

        okButt = (Button) findViewById(R.id.okButton);
        txt = (TextView) findViewById(R.id.questionaire);
        ans = (EditText) findViewById(R.id.answer);

        okButt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String data = txt.getText().toString();

                Intent i = new Intent("cpslab.inhwan.cpslogger_v02.GearCommService");
                i.putExtra("data",data);
                sendBroadcast(i);
            }
        });
    }
}
