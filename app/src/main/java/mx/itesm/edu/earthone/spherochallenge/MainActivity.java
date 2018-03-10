package mx.itesm.edu.earthone.spherochallenge;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.common.internal.AsyncMessage;
import com.orbotix.common.internal.DeviceResponse;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements RobotChangedStateListener, ResponseListener {

    private Button go, stop, left, right, back;

    //Pedir permiso del sphero
    private final int REQUEST_PERMISSION = 42;    //Pedir permiso
    private float ROBOT_SPEED = 100.0f;   //Velocidad de robot

    private int direction;
    //Objeto para manejar
    private ConvenienceRobot convenienceRobot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        go = (Button) findViewById(R.id.go);
        stop = (Button) findViewById(R.id.stop);


        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convenienceRobot.setLed(0.5f,0.5f,0.0f);
                direction = 180;
                convenienceRobot.drive(direction, ROBOT_SPEED);
            }
        });

        left = (Button) findViewById(R.id.Izquierda);
        right = (Button) findViewById(R.id.Derecha);
        back = (Button) findViewById(R.id.Atras);

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convenienceRobot.setLed(0.5f,0.5f,0.5f);
                direction = 90;
                convenienceRobot.drive(direction, ROBOT_SPEED);
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convenienceRobot.setLed(0.5f,0.5f,0.0f);
                //direction = -90;
                direction = 270;
                convenienceRobot.drive(direction, ROBOT_SPEED);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convenienceRobot.setLed(0.5f,0.5f,0.0f);
                direction = 0;
                convenienceRobot.drive(direction, ROBOT_SPEED);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convenienceRobot.setLed(1.0f,0.0f,0.0f);
                convenienceRobot.stop();
            }
        });

        //Conectar al robot, crea la instancia
        DualStackDiscoveryAgent.getInstance().addRobotStateListener(this);
        //permisos:
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                Log.e("Sphero", "Location permission has not already been granted");
                List<String> permissions = new ArrayList<String>();
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_PERMISSION);
            } else {
                Log.d("Sphero", "Location permission already granted");
            }
        }
    }

    //
    @Override
    public void handleResponse(DeviceResponse deviceResponse, Robot robot) {

    }

    //
    @Override
    public void handleStringResponse(String s, Robot robot) {

    }

    //
    @Override
    public void handleAsyncMessage(AsyncMessage asyncMessage, Robot robot) {

    }

    //
    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType robotChangedStateNotificationType) {
        switch (robotChangedStateNotificationType) {
            case Online:
                convenienceRobot = new ConvenienceRobot(robot);
                convenienceRobot.addResponseListener(this);
                convenienceRobot.enableCollisions(true);
                break;
            default:
                break;
        }
    }

    //Ver si tenemos permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch ( requestCode ) {
            case REQUEST_PERMISSION: {
                for( int i = 0; i < permissions.length; i++ ) {
                    if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                        startDiscovery();
                        Log.d( "Permissions", "Permission Granted: " + permissions[i] );
                    } else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                        Log.d( "Permissions", "Permission Denied: " + permissions[i] );
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    //
    // Empezar a descubrir al sphero
    //
    @Override
    protected void onStart() {
        super.onStart();
        startDiscovery();
    }

    private void startDiscovery() {
        if( !DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            try {
                DualStackDiscoveryAgent.getInstance().startDiscovery( this );
            } catch (DiscoveryException e) {
                Log.e("Sphero", "DiscoveryException: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onStop() {
        if( DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }
        if( convenienceRobot != null ) {
            convenienceRobot.disconnect();
            convenienceRobot = null;
        }

        super.onStop();
    }
}
