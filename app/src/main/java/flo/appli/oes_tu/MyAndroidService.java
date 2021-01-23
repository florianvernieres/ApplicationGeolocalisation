/**
 * Application de géolocalisation
 * @author Florian VERNIERES
 * @version 1.0
 */
package flo.appli.oes_tu;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.SmsManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

/**
 *
 */
public class MyAndroidService extends IntentService implements LocationListener {

    /** Constante permettant de définir la durée total du timer */
    //private static final long TEMPS_DEBUT_CHRONO = tempsInterval; // 6secs  = 6000 / 20 minutes = 1200000 / 1 minute = 60 000

    /** Objet de type CountDownTimer permet de créer un timer */
    private CountDownTimer mCountDownTimer;

    /** Temsp restant sur le timer */
    private long tempsRestant;

    /** Adresse à laquelle je me trouve */
    private String monAdresse;

    /** Acienne adresse par laquelle je suis passée */
    private String ancienneAdresse="";

    /** message à envoyer */
    private String message;

    /** Latitude et longitude actuelle */
    private double latitude,longitude;

    /**  Ancienne longitude*/
    private double ancienneLongitude = 0.000;

    /** ancienne Latitude */
    private double ancienneLatitude = 0.000;

    /** EditText comprenant le numero de la personne a qui on envoi les sms */
    public EditText telephone;



    public MyAndroidService() {
        super("MyAndroidService");

    }

    public void onCreate(){
        super.onCreate();
    }


    /**
     *  Méthode qui se déclenche lors de l'appel d'un service
     * @param intent intent de départ
     * @param flags drapeaux
     * @param startId id de départ
     * @return START STICKY ndique au système d'exploitation de recréer le service après avoir
     * suffisamment de mémoire et d'appeler onStartCommand() encore avec une Intent nulle.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location mLocation;
            assert locationManager != null;

            if (locationManager.isProviderEnabled(GPS_PROVIDER)) {

                if (ActivityCompat.checkSelfPermission(MyAndroidService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
                }
                locationManager.requestLocationUpdates(GPS_PROVIDER, 5000, 10, this);
                mLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
                if (mLocation == null){
                    Toast.makeText(MyAndroidService.this, "Impossible de récupérer vos coordonnées, Activez la fonction 'localisation' ", Toast.LENGTH_LONG).show();
                    resetTimer();
                } else {
                   debut();
                }

            } else if (locationManager.isProviderEnabled(NETWORK_PROVIDER)) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                        }

                    }//Permissions refusées
                    locationManager.requestLocationUpdates(NETWORK_PROVIDER, 5000, 10, this);
                    mLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
                    if (mLocation == null){
                        Toast.makeText(MyAndroidService.this, "Impossible de récupérer vos coordonnées, Activez la fonction 'localisation' ", Toast.LENGTH_LONG).show();
                        resetTimer();
                    } else {
                      debut();
                    }
                }

            } else {
                Toast.makeText(MyAndroidService.this, "Impossible de récupérer vos coordonnées, Activez la fonction 'localisation' ", Toast.LENGTH_LONG).show();
                latitude = 0.0;
                longitude = 0.0;
            }

        }
    }



    /**
     * methode permettant de démarrer l'envoi de SMS
     * Affiche un message pour signifier la mis en route de l'appli
     * déclenche le timer
     */
    public void debut() {
        Toast.makeText(this, "C'est parti ! Début d'envoi des SMS", Toast.LENGTH_SHORT).show();
        startTimer();
    }



    /**
     * Méthode permettant de déclencher le timer
     */
    public void startTimer() {
        tempsRestant = MainActivity.tempsChoisi * 60000;
        mCountDownTimer = new CountDownTimer(tempsRestant, 1000) { // param 1 = temps restant, param2 = vitesse d'écoulement du temps en ms
            @Override
            public void onTick(long millisUntilFinished) {
                tempsRestant = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
            }
        }.start();
    }

    public void locManager(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location mLocation;
        assert locationManager != null;
        try {
            if (locationManager.isProviderEnabled(GPS_PROVIDER)) {

                if (ActivityCompat.checkSelfPermission(MyAndroidService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
                }
                locationManager.requestLocationUpdates(GPS_PROVIDER, 5000, 10, this);
                mLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);

                if (mLocation == null){
                    resetTimer();
                } else {
                    latitude = mLocation.getLatitude();
                    longitude = mLocation.getLongitude();
                }

            } else if (locationManager.isProviderEnabled(NETWORK_PROVIDER)) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                        }
                    }//Permissions refusées
                    locationManager.requestLocationUpdates(NETWORK_PROVIDER, 5000, 10, this);
                    mLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
                    if (mLocation == null){
                        resetTimer();
                    } else {
                        latitude = mLocation.getLatitude();
                        longitude = mLocation.getLongitude();
                    }
                }

            } else {
                Toast.makeText(MyAndroidService.this, "Impossible de récupérer vos coordonnées, Activez la fonction 'localisation' ", Toast.LENGTH_LONG).show();
                latitude = 0.0;
                longitude = 0.0;
            }

        } catch (Exception e) {
           resetTimer();
        }

    }



    /**
     * Methode permettant de reset le timer à lieu automatiquement quand celui-ci arrive à 0
     * tempsRestant redeviens = temps total
     * appel de la fonction updateCountDownText pour remettre à jour l'affichage
     * relance le timer
     */
    private void resetTimer() {
        tempsRestant = MainActivity.tempsChoisi * 60000;
        updateCountDownText();
        startTimer();
    }


    /**
     * méthode "four tout" contient;
     * l'affichage du timer
     * les autorisations pour: sms, gps, localisation
     * le système de localisation
     * le systèmme d'envoi d'sms
     */
    public void updateCountDownText() {
        int minutes = (int) (tempsRestant / 1000) / 60; //Calcul du nbr de minutes
        int secondes = (int) (tempsRestant / 1000) % 60; //

        //Reset du timer + envoi sms + ouvre le gps -> permet de gagner de la batterie
        if (minutes == 0 && secondes == 0) {
            telephone = MainActivity.mEditText; //Récupère le numéro de téléphone

            locManager();

            if(Geocoder.isPresent()) {      //Création d'un géocoder si celui-ci est présent
                Geocoder geocoder = new Geocoder(MyAndroidService.this, Locale.getDefault()); //permet de transformer une adresse long/lat en adresse postale
                List<Address> addresses = null;

                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
                // On crée une adresse postale si celle-ci n'est pas null (s'il y a internet)
                if(addresses != null && addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    monAdresse = address;  //l'adresse de ma dernière position

                    // Il y a un problème je n'est pas bougé
                    if (monAdresse.equals(ancienneAdresse) || (latitude == ancienneLatitude && longitude == ancienneLongitude)) {
                        message = "J'ai un problème je suis  à: ";
                    } else { // Mon adresse a changé
                        message = "je vais bien je suis à ";
                    }

                    if (isOnline()) { //Si j'ai de a connection
                        SmsManager.getDefault().sendTextMessage(telephone.getText().toString(), null, message + " " + monAdresse, null, null);
                        ancienneAdresse = monAdresse; //La position actuelle devient ancienne
                        ancienneLatitude = latitude;
                        ancienneLongitude = longitude;
                    }else{
                      resetTimer();
                    }
                }else if (longitude == 0.0 && latitude == 0.0) { //Sans internet ni gps
                    //Toast.makeText(this,"Zone sans réseau",Toast.LENGTH_SHORT).show();
                    //SmsManager.getDefault().sendTextMessage(telephone.getText().toString(), null, "Zone sans réseau " , null, null);

                } else { //Zone sans internet
                    if ((longitude == ancienneLongitude && latitude == ancienneLatitude) || monAdresse.equals(ancienneAdresse)){ //J'ai un problème
                        message = "J'ai un problème, je suis à: ";
                    }else { //Je n'ai pas de problèmes
                        message = "Je vais bien, je suis à: ";
                    }
                    if (isOnline()) {
                        SmsManager.getDefault().sendTextMessage(telephone.getText().toString(), null, message + "Latitude: " + latitude + " / longitude: " + longitude, null, null);
                        ancienneLatitude = latitude;
                        ancienneLongitude = longitude;
                    } else{
                        resetTimer();//On reboucle
                    }
                }
                resetTimer();//On reboucle
                monAdresse = "" ;// Passage dans une zone sans gps ou réseau pour reset le timer
                latitude = 0.0;
                longitude = 0.0;



            } else { //Zone sans je sais pas quoi car j'arrive pas à entrer dans le else mais dans le doute on recommence
               resetTimer();
            }
        }
    }

    public boolean isOnline () {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }




    @Override
    public void onLocationChanged(Location location) {
        locManager();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}