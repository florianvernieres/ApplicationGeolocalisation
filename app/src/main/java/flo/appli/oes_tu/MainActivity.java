/**
 * Application de géolocalisation
 * @author Florian VERNIERES
 * @version 1.0
 */
package flo.appli.oes_tu;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MainActivity extends AppCompatActivity {
    /** editText qui récupère le numéro de la personne à qui on envoi les sms */
    public static EditText mEditText;
    /** ListView comprenant l'ensemble des contacts du téléphone */
    private ListView listView;

    /** custom Adapter pour la mise en forme du répertoire */
    private CustomAdapter customAdapter;

    /** ArrayList comprenant tous les contacts */
    private ArrayList<ContactModel> contactModelArrayList;

    /** Bouton permettant d'afficher et de cacher le répertoire */
    private Button mContact;

    /** Boolean permettant de cacher ou non le répertoire */
    private boolean visible = true;

    /** Nom de la personne du répertoire */
    private String name;

    /** Numéro d'une personne */
    private String phoneNumber;

    /** Bouton de mise en route de l'application */
    private Button start;

    /** Value permettant de définir le type d'action sur le bouton start: 0 = demarrer, 1 = stop */
    public static int value = 0;

    /** EditText rappelant à l'utilisateur d'allumer son GPS */
    private TextView informations;

    /** Liste déroulante comprenant les intervalles de temps */
    private Spinner temps;

    /** Temps choisi par l'utilisateur entre les messages*/
    public static int tempsChoisi;

    /** Texte minutes */
    private TextView minutes;

    /** Texte sélection temps*/
    private TextView message;

    PowerManager.WakeLock wl;


    /**
     * Méthode OnCreate
     * @param savedInstanceState
     */
    @SuppressLint("WakelockTimeout")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());

        Intent it = new Intent(getApplicationContext(), MyAndroidService.class);
        it.putExtra("Key", "Value");
        startService(it);

        listView = (ListView) findViewById(R.id.listView);
        mContact = findViewById(R.id.btnContact);
        mEditText = findViewById(R.id.editText);
        start = findViewById(R.id.Start_Service);
        informations = findViewById(R.id.infos);
        temps = findViewById(R.id.tempsMessage);
        minutes = findViewById(R.id.minutes);
        message = findViewById(R.id.message);



        if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
            //Demande de permission au premier démarrage de l'appli
        }else {
            //Demande une unique fois de donner la permission
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.SEND_SMS)
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED){
                String[] permissions = {Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_BACKGROUND_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_CONTACTS,Manifest.permission.WAKE_LOCK};
                //Afficher la demande de permission
                ActivityCompat.requestPermissions(MainActivity.this,permissions,2);
            } else {
                //afficher un message précisant que la permission est obligatoire
                Toast.makeText(MainActivity.this,"Permissions obligatoires",Toast.LENGTH_SHORT).show();
            }
        }

        final List<Integer> tempsMessage = new ArrayList<Integer>();
        tempsMessage.add(1);
        tempsMessage.add(10);
        tempsMessage.add(15);
        tempsMessage.add(20);
        tempsMessage.add(25);
        tempsMessage.add(30);

        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, tempsMessage);
        //Le layout par défaut est android.R.layout.simple_spinner_dropdown_item
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        temps.setAdapter(adapter);

       temps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               Toast.makeText(MainActivity.this,"Temps entre les messages: " + tempsMessage.get(position).toString() + " minutes",Toast.LENGTH_SHORT).show();
               tempsChoisi = tempsMessage.get(position);
           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {

           }
       });

        contactModelArrayList = new ArrayList<>();
        final Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        while (phones.moveToNext()) {
            name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            ContactModel contactModel = new ContactModel();
            contactModel.setNumber(phoneNumber);
            contactModel.setName(name);
            contactModelArrayList.add(contactModel);
            Log.d("name>>", name + "  " + phoneNumber);

        }
        phones.close();

        customAdapter = new CustomAdapter(MainActivity.this, contactModelArrayList);
        listView.setAdapter(customAdapter);
        listView.setVisibility(View.INVISIBLE);

        mContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visible) {
                    listView.setVisibility(View.VISIBLE);
                    mContact.setText("Fermer contact");
                    mEditText.setVisibility(View.INVISIBLE);
                    start.setVisibility(View.INVISIBLE);
                    informations.setVisibility(View.INVISIBLE);
                    message.setVisibility(View.INVISIBLE);
                    temps.setVisibility(View.INVISIBLE);
                    minutes.setVisibility(View.INVISIBLE);
                    visible = false;

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Toast.makeText(MainActivity.this, contactModelArrayList.get(position).getName() + " a été sélectionné", Toast.LENGTH_SHORT).show();
                            mEditText.setText(contactModelArrayList.get(position).getNumber());
                        }
                    });

                } else {
                    listView.setVisibility(View.INVISIBLE);
                    mContact.setText("Afficher contact");
                    mEditText.setVisibility(View.VISIBLE);
                    start.setVisibility(View.VISIBLE);
                    informations.setVisibility(View.VISIBLE);
                    message.setVisibility(View.VISIBLE);
                    temps.setVisibility(View.VISIBLE);
                    minutes.setVisibility(View.VISIBLE);
                    visible = true;
                }
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (value == 0){
                    startService(new Intent(MainActivity.this, MyAndroidService.class));
                    start.setText("Stop");
                    value = 1;

                } else if(value == 1) {
                    stopService(new Intent(MainActivity.this, MyAndroidService.class));
                    System.exit(0);
                }
            }
        });

    }

    protected void onStart() // sous eclipse sert toi de Source->Override/Implémentation
    {
        super.onStart();
        if (wl != null) {
            wl.acquire();
        }
    }

    protected void onDestroy()
    {
        super.onDestroy();
        if (wl != null) {
            wl.release();
        }
    }


}
