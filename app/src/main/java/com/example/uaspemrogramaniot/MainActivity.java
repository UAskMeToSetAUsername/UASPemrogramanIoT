package com.example.uaspemrogramaniot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private String broker = "tcp://192.168.41.73:1883";
    private String clientId = MqttClient.generateClientId();
    private MemoryPersistence persistence = new MemoryPersistence();

    private String topicSuhu = "4173/dht";
    private String topicKecepatan = "4173/potensio";
    private String topicPenumpang = "4173/dummy";


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

        try {
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        btnSuhu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeToTopic(topicSuhu);
            }
        });

        btnKecepatan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeToTopic(topicKecepatan);

            }
        });
        btnPenumpang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeToTopic(topicPenumpang);
            }
        });

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String data = new String(message.getPayload());

                if (topic.equals(topicSuhu)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvSuhu.setText(data);
                        }
                    });
                } else if (topic.equals(topicKecepatan)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvKecepatan.setText(data);
                        }
                    });
                } else if (topic.equals(topicPenumpang)) {
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
