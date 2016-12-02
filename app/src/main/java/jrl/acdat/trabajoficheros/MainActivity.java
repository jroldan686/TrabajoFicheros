package jrl.acdat.trabajoficheros;
import android.media.Image;
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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

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

        edtImagenes.setText("http://192.168.2.11/acceso/enlaces.txt");
        edtFrases.setText("http://192.168.2.11/acceso/frases.txt");

        try {
            intervalo = Long.valueOf(memoria.leerRaw("intervalo").getContenido());
        } catch (Exception e) {
            Toast.makeText(this, "No se ha podido leer el fichero intervalo.txt", Toast.LENGTH_SHORT).show();
        }

        //StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
    }

    private Resultado leer(File fichero, String codigo) {
        FileInputStream fis = null;
        InputStreamReader isw = null;
        BufferedReader in = null;
        //String linea;
        StringBuilder miCadena = new StringBuilder();
        //String codigo = "UTF-8";
        Resultado resultado = new Resultado();
        int n;
        resultado.setCodigo(true);
        try {
            fis = new FileInputStream(fichero);
            isw = new InputStreamReader(fis, codigo);
            in = new BufferedReader(isw);
            while ((n = in.read()) != -1)
                miCadena.append((char) n);
            //while ((linea = in.readLine()) != null)
            //miCadena.append(linea).append('\n');Leer de un fichero en memoria interna (III)
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
            resultado.setCodigo(false);
            resultado.setMensaje(e.getMessage());
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
            }
        }
        return resultado;
    }

    private void descargarImagenes() {
        String url = String.valueOf(edtImagenes.getText());
        if(url != null) {
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(url, new FileAsyncHttpResponseHandler(this) {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                    Toast.makeText(MainActivity.this, "El fichero " + file.getPath() + " no se ha descargado", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, File file) {
                    //Toast.makeText(MainActivity.this, "El fichero " + file.getPath() + " se ha descargado con exito", Toast.LENGTH_SHORT).show();

                    Resultado resultado = leer(file, UTF8);
                    final String[] urlsImagenes = resultado.getContenido().split("\n");

                    for(int i = 0; i < urlsImagenes.length; i++) {
                        final int numImagen = i;
                        new CountDownTimer(TIEMPO, intervalo * SEGUNDO) {

                            @Override
                            public void onTick(long l) {

                            }

                            @Override
                            public void onFinish() {
                                Picasso.with(MainActivity.this)
                                        .load(urlsImagenes[numImagen])
                                        .placeholder(R.drawable.descargar)
                                        .error(R.drawable.error)
                                        .into(imgvDescarga, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Toast.makeText(MainActivity.this, "La imagen de la ruta " + urlsImagenes[numImagen] + " no se ha descargado", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }.start();
                    }
                    /*
                        my_button.setBackgroundResource(R.drawable.icon);

                        // Execute some code after 2 seconds have passed
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                my_button.setBackgroundResource(R.drawable.defaultcard);
                            }
                        }, 2000);
                     */
                }
            });
        } else {
            Toast.makeText(this, "La ruta de las imagenes esta vacia", Toast.LENGTH_SHORT).show();
        }
    }

    private void descargarFrases() {
        String url = String.valueOf(edtFrases.getText());
        if(url != null) {
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(url, new FileAsyncHttpResponseHandler(this) {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                    Toast.makeText(MainActivity.this, "El fichero " + file.getPath() + " no se ha descargado", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, File file) {
                    //Toast.makeText(MainActivity.this, "El fichero " + file.getPath() + " se ha descargado con exito", Toast.LENGTH_SHORT).show();

                    Resultado resultado = leer(file, UTF8);
                    String[] frases = resultado.getContenido().split("\n");
                    for(int i = 0; i < frases.length; i++)
                        txvDescarga.setText(frases[i]);
                }
            });
        } else {
            Toast.makeText(this, "La ruta de las frases esta vacia", Toast.LENGTH_SHORT).show();
        }
    }
}