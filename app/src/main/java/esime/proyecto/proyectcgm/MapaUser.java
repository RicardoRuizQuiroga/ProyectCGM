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
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;


public class MapaUser extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap googleMap;
    private Marker markerOrigen;
    private Marker markerDestino;
    private Polyline rutaPolyline;
    private Button buttonObtenerRuta;

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
    }

    private void enableLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private void enableTrafficOverlay() {
        TileProvider trafficTileProvider = new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                String baseUrl = "https://mt1.google.com/vt/lyrs=m@221097413,traffic&x=" + x + "&y=" + y + "&z=" + zoom;
                try {
                    return new URL(baseUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        TileOverlay trafficOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(trafficTileProvider));
        trafficOverlay.setTransparency(0.5f);
    }

    private void obtenerRuta() {
        if (markerOrigen == null || markerDestino == null) {
            Toast.makeText(this, "Debe seleccionar origen y destino", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng origen = markerOrigen.getPosition();
        LatLng destino = markerDestino.getPosition();

        // Configurar opciones de la línea de ruta
        PolylineOptions polylineOptions = new PolylineOptions()
                .width(10f)
                .color(Color.BLUE)
                .startCap(new RoundCap())
                .endCap(new RoundCap());

        // Agregar los puntos de origen y destino a las opciones de la línea de ruta
        polylineOptions.add(origen, destino);

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
        int padding = 100; // Margen en píxeles alrededor de la ruta
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
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