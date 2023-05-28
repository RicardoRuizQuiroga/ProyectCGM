package esime.proyecto.proyectcgm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;

import java.util.ArrayList;
import java.util.List;

public class MapaUser extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap googleMap;
    private Marker markerOrigen;
    private Marker markerDestino;
    private Polyline rutaPolyline;
    private Button buttonObtenerRuta;
    private Button buttonCancelarRuta;
    private List<Polyline> polylinesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_user);

        buttonObtenerRuta = findViewById(R.id.buttonObtenerRuta);
        buttonCancelarRuta = findViewById(R.id.buttonCancelarRuta);

        buttonObtenerRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerRuta();
            }
        });

        buttonCancelarRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelarRuta();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        LatLng avenidaTlahuac = new LatLng(19.295454, -99.035912); // Coordenadas de Avenida Tláhuac en Ciudad de México
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(avenidaTlahuac, 14f));

        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (markerOrigen == null) {
                    markerOrigen = googleMap.addMarker(new MarkerOptions().position(latLng).title("Origen"));
                } else if (markerDestino == null) {
                    markerDestino = googleMap.addMarker(new MarkerOptions().position(latLng).title("Destino"));
                    buttonObtenerRuta.setEnabled(true);
                }
            }
        });

        enableLocationPermission();
        enableTrafficOverlay();

        polylinesList = new ArrayList<>();
    }

    private void enableLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private void enableTrafficOverlay() {
        // Código para habilitar la capa de tráfico en el mapa
    }

    private void obtenerRuta() {
        if (markerOrigen == null || markerDestino == null) {
            Toast.makeText(this, "Debe seleccionar origen y destino", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng origen = markerOrigen.getPosition();
        LatLng destino = markerDestino.getPosition();

        // Crear solicitud de dirección utilizando la API de Google Maps
        DirectionsApiRequest request = DirectionsApi.newRequest(getGeoApiContext())
                .mode(TravelMode.DRIVING)
                .origin(new com.google.maps.model.LatLng(origen.latitude, origen.longitude))
                .destination(new com.google.maps.model.LatLng(destino.latitude, destino.longitude))
                .units(Unit.METRIC);

        // Realizar la solicitud de dirección de forma asincrónica
        request.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                if (result != null && result.routes.length > 0) {
                    DirectionsRoute ruta = result.routes[0];

                    // Obtener los puntos de referencia de la ruta
                    List<LatLng> puntosReferencia = obtenerPuntosReferencia(ruta);

                    // Configurar opciones de la línea de ruta
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .width(10f)
                            .color(Color.BLUE)
                            .startCap(new RoundCap())
                            .endCap(new RoundCap());

                    // Agregar los puntos de referencia a las opciones de la línea de ruta
                    polylineOptions.addAll(puntosReferencia);

                    // Dibujar la línea de ruta en el mapa
                    rutaPolyline = googleMap.addPolyline(polylineOptions);
                    polylinesList.add(rutaPolyline);

                    // Ajustar la cámara para mostrar toda la ruta
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(origen);
                    builder.include(destino);
                    LatLngBounds bounds = builder.build();
                    int padding = 100; // Margen en píxeles alrededor de la ruta
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                    buttonObtenerRuta.setEnabled(false);
                    buttonCancelarRuta.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                Toast.makeText(MapaUser.this, "Error al obtener la ruta", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<LatLng> obtenerPuntosReferencia(DirectionsRoute ruta) {
        List<LatLng> puntosReferencia = new ArrayList<>();
        if (ruta != null && ruta.legs != null && ruta.legs.length > 0 &&
                ruta.legs[0].steps != null && ruta.legs[0].steps.length > 0) {
            for (DirectionsStep step : ruta.legs[0].steps) {
                com.google.maps.model.LatLng startLatLng = step.startLocation;
                puntosReferencia.add(new LatLng(startLatLng.lat, startLatLng.lng));

                com.google.maps.model.LatLng endLatLng = step.endLocation;
                puntosReferencia.add(new LatLng(endLatLng.lat, endLatLng.lng));
            }
        }
        return puntosReferencia;
    }

    private GeoApiContext getGeoApiContext() {
        // Reemplaza "YOUR_API_KEY" con tu propia clave de API de Google Maps
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyCWBbI7Qz4alPL4wnc2D7DKMI6CYXEiX8E")
                .build();
        return context;
    }

    private void cancelarRuta() {
        if (rutaPolyline != null) {
            rutaPolyline.remove();
            rutaPolyline = null;
        }

        for (Polyline polyline : polylinesList) {
            polyline.remove();
        }
        polylinesList.clear();

        buttonObtenerRuta.setEnabled(true);
        buttonCancelarRuta.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationPermission();
            }
        }
    }
}
