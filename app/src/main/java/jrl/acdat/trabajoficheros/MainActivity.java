package jrl.acdat.trabajoficheros;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    public static final String URLIMAGENES = "http://alumno.mobi/~alumno/superior/roldan/imagenes.txt";
    public static final String URLFRASES = "http://alumno.mobi/~alumno/superior/roldan/frases.txt";
    public static final String URLERRORES = "http://alumno.mobi/~alumno/superior/roldan/errores.php";

    public static final String FICHERORAW = "intervalo";
    public static final String EXTENSIONFICHERORAW = ".txt";
    public static final String FICHEROENLACES = "enlaces.txt";
    public static final String FICHEROFRASES = "frases.txt";

    public static final String UTF8 = "utf-8";
    public static final int SEGUNDO = 1000;
    public static final int TIEMPO = 5 * SEGUNDO;

    EditText edtImagenes, edtFrases;
    Button btnDescargar;
    ImageView imgvDescarga;
    TextView txvDescarga;
    Memoria memoria;
    StringBuilder frases;
    CountDownTimer cdtImagenes;
    CountDownTimer cdtFrases;
    long intervalo = 1;
    int posicionImagen = 0;
    int posicionFrase = 0;
    ArrayList<CountDownTimer> contadoresImagenes;
    ArrayList<CountDownTimer> contadoresFrases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtImagenes = (EditText)findViewById(R.id.edtImagenes);
        edtFrases = (EditText)findViewById(R.id.edtFrases);
        btnDescargar = (Button)findViewById(R.id.btnDescargar);
        btnDescargar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                pararAplicacion();      // Si esta la aplicacion corriendo, primero se paran los CountDownTimer en ejecucion
                descargarImagenes();
                descargarFrases();
            }
        });
        imgvDescarga = (ImageView)findViewById(R.id.imgvDescarga);
        txvDescarga = (TextView)findViewById(R.id.txvDescarga);
        memoria = new Memoria(this);
        frases = new StringBuilder();
        contadoresImagenes = new ArrayList<CountDownTimer>();
        contadoresFrases = new ArrayList<CountDownTimer>();

        edtImagenes.setText(URLIMAGENES);
        edtFrases.setText(URLFRASES);

        try {
            intervalo = Long.valueOf(memoria.leerRaw(FICHERORAW).getContenido());
        } catch (Exception e) {
            Toast.makeText(this, "No se ha podido leer el fichero \"" +
                    FICHERORAW + EXTENSIONFICHERORAW + "\"", Toast.LENGTH_LONG).show();
        }
    }

    private void subirError(String mensajeError) {
        String error = new Date().toString() + " -> " + mensajeError;
        RequestParams params = new RequestParams();
        params.put("error", error);
        RestClient.post(URLERRORES, params, new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                String mensaje;
                if(responseString != null) {
                    mensaje = responseString;
                } else {
                    mensaje = throwable.getLocalizedMessage();
                }
                Toast.makeText(MainActivity.this, "El error \"" + mensaje +
                        "\" no se ha subido al Servidor", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                String mensaje;
                if(responseString != null) {
                    mensaje = responseString;
                } else {
                    mensaje = String.valueOf(statusCode);
                }
                Toast.makeText(MainActivity.this, "El error \"" + mensaje +
                 "\" se ha subido con exito al Servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Resultado leer(File fichero, String codigo) {
        FileInputStream fis = null;
        InputStreamReader isw = null;
        BufferedReader in = null;
        StringBuilder miCadena = new StringBuilder();
        Resultado resultado = new Resultado();
        int n;
        resultado.setCodigo(true);
        try {
            fis = new FileInputStream(fichero);
            isw = new InputStreamReader(fis, codigo);
            in = new BufferedReader(isw);
            while ((n = in.read()) != -1)
                miCadena.append((char) n);
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
            resultado.setCodigo(false);
            resultado.setMensaje(e.getMessage());
            subirError(e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                    resultado.setContenido(miCadena.toString());
                }
            } catch (IOException e) {
                Log.e("Error al cerrar", e.getMessage());
                resultado.setCodigo(false);
                resultado.setMensaje(e.getMessage());
                subirError(e.getMessage());
            }
        }
        return resultado;
    }

    private void contadorImagenes(final String[] urlsImagenes, int tiempo, long interval) {
        final long milisegundos = intervalo;
        cdtImagenes = new CountDownTimer(tiempo, interval) {

            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                Picasso.with(MainActivity.this)
                        .load(urlsImagenes[posicionImagen++ % urlsImagenes.length])
                        .placeholder(R.drawable.descargar)      // Muestra una imagen con una flecha para indicar que se esta intentando descargar
                        .error(R.drawable.error)                // Muestra una imagen con una X para indicar que no se ha podido descargar
                        .into(imgvDescarga, new Callback() {

                            @Override
                            public void onSuccess() {
                                contadorImagenes(urlsImagenes, TIEMPO, milisegundos);
                            }

                            @Override
                            public void onError() {
                                int posicion = posicionImagen % urlsImagenes.length;
                                String mensaje = "La imagen de la ruta \"" +
                                        urlsImagenes[posicion] +
                                        "\" no se ha descargado";
                                Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                                subirError(mensaje);
                                contadorImagenes(urlsImagenes, TIEMPO, milisegundos);
                            }
                        });
            }
        }.start();
        contadoresImagenes.add(cdtImagenes);
    }

    private void contadorFrases(final String[] frases, int tiempo, long interval) {
        final long milisegundos = intervalo;
        cdtFrases = new CountDownTimer(tiempo, interval) {

            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                    txvDescarga.setText(frases[posicionFrase++ % frases.length]);
                    contadorFrases(frases, TIEMPO, milisegundos);
            }
        }.start();
        contadoresFrases.add(cdtFrases);
    }

    private void descargarImagenes() {
        final ProgressDialog progreso = new ProgressDialog(this);
        String url = String.valueOf(edtImagenes.getText());
        if(url != null) {
            RestClient.get(url, new FileAsyncHttpResponseHandler(this) {

                @Override
                public void onStart() {
                    super.onStart();
                    progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progreso.setMessage("Conectando . . .");
                    progreso.setCancelable(false);
                    progreso.show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                    progreso.dismiss();
                    String mensaje = "El fichero \"" + FICHEROENLACES + "\" no se ha descargado";
                    Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                    subirError(mensaje);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, File file) {
                    progreso.dismiss();
                    Resultado resultado = leer(file, UTF8);
                    String[] urlsImagenes = resultado.getContenido().split("\n");
                    contadorImagenes(urlsImagenes, 0, 0);
                }
            });
        } else {
            String mensaje = "La ruta de las imagenes esta vacia";
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            subirError(mensaje);
        }
    }

    private void descargarFrases() {
        final ProgressDialog progreso = new ProgressDialog(this);
        String url = String.valueOf(edtFrases.getText());
        if(url != null) {
            RestClient.get(url, new FileAsyncHttpResponseHandler(this) {

                @Override
                public void onStart() {
                    super.onStart();
                    progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progreso.setMessage("Conectando . . .");
                    progreso.setCancelable(false);
                    progreso.show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                    progreso.dismiss();
                    String mensaje = "El fichero \"" + FICHEROFRASES + "\" no se ha descargado";
                    Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                    subirError(mensaje);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, File file) {
                    progreso.dismiss();
                    Resultado resultado = leer(file, UTF8);
                    String[] frases = resultado.getContenido().split("\n");
                    contadorFrases(frases, 0, 0);
                }
            });
        } else {
            String mensaje = "La ruta de las frases esta vacia";
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            subirError(mensaje);
        }
    }

    private void pararAplicacion() {
        // Se paran todos los CountDownTimer en ejecución desde el último hasta el primero
        for(int i = contadoresImagenes.size() - 1; i > 0; i--) {
            CountDownTimer contador = contadoresImagenes.get(i);
            if (contador != null) contador.cancel();
        }
        for(int i = contadoresFrases.size() - 1; i > 0; i--) {
            CountDownTimer contador = contadoresFrases.get(i);
            if (contador != null) contador.cancel();
        }
        // Se inicializan las posiciones
        posicionImagen = 0;
        posicionFrase = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        pararAplicacion();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pararAplicacion();
    }
}