package utp.edu.wykrywaczswiatel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class BazaWykrywaczSwiatel extends AppCompatActivity {

    private RoomConnect roomConnect;

    public Button clear;

    public static Bitmap trafficLight;
    public static String lightName;
    public static int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baza_wykrywacz_swiatel);
        roomConnect= new RoomConnect(this);
        clear = (Button) findViewById(R.id.clear);
        GetData();
    }

    public void ClearAll(View view) {
        //roomConnect.DeleteAll();
        //Toast.makeText(this, "Baza została wyczyśzczona!", Toast.LENGTH_LONG).show();
        //recreate();
    }

    private void GetData()
    {
        ArrayList<LightData> lr = roomConnect.getAllData();

        clear.setText(String.valueOf(lr.size()) + " " + clear.getText());

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.databody);

        for (LightData lightData : lr) {
            Button btn1 = new Button(this);
            btn1.setText(String.valueOf(lightData.id) + " " + lightData.data.toString());
            btn1.setId(lightData.id);

            linearLayout.addView(btn1);

            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LightData lightData = roomConnect.getDataFromId(v.getId());
                    //tutaj ładowanie wyniku
                    trafficLight = lightData.bitmap;
                    id = lightData.id;
                    lightName = lightData.data.toString() + "\n";
                    lightName += "Lightness: " + String.valueOf(lightData.lightness) + "\n";
                    if (lightData.light == LightResults.Light.LIGHT_GREEN) {
                        lightName += "Light green!";
                    } else if (lightData.light == LightResults.Light.LIGHT_RED) {
                        lightName += "Light red!";
                    } else if (lightData.light == LightResults.Light.LIGHT_YELLOW) {
                        lightName += "Light yellow!";
                    } else if (lightData.light == LightResults.Light.LIGHT_NULL) {
                        lightName += "Light not detected!";
                    }

                    Intent intent = new Intent(v.getContext(), photoDetails.class);
                    startActivity(intent);
                }
            });
        }
    }
}