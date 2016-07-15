package mn.hewertho.moodlamp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    public static int ENABLE_BLUETOOTH = 1;
    public static int SELECT_PAIRED_DEVICE = 2;
    public static int SELECT_DISCOVERED_DEVICE = 3;

    static TextView selectedDevice;
    static TextView statusMessage;
    static TextView labelRedValue;

    static SeekBar seekBarRed;

    ConnectionThread connect;
    static boolean isConnected = false;

    String redValue = "0";

    String formatData = "<$1;$2,$3;$4;$5>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedDevice = (TextView) findViewById(R.id.selectedDevice);
        statusMessage = (TextView) findViewById(R.id.statusMessage);

        seekBarRed = (SeekBar) findViewById(R.id.seekBarRed);
        labelRedValue = (TextView) findViewById(R.id.labelRedValue);

        seekBarRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                redValue = new Integer(progress).toString();
                labelRedValue.setText(progress + " / " + seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                labelRedValue.setText(progress + " / " + seekBar.getMax());
            }
        });

        enableBluetooth();
    }

    public void enableBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            statusMessage.setText("No bluetooth");
        } else {
            statusMessage.setText("Bluetooth working");
        }

        if (bluetoothAdapter.isEnabled()) {
            statusMessage.setText("Bluetooth enabled");
        } else {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, ENABLE_BLUETOOTH);

            statusMessage.setText("Request enable bluetooth...");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                statusMessage.setText("Bluetooth enabled");
            } else {
                statusMessage.setText("Bluetooth disabled");
            }
        } else if (requestCode == SELECT_PAIRED_DEVICE || requestCode == SELECT_DISCOVERED_DEVICE) {
            if (resultCode == RESULT_OK) {
                selectedDevice.setText(data.getStringExtra("btDevName") +  ":" + data.getStringExtra("btDevAddress"));

                statusMessage.setText("Device selected.");

                connect = new ConnectionThread(data.getStringExtra("btDevAddress"));
                connect.start();
            } else {
                statusMessage.setText("None device selected");
            }
        }
    }

    public void searchPairedDevices(View view) {

        Intent searchPairedeDevicesIntent = new Intent(this, PairedDevices.class);
        startActivityForResult(searchPairedeDevicesIntent, SELECT_PAIRED_DEVICE);
    }

    public void discoverDevices(View view) {

        Intent searchPairedDevicesIntent = new Intent(this, DiscoveredDevices.class);
        startActivityForResult(searchPairedDevicesIntent, SELECT_DISCOVERED_DEVICE);
    }

    public void enableVisibility(View view) {

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
        startActivity(discoverableIntent);
    }

    public void waitConnection(View view) {

        connect = new ConnectionThread();
        connect.start();
    }

    public void sendMessage(View view) {
        String messageData = formatData.replace("$1", redValue);
        messageData = messageData.replace("$2", "233");
        messageData = messageData.replace("$3", "222");
        messageData = messageData.replace("$4", "1");
        messageData = messageData.replace("$5", "400");

        byte[] data =  messageData.getBytes();

        connect.write(data);
    }

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            String dataString= new String(data);

            if(dataString.equals("---N")) {
                statusMessage.setText("Ocorreu um erro durante a conexão D:");
                isConnected = false;
            } else if(dataString.equals("---S")) {
                statusMessage.setText("Conectado :D");
                isConnected = true;
            } else {
                // statusMessage.setText(new String(data));
            }

        }
    };
}
