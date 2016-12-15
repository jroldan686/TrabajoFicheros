package jrl.acdat.trabajoficheros;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
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

    //public static final String URLENLACES = "http://192.168.2.11/acceso/enlaces.txt";
    //public static final String URLFRASES = "http://192.168.2.11/acceso/frases.txt";
    //public static final String URLERRORES = "http://192.168.2.11/acceso/errors.php";
    //public static final String URLENLACES = "http://192.168.1.5/curso1617/enlaces.txt";
    //public static final String URLFRASES = "http://192.168.1.5/curso1617/frases.txt";
    //public static final String URLERRORES = "http://192.168.1.5/curso1617/errors.php";
    public static final String URLENLACES = "http://bitbits.hopto.org/ACDAT/enlaces.txt";
    public static final String URLFRASES = "http://bitbits.hopto.org/ACDAT/frases.txt";
    public static final String URLERRORES = "http://bitbits.hopto.org/ACDAT/errors.php";
    public static final String UTF8 = "utf-8";
    public static final int SEGUNDO = 1000;
    public static final int TIEMPO = 5 * SEGUNDO;

    EditText edtImagenes, edtFrases;
    Button btnDescargar;
    ImageView imgvDescarga;
    TextView txvDescarga;
    Memoria memoria;
    StringBuilder frases;
    long intervalo = 1;
    int posicionImagen = 0;
    int posicionFrase = 0;

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
                descargarImagenes();
                descargarFrases();
            }
        });
        imgvDescarga = (ImageView)findViewById(R.id.imgvDescarga);
        txvDescarga = (TextView)findViewById(R.id.txvDescarga);
        memoria = new Memoria(this);
        frases = new StringBuilder();

        edtImagenes.setText(URLENLACES);
        edtFrases.setText(URLFRASES);

        try {
            intervalo = Long.valueOf(memoria.leerRaw("intervalo").getContenido());
        } catch (Exception e) {
            Toast.makeText(this, "No se ha podido leer el fichero intervalo.txt", Toast.LENGTH_LONG).show();
        }
    }

    private void subirError(String mensajeError) {
        final ProgressDialog progreso = new ProgressDialog(this);

        String error = new Date().toString() + " -> " + mensajeError;
        RequestParams params = new RequestParams();
        params.put("error", error);
        RestClient.post(URLERRORES, params, new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progreso.setMessage("Conectando . . .");
                progreso.setCancelable(false);
                progreso.show();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                progreso.dismiss();
                Toast.makeText(MainActivity.this, "El error " + responseString +
                        " no se ha subido al Servidor", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                progreso.dismiss();
                Toast.makeText(MainActivity.this, "El error " + responseString +
                 " se ha subido con exito al Servidor", Toast.LENGTH_SHORT).show();
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

        new CountDownTimer(tiempo, interval) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                Picasso.with(MainActivity.this)
                        .load(urlsImagenes[posicionImagen++ % urlsImagenes.length])
                        .placeholder(R.drawable.descargar)
                        .error(R.drawable.error)
                        .into(imgvDescarga, new Callback() {

                            @Override
                            public void onSuccess() {
                                contadorImagenes(urlsImagenes, TIEMPO, milisegundos);
                            }

                            @Override
                            public void onError() {
                                String mensaje = "La imagen de la ruta " +
                                        urlsImagenes[posicionImagen % urlsImagenes.length] +
                                        " no se ha descargado";
                                Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                                subirError(mensaje);
                                contadorImagenes(urlsImagenes, TIEMPO, milisegundos);
                            }
                        });
            }
        }.start();
    }

    private void contadorFrases(final String[] frases, int tiempo, long interval) {

        final long milisegundos = intervalo;

        new CountDownTimer(tiempo, interval) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                    txvDescarga.setText(frases[posicionFrase++ % frases.length]);
                    contadorFrases(frases, TIEMPO, milisegundos);
            }
        }.start();
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
                    String mensaje = "El fichero " + file.getPath() + " no se ha descargado";
                    Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                    subirError(mensaje);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, File file) {
                    progreso.dismiss();
                    //Toast.makeText(MainActivity.this, "El fichero " + file.getPath() +
                    // " se ha descargado con exito", Toast.LENGTH_SHORT).show();

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
                    String mensaje = "El fichero " + file.getPath() + " no se ha descargado";
                    Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                    subirError(mensaje);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, File file) {
                    progreso.dismiss();
                    //Toast.makeText(MainActivity.this, "El fichero " + file.getPath() +
                    // " se ha descargado con exito", Toast.LENGTH_SHORT).show();

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
}