package com.example.kai.map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

public class SetMap extends AppCompatActivity {

    private Button btn_BackMap;
    private Button btn_Normal;
    private Button btn_Hybrid;
    private Button btn_Satellite;
    private Button btn_Terrain;
    private SeekBar skb_StableRange;
        private TextView txt_Range;
        private int Range;
    private SeekBar skb_Speed;
        private TextView txt_Speed;
        private int Speed;
    private int SelectMapType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_map);

        FindViews();
        SetListener();

        getStatusData();
    }
/*======================================*/
//FindViews
/*======================================*/
    private void FindViews() {
        btn_BackMap = (Button)findViewById(R.id.button_BackMap);
        txt_Range = (TextView)findViewById(R.id.textViewRange);
        skb_StableRange = (SeekBar)findViewById(R.id.seekBarRange);
        txt_Speed = (TextView)findViewById(R.id.textViewSpeed);
        skb_Speed = (SeekBar)findViewById(R.id.seekBarSpeed);
        btn_Normal = (Button)findViewById(R.id.button_Normal);
        btn_Hybrid = (Button)findViewById(R.id.button_Hybrid);
        btn_Satellite = (Button)findViewById(R.id.button_Satellite);
        btn_Terrain = (Button)findViewById(R.id.button_Terrain);
    }
/*======================================*/
//SetListener
/*======================================*/
    private void SetListener() {
        btn_BackMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                Bundle b = new Bundle();
                b.putInt("MapType",SelectMapType);
                b.putInt("StableRange",Range);
                b.putInt("Speed",Speed);
                i.putExtras(b);
                SetMap.this.setResult(RESULT_OK, i);
                finish();
            }
        });
        skb_StableRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressRange, boolean fromUser) {
                Range = progressRange;
                txt_Range.setText("靜止角度:±"+Range+"度");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        skb_Speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressSpeed, boolean fromUser) {
                Speed = progressSpeed;
                txt_Speed.setText("移動速度:"+Speed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        btn_Normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });
        btn_Hybrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });
        btn_Satellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        });
        btn_Terrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
        });
    }
/*======================================*/
//getStatusData
/*======================================*/
    private void getStatusData() {
        Bundle b = getIntent().getExtras();
        if(b != null) {
            SetMapType(b.getInt("MapType"));
            Range = b.getInt("StableRange");
            Speed = b.getInt("Speed");
            txt_Range.setText("靜止角度:±"+Range+"度");
            txt_Speed.setText("移動速度:"+Speed);
            skb_StableRange.setProgress(Range);
            skb_Speed.setProgress(Speed);
        }
    }
/*======================================*/
//SetMapType
/*======================================*/
    private void SetMapType(int MapType){
        SelectMapType = MapType;
        btn_Normal.setBackgroundColor(0xFF3C3C3C);
        btn_Normal.setTextColor(0xFF00CACA);
        btn_Hybrid.setBackgroundColor(0xFF3C3C3C);
        btn_Hybrid.setTextColor(0xFF00CACA);
        btn_Satellite.setBackgroundColor(0xFF3C3C3C);
        btn_Satellite.setTextColor(0xFF00CACA);
        btn_Terrain.setBackgroundColor(0xFF3C3C3C);
        btn_Terrain.setTextColor(0xFF00CACA);
        switch (MapType) {
            case GoogleMap.MAP_TYPE_NORMAL :
                btn_Normal.setBackgroundColor(0xFF00CACA);
                btn_Normal.setTextColor(0xFF3C3C3C);
                break;
            case GoogleMap.MAP_TYPE_HYBRID :
                btn_Hybrid.setBackgroundColor(0xFF00CACA);
                btn_Hybrid.setTextColor(0xFF3C3C3C);
                break;
            case GoogleMap.MAP_TYPE_SATELLITE :
                btn_Satellite.setBackgroundColor(0xFF00CACA);
                btn_Satellite.setTextColor(0xFF3C3C3C);
                break;
            case GoogleMap.MAP_TYPE_TERRAIN :
                btn_Terrain.setBackgroundColor(0xFF00CACA);
                btn_Terrain.setTextColor(0xFF3C3C3C);
                break;
            default:
                break;
        }
    }
}

