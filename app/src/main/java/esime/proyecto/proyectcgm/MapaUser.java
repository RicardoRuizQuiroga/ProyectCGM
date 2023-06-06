package esime.proyecto.proyectcgm;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

public class MapaUser extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String API_KEY = "AIzaSyCWBbI7Qz4alPL4wnc2D7DKMI6CYXEiX8E";

    private GoogleMap googleMap;
    private Marker markerOrigen;
    private Marker markerDestino;
    private Polyline rutaPolyline;
    private Button buttonObtenerRuta;
    private LocationManager locationManager;
    private GeoApiContext geoApiContext;

    private double minLat = 19.299652020491646;
    private double maxLat = 19.35655457105291;
    private double minLng = -99.10735367535725;
    private double maxLng = -99.04449114705703;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_user);

        buttonObtenerRuta = findViewById(R.id.buttonObtenerRuta);
        buttonObtenerRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerRuta();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        geoApiContext = new GeoApiContext.Builder()
                .apiKey(API_KEY)
                .build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        LatLng avenidaTlahuac = new LatLng((maxLat + minLat) / 2, (maxLng + minLng) / 2); // Coordenadas del centro de la Avenida Tláhuac en Ciudad de México
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(avenidaTlahuac, 14f));

        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (isLocationOnAvenidaTlahuac(latLng)) {
                    if (markerDestino == null) {
                        markerDestino = googleMap.addMarker(new MarkerOptions().position(latLng).title("Destino"));
                        buttonObtenerRuta.setEnabled(true);
                    }
                } else {
                    Toast.makeText(MapaUser.this, "Seleccione una ubicación en la Avenida Tláhuac", Toast.LENGTH_SHORT).show();
                }
            }
        });

        enableLocationPermission();
        enableLocationUpdates();
        showTraffic(); // Mostrar tráfico en el mapa
    }

    private boolean isLocationOnAvenidaTlahuac(LatLng latLng) {
        // Verificar si la ubicación está dentro de los límites de la Avenida Tláhuac
        return latLng.latitude >= minLat && latLng.latitude <= maxLat && latLng.longitude >= minLng && latLng.longitude <= maxLng;
    }

    private void enableLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private void enableLocationUpdates() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
        }
    }

    private void obtenerRuta() {
        if (markerDestino == null) {
            Toast.makeText(this, "Debe seleccionar un destino", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng destino = markerDestino.getPosition();

        // Obtener la ubicación del usuario
        Location lastKnownLocation = null;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (lastKnownLocation != null) {
            LatLng origen = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

            // Consultar la ruta utilizando la API de Direcciones de Google Maps
            DirectionsResult result;
            try {
                result = DirectionsApi.newRequest(geoApiContext)
                        .mode(TravelMode.DRIVING)
                        .origin(new com.google.maps.model.LatLng(origen.latitude, origen.longitude))
                        .destination(new com.google.maps.model.LatLng(destino.latitude, destino.longitude))
                        .await();

                // Verificar si se obtuvo una ruta válida
                if (result.routes != null && result.routes.length > 0) {
                    com.google.maps.model.LatLng[] path = result.routes[0].overviewPolyline.decodePath().toArray(new com.google.maps.model.LatLng[0]);

                    // Configurar opciones de la línea de ruta
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .width(10f)
                            .color(Color.BLUE)
                            .startCap(new RoundCap())
                            .endCap(new RoundCap());

                    // Agregar los puntos de la ruta a las opciones de la línea de ruta
                    for (com.google.maps.model.LatLng point : path) {
                        polylineOptions.add(new LatLng(point.lat, point.lng));
                    }

                    // Dibujar la línea de ruta en el mapa
                    if (rutaPolyline != null) {
                        rutaPolyline.remove();
                    }
                    rutaPolyline = googleMap.addPolyline(polylineOptions);

                    // Ajustar la cámara para mostrar toda la ruta
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(origen);
                    builder.include(destino);
                    LatLngBounds bounds = builder.build();
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

                    // Limpiar el marcador de destino
                    markerDestino.remove();
                    markerDestino = null;
                    buttonObtenerRuta.setEnabled(false);
                } else {
                    Toast.makeText(this, "No se encontró una ruta", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTraffic() {
        googleMap.setTrafficEnabled(true);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {}

    @Override
    public void onProviderEnabled(@NonNull String provider) {}

    @Override
    public void onProviderDisabled(@NonNull String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                    enableLocationUpdates();
                }
            }
        }
    }
}
