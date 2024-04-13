package com.example.arduinoconection;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 3;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 2;
    private BluetoothAdapter nBtAdapter;
    private BluetoothSocket btSocket;
    private BluetoothDevice dispositivoSeleccionado;
    private ConnectedThread myConexionBT;
    private ArrayList<String> mNameDevices = new ArrayList<>();
    private ArrayAdapter<String> deviceAdapter;

    Button buscar, conectar, led1on, led2on, led1off, led2off, desconectar;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestBluetoothConnectPermission();
        requestLocationPermission();

        buscar = findViewById(R.id.buscar);
        conectar = findViewById(R.id.conectar);
        led1on = findViewById(R.id.le1on);
        led2on = findViewById(R.id.led2on);
        led1off = findViewById(R.id.led1off);
        led2off = findViewById(R.id.led2off);
        spinner = findViewById(R.id.spinner);
        desconectar = findViewById(R.id.desconectar);

        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mNameDevices);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(deviceAdapter);

        buscar.setOnClickListener(v -> {
            dispositivosVinculados();
            // El codigo que ejecutara el botón
        });

        conectar.setOnClickListener(v -> {
            conectarDispBT();
            // El codigo que ejecutara el botón
        });

        led1on.setOnClickListener(v -> {

            // El codigo que ejecutara el botón

            Toast.makeText(getBaseContext(), "Se presiono el boton de luz 1", Toast.LENGTH_SHORT).show();
        });

        led2on.setOnClickListener(v -> {
            // El codigo que ejecutara el botón
            Toast.makeText(getBaseContext(), "Se presiono el boton de luz 2", Toast.LENGTH_SHORT).show();

        });

        led1off.setOnClickListener(v -> {
            // El codigo que ejecutara el botón



        });

        led2off.setOnClickListener(v -> {
            // El codigo que ejecutara el botón


        });

        desconectar.setOnClickListener(v -> {
            if (btSocket != null) {
                try {
                    btSocket.close();
                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), "La conexión falló", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            finish();
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                dispositivoSeleccionado = getBluetoothDeviceByName(mNameDevices.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                dispositivoSeleccionado = null;
            }
        });
    }

    public void dispositivosVinculados() {
        nBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (nBtAdapter == null) {
            showToast("Bluetooth no disponible en este dispositivo,");
            return;
        }
        if (!nBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Set<BluetoothDevice> pairedDevices = nBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            mNameDevices.clear();
            for (BluetoothDevice device : pairedDevices) {
                mNameDevices.add(device.getName());
            }
            deviceAdapter.notifyDataSetChanged();
        }
    }

    public void conectarDispBT() {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        try {
            btSocket = dispositivoSeleccionado.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            showToast("Fallo al crear el socket");
            finish();
        }
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                showToast("Error al cerrar el socket");
            }
            showToast("Error al conectar el socket");
            finish();
        }
        myConexionBT = new ConnectedThread(btSocket);
        myConexionBT.start();
    }

    private BluetoothDevice getBluetoothDeviceByName(String name) {
        Set<BluetoothDevice> pairedDevices = nBtAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(name)) {
                return device;
            }
        }
        return null;
    }

    public void showToast(String message) {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void requestBluetoothConnectPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
        }
    }

    public void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso para conectar con dispositivos Bluetooth otorgado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso para conectar con dispositivos Bluetooth denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso para acceder a la ubicación otorgado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso para acceder a la ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    // Manejar los datos recibidos
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) { }
        }
    }
}
//package com.example.arduinoconection;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Spinner;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import androidx.activity.result.ActivityResult;
//import androidx.activity.result.ActivityResultCallback;
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.core.app.ActivityCompat;
//import android.Manifest;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.util.Log;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.TextView;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.Set;
//import java.util.UUID;
//
//public class MainActivity extends AppCompatActivity {
//    private static final String TAG = "MainActivity";
//    private static final  UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//    private static final int REQUEST_ENABLE_BT = 1;
//    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 3;
//    private static final int REQUEST_FINE_LOCATION_PERMISSION = 2;
//    private BluetoothAdapter nBtAdapter;
//    private BluetoothSocket btSocket;
//    private BluetoothDevice DispositivoSeleccionado;
//    private ConnectedThread MyConexionBT;
//    private  ArrayList<String> mNameDevices = new ArrayList<>();
//    private ArrayAdapter<String> deviceAdapter;
//
//    Button buscar , conectar , led1on, led2on, led1off, led2off, desconectar;
//    Spinner spinner;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        requestBluetoothConnectPermission();
//        requestLocationPermission();
//
//
//        buscar = findViewById(R.id.buscar);
//        conectar = findViewById(R.id.conectar);
//        led1on = findViewById(R.id.le1on);
//        led2on = findViewById(R.id.led2on);
//        led1off = findViewById(R.id.led1off);
//        led2off = findViewById(R.id.led2off);
//        spinner = findViewById(R.id.spinner);
//        desconectar = findViewById(R.id.desconectar);
//
//        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mNameDevices);
//        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(deviceAdapter);
//
//        buscar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DispositivosVinculados();
//                // El codigo que ejecutara el botón
//
//            }
//        });
//        conectar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ConectarDispBT();
//                // El codigo que ejecutara el botón
//
//            }
//        });
//        led1on.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // El codigo que ejecutara el botón
//                MyConexionBT.write("b");
//                Toast.makeText(getBaseContext(), "Se presiono el boton de luz 1", Toast.LENGTH_SHORT).show();
//
//            }
//        });
//        led2on.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // El codigo que ejecutara el botón
//                Toast.makeText(getBaseContext(), "Se presiono el boton de luz 2", Toast.LENGTH_SHORT).show();
//            }
//        });
//        led1off.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // El codigo que ejecutara el botón
//
//            }
//        });
//        led1off.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // El codigo que ejecutara el botón
//
//            }
//        });
//        led2off.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // El codigo que ejecutara el botón
//
//            }
//        });
//        desconectar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(btSocket!=null){
//                    try {btSocket.close();}
//                    catch (IOException e){
//                        Toast.makeText(getBaseContext(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//                finish();
//
//                // El codigo que ejecutara el botón
//
//            }
//        });
//        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                new ActivityResultCallback<ActivityResult>(){
//                    @Override
//                    public void onActivityResult(ActivityResult result){
//                        if(result.getResultCode() == MainActivity.REQUEST_ENABLE_BT){
//                            Log.d(TAG, "Actividad Registrada");
//                            //Toast.makeText(getBaseContext(), "Actividad Registrada", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//        public void DispositivosVinculados() {
//            nBtAdapter = BluetoothAdapter.getDefaultAdapter();
//            if(nBtAdapter == null){
//                showToast("Bluetooth no disponible en este dispositivo,");
//                finish();
//                return;
//            }
//            if(!nBtAdapter.isEnabled()) {
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_DENIED){
//                    return;
//                }
//                someActivityResultLauncher.launch(enableBtIntent);
//
//            }
//            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
//                    DispositivoSeleccionado = getBluetoothDeviceByName(mNameDevices.get(position));
//                }
//                @Override
//                public void onNothingSelected(AdapterView<?> parent) {
//                    DispositivoSeleccionado = null;
//                }
//            });
//            Set<BluetoothDevice> pairedDevices = nBtAdapter.getBondedDevices();
//            if(pairedDevices.size() > 0) {
//                for (BluetoothDevice device : pairedDevices){
//                    nNameDevices.add(device.getName());
//                }
//                deviceAdapter.notifyDataSetChanged();
//            } else {
//                showToast("No hay dispositivos Bluetooth emparejados.");
//            }
//        }
//        private void requestLocationPermission(){
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
//        }
//        private void requestBluetoothConnectPermission(){
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
//        }
//        @Override
//        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
//            super.onRequestPermissionsResult(requestCode,permissions, grantResults);
//            if(requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION){
//                if(grantResults.length > 0 && grantResults == PackageManager.PERMISSION_GRANTED){
//                    Log.d(TAG, "Permiso aceptado, ahora puedes usar las funciones de BLUETOOTH");
//                } else {
//                    Log.d(TAG, "Permiso denegado, intente de otra manera");
//                }
//            }
//        }
//
//        private BluetoothDevice getBluetoothDeviceByName(String name){
//            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)!= PackageManager.PERMISSION_GRANTED){
//                Log.d(TAG, "No");
//            }
//            Set<BluetoothDevice> pairDevices = nBtAdapter.getBondedDevices();
//            for(BluetoothDevice device : pairDevices){
//                if (device.getName().equals(name)){
//                    return device;
//                }
//            }
//            return null;
//        }
//        private void ConectarDispBT(){
//            if(DispositivoSeleccionado == null){
//                showToast("selecciona uni dispositivo Bluetooth");
//                return;
//            }
//            try {
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    return;
//                }
//                btSocket = DispositivoSeleccionado.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
//                btSocket.connect();
//                MyConexionBT = new ConnectedThread(btSocket);
//                MyConexionBT.start();
//                showToast("Conexión Exitosa");
//
//            } catch (IOException e){
//                showToast("Error al conectar");
//            }
//        }
//        private class ConnectedThread extends Thread {
//            private final OutputStream nnOutStream;
//            ConnectedThread(BluetoothSocket socket){
//                InputStream tmpIn = null;
//                OutputStream tmpOut = null;
//                try {
//                    tmpIn = socket.getInputStream();
//                    tmpOut = socket.getOutputStream();
//                } catch (IOException e){
//                    showToast("Error al crear dlujo de datos");
//                }
//                nnOutStream = tmpOut;
//            }
//            public void write(char input){
//                try {
//                    nnOutStream.write((byte)input);
//                } catch (IOException e){
//                    Toast.makeText(getBaseContext(), "La conexión fallo", Toast.LENGTH_LONG).show();
//                    finish();
//                }
//            }
//        }
//        private void showToast(final String message){
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//
//
//        //EdgeToEdge.enable(this);
//        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            //Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            //v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            //return insets;
//       // });
//    }
//}