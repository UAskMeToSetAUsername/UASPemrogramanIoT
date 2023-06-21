package com.example.uaspemrogramaniot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MainActivity extends AppCompatActivity {

    private Button btnSuhu;
    private Button btnKecepatan;
    private Button btnPenumpang;
    private TextView tvSuhu;
    private TextView tvKecepatan;
    private TextView tvJumlahPenumpang;

    private MqttClient client;
    private String broker = "tcp://192.168.41.73:1883"; // Ganti dengan alamat broker MQTT yang sesuai
    private String clientId = MqttClient.generateClientId();
    private MemoryPersistence persistence = new MemoryPersistence();

    private String topicSuhu = "4173/dht"; // Ganti dengan topik yang digunakan untuk suhu
    private String topicKecepatan = "4173/potensio"; // Ganti dengan topik yang digunakan untuk kecepatan
    private String topicPenumpang = "4173/dummy"; // Ganti dengan topik yang digunakan untuk jumlah penumpang


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSuhu = findViewById(R.id.btn_suhu);
        btnKecepatan = findViewById(R.id.btn_kecepatan);
        btnPenumpang = findViewById(R.id.btn_jmlh);
        tvSuhu = findViewById(R.id.tv_suhu);
        tvKecepatan = findViewById(R.id.tv_kecepatan);
        tvJumlahPenumpang = findViewById(R.id.tv_jmlh);

        // Menghubungkan ke broker MQTT
        try {
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        // Mengatur listener tombol Suhu
        btnSuhu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeToTopic(topicSuhu);
            }
        });

        // Mengatur listener tombol Kecepatan
        btnKecepatan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeToTopic(topicKecepatan);

            }
        });
        // Mengatur listener tombol Penumpang
        btnPenumpang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeToTopic(topicPenumpang);
            }
        });

        // Mengatur callback untuk menerima pesan MQTT
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                // Reconnect atau lakukan tindakan yang sesuai jika koneksi hilang
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // Menghandle pesan yang diterima sesuai dengan topiknya
                String data = new String(message.getPayload());

                if (topic.equals(topicSuhu)) {
                    // Menampilkan data suhu di TextView
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvSuhu.setText(data);
                        }
                    });
                } else if (topic.equals(topicKecepatan)) {
                    // Menampilkan data kecepatan di TextView
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvKecepatan.setText(data);
                        }
                    });
                } else if (topic.equals(topicPenumpang)) {
                    // Menampilkan data jumlah penumpang di TextView
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvJumlahPenumpang.setText(data);
                        }
                    });
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Tindakan yang diambil setelah pesan terkirim (jika diperlukan)
            }
        });
    }

    private void publishData(String topic, String data) {
        try {
            MqttTopic mqttTopic = client.getTopic(topic);
            MqttMessage message = new MqttMessage(data.getBytes());
            mqttTopic.publish(message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribeToTopic(String topic) {
        try {
            client.subscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            client.disconnect();
            client.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}

//Pastikan untuk mengganti `<alamat_broker>`, `<topic_suhu>`, `<topic_kecepatan>`, dan `<topic_penumpang>` sesuai dengan konfigurasi dan topik yang Anda gunakan.
//Kode di atas akan melakukan koneksi ke broker MQTT saat aplikasi dimulai. Kemudian, saat tombol-tombol ditekan, fungsi `publishData()` akan mem-publish data ke topik yang sesuai. Fungsi `subscribeToTopic()` akan melakukan subscribe ke topik yang diinginkan. Jika ada pesan yang diterima di topik yang di-subscribe, fungsi `messageArrived()` akan dijalankan dan data akan ditampilkan di TextView yang sesuai.
//Pastikan Anda juga telah menambahkan library Paho MQTT Android ke proyek Android Studio Anda agar kode dapat berfungsi dengan baik.
