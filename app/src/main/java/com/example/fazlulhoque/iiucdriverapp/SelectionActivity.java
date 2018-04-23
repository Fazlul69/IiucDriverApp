package com.example.fazlulhoque.iiucdriverapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SelectionActivity extends AppCompatActivity {

    private static RadioGroup radio_gr;
    private static RadioButton maleRadio, femaleRadio, teacherRadio;
    private static Button btnGoTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        radio_gr = (RadioGroup)findViewById(R.id.genderGrp);
        maleRadio = (RadioButton)findViewById(R.id.maleRadio);
        femaleRadio = (RadioButton)findViewById(R.id.femaleRadio);
        teacherRadio = (RadioButton)findViewById(R.id.teacherRadio);

        btnGoTo = (Button)findViewById(R.id.btnGoTo);

        btnGoTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(maleRadio.isChecked()){
                    Intent intent=new Intent(SelectionActivity.this,DriverMapActivity.class);

                    intent.putExtra("gender","male");
                    startActivity(intent);
                }

                if(femaleRadio.isChecked()){
                    Intent intent=new Intent(SelectionActivity.this,DriverMapActivity.class);

                    intent.putExtra("gender","female");
                    startActivity(intent);
                }

                if(teacherRadio.isChecked()){
                    Intent intent=new Intent(SelectionActivity.this,DriverMapActivity.class);

                    intent.putExtra("gender","teacher");
                    startActivity(intent);
                }

            }
        });

    }
}
