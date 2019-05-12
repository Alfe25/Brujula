package es.upm.dit.adsw.compass;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Ejercicio4 ADSW
 * @version 11/05/2019
 * @author Mateo
 * @author Daniel
 * @author Andrés
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magneticField;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {

        super.onResume();
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if(accelerometer!= null) {
            mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }

        if(magneticField!= null) {
            mSensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     *  Retirar el registro cuando la aplicaciónno no está visible
     *  Evita el consumo innecesario de bateria y otros recursos
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Retirar el registro cuando la aplicaciónno no está visible
        mSensorManager.unregisterListener(this,accelerometer);
        mSensorManager.unregisterListener(this, magneticField);
        //Evita el consumo innecesario de bateria
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
        }
    }

    /**
     *Nos da la orientacion en grados
     */
    public void posicionGrados(View view){

        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        double radianes = orientationAngles[0] + Math.PI;
        int grados = ((int) Math.toDegrees(radianes));

        TextView textoGrados = (TextView) findViewById(R.id.grados);
        String gradosString = Integer.toString(grados);
        textoGrados.setText( gradosString + "º");


    }

    /**
     * Nos da el punto cardinal mas cercano
     */
    public void puntoCardinal(View view){

        TextView textoGrados = (TextView) findViewById(R.id.grados);
        String gradosString = textoGrados.getText().toString();
        int grados = Integer.parseInt(gradosString);

        TextView puntoCardinal = (TextView) findViewById(R.id.pCardinal);

        if(grados <= 23 || grados >= 337){
            //SUR
            puntoCardinal.setText("SUR");
        }else if(grados > 23 && grados < 68){
            //Suroeste
            puntoCardinal.setText("Suroeste");
        }else if(grados >= 68 && grados <= 113){
            //OESTE
            puntoCardinal.setText("OESTE");
        }else if(grados > 113 && grados < 158){
            //Noroeste
            puntoCardinal.setText("Noroeste");
        }else if(grados >= 158 && grados <= 203){
            //NORTE
            puntoCardinal.setText("NORTE");
        }else if(grados > 203 && grados < 248){
            //Noreste
            puntoCardinal.setText("Noreste");
        }else if(grados >= 248 && grados <= 293){
            //ESTE
            puntoCardinal.setText("ESTE");
        }else if(grados > 293 && grados < 337){
            //Sureste
            puntoCardinal.setText("Sureste");
        }
    }


    /**
     * Imprime el texto escrito por el usuario: "Estoy en" + ubicacion
     * @param view
     */
    public void imprimirTexto(View view) {

        EditText et = (EditText) findViewById(R.id.wText);
        String ubicacion = et.getText().toString();

        TextView tw = (TextView) findViewById(R.id.grados);
        String grados = tw.getText().toString();

        TextView texto = (TextView) findViewById(R.id.rText);

        if (ubicacion.isEmpty()) {
            String error1 = "No hay ubicacion";
            Toast.makeText(this, error1, Toast.LENGTH_SHORT).show();    //mensaje de "error"
            texto.setText(error1);
        } else {
            texto.setText("Estoy en " + ubicacion + " con una orientacion de " + grados + " grados");
        }
    }


    /**
     * Comparte la ubicacion con apps del dispositivo
     *
     * @param view
     */
    public void compartirUbicacion(View view) {

        Intent intent = new Intent(Intent.ACTION_SEND);

        TextView tw = (TextView) findViewById(R.id.rText);
        String texto = tw.getText().toString();
        intent.putExtra(Intent.EXTRA_TEXT, texto);

        EditText et = (EditText) findViewById(R.id.wText);
        String ubicacion = et.getText().toString();

        if (ubicacion.isEmpty()) {
            String error1 = "No hay ubicacion";
            Toast.makeText(this, error1, Toast.LENGTH_SHORT).show();    //mensaje de "error"
            return;
        }

        intent.setType("test/plain");
        startActivity(intent);

    }

    /**
     * Abre la app de Maps con la ubicacion escrita
     * @param view Si no hay ninguna ubicacion da un Toast
     *             Si no encuentra la app de Maps en el dispositivo, lanza un Toast
     */
    public void maps(View view) {

        EditText editText = (EditText) findViewById(R.id.wText);
        String ubicacion = editText.getText().toString();

        if (ubicacion.isEmpty()) {
            String error1 = "No hay ubicacion";
            Toast.makeText(this, error1, Toast.LENGTH_SHORT).show();        //mensaje de "error"
            return;
        } else {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + ubicacion);
            Intent mapaIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapaIntent.setPackage("com.google.android.apps.maps");

            if (mapaIntent.resolveActivity(getPackageManager()) == null) {
                String error2 = "No se encuentra app";
                Toast.makeText(this, error2, Toast.LENGTH_SHORT).show();    //mensaje de "error"
                return;
            } else {
                startActivity(mapaIntent);
            }

        }
    }
}
