package utp.edu.wykrywaczswiatel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class photoDetails extends AppCompatActivity {

    private TextView info;
    private ImageView lightView;
    private RoomConnect roomConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_details);
        roomConnect= new RoomConnect(this);
        info = (TextView) findViewById(R.id.info);
        lightView = (ImageView) findViewById(R.id.lightView);
        info.setText(BazaWykrywaczSwiatel.lightName);
        lightView.setImageBitmap(BazaWykrywaczSwiatel.trafficLight);

    }

    public void MainView(View view) {
        roomConnect.DeleteFromId(BazaWykrywaczSwiatel.id);
        Toast.makeText(this, "Usunięto wynik!", Toast.LENGTH_LONG).show();
        finish();
    }
}