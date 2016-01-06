package com.example.kai.map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import java.lang.Math;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;

public class GoogleMapView extends FragmentActivity implements SensorEventListener {
    private TextView Info1;               //顯示x軸加速度
    private TextView Info2;               //顯示y軸加速度
    private TextView Info3;               //顯示z軸加速度
    private TextView Info4;                 //顯示總加速度
    private TextView Info5;                //顯示總加速度和XY平面夾角
    private TextView Info6;                //顯示總加速度和YZ平面夾角
    private TextView Info7;                 //計數或其他除錯檢查點顯示
    private SensorManager sensorManager; // 管理感應器的東東

    private Button btn_Set;
    private Button btn_FindMe;
    private Button btn_Stop;
        private boolean Stop;
    private Button btn_Rotate;
    private Button btn_Zoom;
        int ModeTemp;
    private Button btn_GoHere;
        private boolean haveGoal;
    private Button btn_Mod;
        int CameraMode;

    private GoogleMap map;
    private LocationManager locationMgr;
    private String provider;
    private Camera camera;
        private int StableRange;
        private int Speed;

    float[] acc;                          // 加速度(包和重力影響)
    float[] velocity;                      //速度
    double angleXZ;
    double angleYZ;
    double angleXY;
        boolean isSetCenterAngle;
    double distance;

/****************************************/
//onCreate
/****************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map_view);

        FindViews();
        setVariable();
        SetListener();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Info1.setText("");
        Info2.setText("");
        Info3.setText("");
        Info4.setText("");
        Info5.setText("");
        Info6.setText("");
            Info6.setBackgroundColor(0xFF3C3C3C);
            Info6.setTextColor(0xFFFFFFFF);
        Info7.setText("距離自己:0m");
            Info7.setBackgroundColor(0xFF3C3C3C);
            Info7.setTextColor(0xFFFFFFFF);
    }
/****************************************/
//onResume
/****************************************/
    @Override
    protected void onResume() {
        super.onResume();

        isSetCenterAngle = false;
        SetSensor();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
/****************************************/
//onStart
/****************************************/
    @Override
    protected void onStart() {
        super.onStart();
    }
/****************************************/
//onPause
/****************************************/
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
/****************************************/
//onRestart
/****************************************/
    @Override
    protected void onRestart() {
        super.onRestart();
    }
/****************************************/
//onActivityResult
/****************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 111){
                map.setMapType(data.getExtras().getInt("MapType"));
                StableRange = data.getExtras().getInt("StableRange");
                Speed = data.getExtras().getInt("Speed");
            }
        }
    }
/*======================================*/
//FindViews
/*======================================*/
    private void FindViews() {
        Info1 = (TextView) this.findViewById(R.id.textView_info1);
        Info2 = (TextView) this.findViewById(R.id.textView_info2);
        Info3 = (TextView) this.findViewById(R.id.textView_Info3);
        Info4 = (TextView) this.findViewById(R.id.textView_info4);
        Info5 = (TextView) this.findViewById(R.id.textView_Info5);
        Info6 = (TextView) this.findViewById(R.id.textView_Info6);
        Info7 = (TextView) this.findViewById(R.id.textView_Info7);
        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        btn_FindMe = (Button)findViewById(R.id.button_FindMe);
        btn_Set = (Button) findViewById(R.id.button_Set);
        btn_Mod = (Button)findViewById(R.id.button_Mod);
        btn_Stop = (Button)findViewById(R.id.button_Stop);
        btn_Rotate = (Button)findViewById(R.id.button_Rotate);
        btn_Zoom = (Button)findViewById(R.id.button_Zoom);
        btn_GoHere = (Button)findViewById(R.id.button_GoHere);
    }
/*======================================*/
//SetListener
/*======================================*/
    private void SetListener() {
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                camera.Lng = cameraPosition.target.longitude;
                camera.Lat = cameraPosition.target.latitude;
                camera.Zoom = cameraPosition.zoom;
                camera.Bearing = cameraPosition.bearing;
                camera.Tilt = cameraPosition.tilt;

                distance = CalcDistance(camera.MyLat,camera.MyLng,camera.Lat,camera.Lng);
                Info7.setText("距離自己:" + String.format("%.0f", distance)+"m");
                if(haveGoal){
                    distance = CalcDistance(camera.Lat,camera.Lng,camera.GoalLat,camera.GoalLng);
                    Info6.setText("距離目標:"+ String.format("%.0f", distance)+"m");
                }
            }
        });
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                camera.Lat = latLng.latitude;
                camera.Lng = latLng.longitude;
                camera.ChangePosition(camera.Lat, camera.Lng, camera.Bearing, camera.Tilt, camera.Zoom);
            }
        });
        btn_FindMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (initLocationProvider()) {
                    whereAmI();
                }
            }
        });
        btn_Set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                Bundle b = new Bundle();
                b.putInt("StableRange",StableRange);
                b.putInt("Speed",Speed);
                b.putInt("MapType", map.getMapType());
                i.putExtras(b);
                i.setClass(GoogleMapView.this, SetMap.class);
                startActivityForResult(i, 111);
            }
        });
        btn_Mod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CameraMode < 2) {
                    CameraMode++;
                } else {
                    CameraMode = 1;
                }
                btn_Mod.setText("模式" + CameraMode);
            }
        });
        btn_Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Stop) {
                    sensorManager.unregisterListener(GoogleMapView.this);
                    ModeTemp = CameraMode;
                    CameraMode = 0;
                    btn_Stop.setBackgroundColor(0xFF00CACA);
                    btn_Stop.setTextColor(0xFF3C3C3C);
                    btn_Mod.setEnabled(false);
                    btn_Zoom.setEnabled(false);
                    btn_Rotate.setEnabled(false);
                    Stop = true;
                }else {
                    isSetCenterAngle = false;
                    SetSensor();
                    CameraMode = ModeTemp;
                    btn_Stop.setBackgroundColor(0xFF3C3C3C);
                    btn_Stop.setTextColor(0xFF00CACA);
                    btn_Mod.setEnabled(true);
                    btn_Zoom.setEnabled(true);
                    btn_Rotate.setEnabled(true);
                    Stop = false;
                }
            }
        });
        btn_Rotate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ModeTemp = CameraMode;
                    CameraMode = 102;
                    btn_Rotate.setBackgroundColor(0xFF00CACA);
                    btn_Rotate.setTextColor(0xFF3C3C3C);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    CameraMode = ModeTemp;
                    btn_Rotate.setBackgroundColor(0xFF3C3C3C);
                    btn_Rotate.setTextColor(0xFF00CACA);
                    isSetCenterAngle = false;
                }
                return false;
            }
        });
        btn_Zoom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ModeTemp = CameraMode;
                    CameraMode = 101;
                    btn_Zoom.setBackgroundColor(0xFF00CACA);
                    btn_Zoom.setTextColor(0xFF3C3C3C);
                    isSetCenterAngle = false;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    CameraMode = ModeTemp;
                    btn_Zoom.setBackgroundColor(0xFF3C3C3C);
                    btn_Zoom.setTextColor(0xFF00CACA);
                    isSetCenterAngle = false;
                }
                return false;
            }
        });
        btn_GoHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!haveGoal) {
                    btn_GoHere.setText("清除目標");
                    btn_GoHere.setBackgroundColor(0xFF00CACA);
                    btn_GoHere.setTextColor(0xFF3C3C3C);
                    camera.GoalLat = camera.Lat;
                    camera.GoalLng = camera.Lng;
                    haveGoal = true;
                    SetMarker();
                } else {
                    Info6.setText("");
                    btn_GoHere.setText("設定目標");
                    btn_GoHere.setBackgroundColor(0xFF3C3C3C);
                    btn_GoHere.setTextColor(0xFF00CACA);
                    haveGoal = false;
                    SetMarker();
                }
            }
        });
    }
/*======================================*/
//setVariable
/*======================================*/
    private void setVariable() {
        acc = new float[3];
        velocity = new float[3];
        CameraMode = 1;
        camera = new Camera();
        StableRange = 10;
        Speed = 5;
        isSetCenterAngle = false;
        haveGoal = false;
        Stop = false;
        distance = 0;
    }
/*======================================*/
//SetSensor
/*======================================*/
    protected void SetSensor() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);//移動約0.0205s    約靜止0.0215s
    }
/*======================================*/
//initLocationProvider
/*======================================*/
    private boolean initLocationProvider() {
        locationMgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (locationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
            return true;
        }
        return false;
    }
/*======================================*/
//whereAmI
/*======================================*/
private void whereAmI() {
    if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        Location location = locationMgr.getLastKnownLocation(provider);
        camera.MyLat = location.getLatitude();
        camera.MyLng = location.getLongitude();
        camera.Lat = camera.MyLat;
        camera.Lng = camera.MyLng;
        camera.ChangePosition(camera.MyLat, camera.MyLng, camera.Bearing, camera.Tilt, camera.Zoom);
        SetMarker();
    }
}
/*======================================*/
//SetMarker
/*======================================*/
    private  void SetMarker() {
        distance = CalcDistance(camera.MyLat,camera.MyLng,camera.GoalLat,camera.GoalLng);
        map.clear();
        camera.ShowMarker(camera.MyLat,camera.MyLng,"您的位置",BitmapDescriptorFactory.HUE_CYAN);
        if(haveGoal) {
            camera.ShowMarker(camera.GoalLat,camera.GoalLng,"距離自己"+ String.format("%.0f", distance)+"m",BitmapDescriptorFactory.HUE_RED);
        }
    }
/*======================================*/
//CalcDistance
/*======================================*/
    private double CalcDistance(double StartLat, double StartLng, double EndLat, double EndLng) {
        double d;
        double d_lat;
        double d_lng;
            d_lat = StartLat-EndLat ;
            d_lng = Math.cos(EndLat*(Math.PI/180)) * (Math.abs(StartLng-EndLng) > 180 ? (StartLat-EndLng > 0 ? StartLng-EndLng-360 : StartLng-EndLng+360) : StartLng-EndLng) ;
            d = Math.sqrt(Math.pow(d_lat,2) + Math.pow(d_lng,2))*111*1000;
        return d;
    }
/****************************************/
//onSensorChanged
/****************************************/
    @Override
    public void onSensorChanged(SensorEvent event) {
        acc = event.values.clone();
    //--------------------<計算加速度在空間中的角度>
        angleXY = Math.atan2(acc[0],acc[1])*(180/3.14159);
        angleYZ = Math.atan2(acc[1],acc[2])*(180/3.14159);
        angleXZ = Math.atan2(acc[0],acc[2])*(180/3.14159);
        if(!isSetCenterAngle) {
            camera.SetCenterAngle(angleXZ, "XZ");
            camera.SetCenterAngle(angleYZ, "YZ");
            camera.SetCenterAngle(angleXY, "XY");
            isSetCenterAngle = true;
        }
    //--------------------<依照選擇模式改變攝影機視角>
        switch (CameraMode) {
            case 0:
                break;
            case 1:
                CameraControllerYZ(1,angleYZ);
                CameraControllerXZ(1, angleXZ);
                break;
            case 2:
                CameraControllerYZ(1, angleYZ);
                CameraControllerXY(2, angleXY);
                break;
            case 101:
                CameraControllerYZ(3,angleYZ);
                CameraControllerXY(2, angleXY);
                break;
            case 102:
                CameraControllerYZ(2,angleYZ);
                CameraControllerXY(2,angleXY);
                break;
            default:
                CameraMode = 1;
                break;
        }
        camera.ChangePosition(camera.Lat, camera.Lng, camera.Bearing, camera.Tilt, camera.Zoom);
        //--------------------<訊息>

    /*   Info1.setText("X:" + String.format("%.3f", camera.Lat));
        Info2.setText("Y:" + String.format("%.3f", camera.Lng)+","+String.format("%.3f", camera.MyLng));
        Info3.setText("Z:" + String.format("%.3f", acc[2]));
        Info4.setText("YZ:"+String.format("%.3f", angleYZ));
        Info5.setText("XY:"+String.format("%.3f", angleXY));
        Info6.setText("距離:"+ String.format("%.0f", Math.sqrt(km1*km1+km2*km2)*111*1000)+"m"); */


    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
/*======================================*/
//CameraControllerYZ0
/*======================================*/
    public void CameraControllerYZ(int mode,double angleYZ) {
        switch (mode) {
            case 0:
                //沒事情發生
                break;
            case 1:
                if (Math.abs(angleYZ-camera.CenterAngleYZ) > StableRange) {
                    float[] MapV = new float[2];
                    velocity[1] = -(float)((angleYZ-camera.CenterAngleYZ)*0.015*Speed);
                    if(camera.Bearing < 90) {
                        MapV[0] = velocity[1]*(float)Math.sin(camera.Bearing * (Math.PI / 180));
                        MapV[1] = velocity[1]*(float)Math.cos(camera.Bearing * (Math.PI / 180));
                    }else if (camera.Bearing < 180) {
                        MapV[0] = velocity[1]*(float)Math.cos((camera.Bearing - 90) * (Math.PI / 180));
                        MapV[1] = -velocity[1]*(float)Math.sin((camera.Bearing - 90) * (Math.PI / 180));
                    }else if (camera.Bearing < 270) {
                        MapV[0] = -velocity[1]*(float)Math.sin((camera.Bearing - 180) * (Math.PI / 180));
                        MapV[1] = -velocity[1]*(float)Math.cos((camera.Bearing - 180) * (Math.PI / 180));
                    }else if (camera.Bearing < 360) {
                        MapV[0] = -velocity[1]*(float)Math.cos((camera.Bearing - 270) * (Math.PI / 180));
                        MapV[1] = velocity[1]*(float)Math.sin((camera.Bearing - 270) * (Math.PI / 180));
                    }
                    camera.ChangeLatLng(MapV[0], MapV[1]);
                }
                break;
            case 2:
                camera.ChangeTilt((float) angleYZ);
                break;
            case 3:
                if(Math.abs(angleYZ-camera.CenterAngleYZ) > StableRange) {
                    camera.ChangeZoom(camera.Zoom - (float) (0.005 * (angleYZ - camera.CenterAngleYZ)));
                }
                break;
            default:
                break;
        }

    }
/*======================================*/
//CameraControllerXY0
/*======================================*/
    public void CameraControllerXY(int mode,double angleXY) {
        switch (mode) {
            case 0:
                //沒事情發生
                break;
            case 1:
                if (Math.abs(angleXY-camera.CenterAngleXY) > StableRange) {
                    float[] MapV = new float[2];
                    velocity[0] = -(float)((angleXY-camera.CenterAngleXY)*0.015*Speed);  //往右傾斜(目標:往右移動)(狀態X為負,Y不變)(角度為負但經度應該要增加,所以加上負號)
                    if(camera.Bearing < 90) {
                        MapV[0] = velocity[0]*(float)Math.cos(camera.Bearing * (Math.PI / 180));
                        MapV[1] = -velocity[0]*(float)Math.sin(camera.Bearing * (Math.PI / 180));
                    }else if (camera.Bearing < 180) {
                        MapV[0] = -velocity[0]*(float)Math.sin((camera.Bearing - 90) * (Math.PI / 180));
                        MapV[1] = -velocity[0]*(float)Math.cos((camera.Bearing - 90) * (Math.PI / 180));
                    }else if (camera.Bearing < 270) {
                        MapV[0] = -velocity[0]*(float)Math.cos((camera.Bearing - 180) * (Math.PI / 180));
                        MapV[1] = velocity[0]*(float)Math.sin((camera.Bearing - 180) * (Math.PI / 180));
                    }else if (camera.Bearing < 360) {
                        MapV[0] = velocity[0]*(float)Math.sin((camera.Bearing - 270) * (Math.PI / 180));
                        MapV[1] = velocity[0]*(float)Math.cos((camera.Bearing - 270) * (Math.PI / 180));
                    }
                    camera.ChangeLatLng(MapV[0], MapV[1]);
                }
                break;
            case 2:
                if (Math.abs(angleXY-camera.CenterAngleXY) > StableRange) {
                    if(velocity[1] >= 0) {
                        camera.ChangeBearing(camera.Bearing - (float) (0.1 * (angleXY-camera.CenterAngleXY)));
                    }else {
                        camera.ChangeBearing(camera.Bearing + (float) (0.1 *(angleXY-camera.CenterAngleXY)));
                    }
                    velocity[1] = 0;
                }
                break;
            default:
                break;
        }
    }
/*======================================*/
//CameraControllerXZ0
/*======================================*/
    public void CameraControllerXZ(int mode,double angleXZ) {
        switch (mode) {
            case 0:
                //沒事情發生
                break;
            case 1:
                if (Math.abs(angleXZ-camera.CenterAngleXZ) > StableRange) {
                    float[] MapV = new float[2];
                    velocity[0] = -mode*(float)((angleXZ-camera.CenterAngleXZ)*0.012*Speed);  //往右傾斜(目標:往右移動)(狀態X為負,Y不變)(角度為負但經度應該要增加,所以加上負號)
                    if(camera.Bearing < 90) {
                        MapV[0] = velocity[0]*(float)Math.cos(camera.Bearing * (Math.PI / 180));
                        MapV[1] = -velocity[0]*(float)Math.sin(camera.Bearing * (Math.PI / 180));
                    }else if (camera.Bearing < 180) {
                        MapV[0] = -velocity[0]*(float)Math.sin((camera.Bearing - 90) * (Math.PI / 180));
                        MapV[1] = -velocity[0]*(float)Math.cos((camera.Bearing - 90) * (Math.PI / 180));
                    }else if (camera.Bearing < 270) {
                        MapV[0] = -velocity[0]*(float)Math.cos((camera.Bearing - 180) * (Math.PI / 180));
                        MapV[1] = velocity[0]*(float)Math.sin((camera.Bearing - 180) * (Math.PI / 180));
                    }else if (camera.Bearing < 360) {
                        MapV[0] = velocity[0]*(float)Math.sin((camera.Bearing - 270) * (Math.PI / 180));
                        MapV[1] = velocity[0]*(float)Math.cos((camera.Bearing - 270) * (Math.PI / 180));
                    }
                    camera.ChangeLatLng(MapV[0], MapV[1]);
                }
                break;
            case 2:
                if (Math.abs(angleXZ-camera.CenterAngleXZ) > StableRange) {
                    camera.ChangeBearing(camera.Bearing - (float) (0.12 * (angleXZ-camera.CenterAngleXZ)));
                }
                break;
            default:
                break;
        }
    }
/*---------------------------------------------------------*/
//Camera
/*---------------------------------------------------------*/
    public class Camera {
        double CenterAngleYZ;
        double CenterAngleXY;
        double CenterAngleXZ;
        double MyLat;
        double MyLng;
        double GoalLat;
        double GoalLng;
        double Lat; //緯度
        double Lng; //經度
        float Zoom; //縮放層級,增加->放大,減少->縮小
        float Bearing;  //視角和北極的角度0~360(順時針)
        float Tilt; //視角傾斜的程度0~67.5度(0為俯視)


        public Camera() {
            Lat = 0.0;
            Lng = 0.0;
            Zoom = 18.0f;
            Bearing = 0.0f;
            Tilt = 0.0f;
        }

        public void ChangeLatLng(float x,float y) {
            Lng = Lng+x*Math.pow(2,(map.getMaxZoomLevel() - Zoom))*0.000003;
            Lat = Lat+y*Math.pow(2,(map.getMaxZoomLevel() - Zoom))*0.000003;
            if(Lat > 90) {
                Lat = 90;
            } else if(Lat < -90) {
                Lat = -90;
            }
            if(Lng > 180) {
                Lng = -180;
            }else if(Lng < -180 ) {
                Lng = 180;
            }
        }
        private void ChangeTilt (float d) {
            if(d > 0) {
                if (d < 30) {   //任何大小都能傾斜30度
                    Tilt = d;
                } else if (Zoom <= 14 && d < 30+(Zoom-10)*3.75) {   //在Zoom <= 14的情況,角度範圍為線性成長(+1比+3.75)
                    Tilt = d;
                } else if (Zoom <= 15.5 && d < 45+(Zoom-14)*8.332) {    //在Zoom <= 15.5的情況,角度範圍為線性成長(+1比+8.332)
                    Tilt = d;
                } else if (d < 67.5){   //在Zoom > 15.5的情況,角度範圍為67.5
                    Tilt = d;
                }
            }
        }
        private void ChangeBearing(float d) {
            while ( d < 0 || 360 < d ) {
                if(d < 0) {
                    d = d + 360;
                }else  if (360 < d) {
                    d = d - 360;
                }
            }
            Bearing = d;
        }
        private void ChangeZoom(float zoom) {
            if (map.getMinZoomLevel() < zoom && zoom < map.getMaxZoomLevel()) {
               Zoom = zoom;
            }
        }
        private void ChangePosition(double Lat, double Lng, float Bearing, float Tilt, float Zoom) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(Lat, Lng))      // Sets the center of the map to Mountain View
                    .bearing(Bearing)                // Sets the orientation of the camera to east
                    .tilt(Tilt)                      // Sets the tilt of the camera to 30 degrees.build();
                    .zoom(Zoom)                   // Sets the zoom
                    .build();                               // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),39,null);
        }
        private void ShowMarker(double Lat, double Lng, String msg, float color) {
            MarkerOptions markerOpt = new MarkerOptions();
            markerOpt.position(new LatLng(Lat, Lng));
            markerOpt.title(msg);
            markerOpt.draggable(false);
            markerOpt.icon(BitmapDescriptorFactory.defaultMarker(color));
            map.addMarker(markerOpt);
        }
        private void SetCenterAngle(double angle, String XYZ) {
            switch (XYZ) {
                case "YZ":
                    CenterAngleYZ = angle;
                    break;
                case "XY":
                    CenterAngleXY = angle;
                    break;
                case "XZ":
                    CenterAngleXZ = angle;
                    break;
                default:
                    break;
            }
        }
    }
/**/
/****************************************/
//沒用東東
/****************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_google_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
