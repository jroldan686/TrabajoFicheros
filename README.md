PLANTEAMIENTO

He pensado usar dos métodos diferentes para las descargas de las imágenes y las frases: el método descargarImagenes() y el método descargarFrases(). Ambos métodos descargan un fichero de texto asíncronamente utilizando RestClient() con FileAsyncHttpResponseHandler().

Una vez descargados, el método descargarImagenes() lee, a través del método leer(), y almacena cada uno de los enlaces obtenidos del fichero de texto "enlaces.txt" descargado del Servidor, y llama al método contadorImagenes() para que Picasso descargue cada una de las imágenes referenciadas en los enlaces, y vuelve a llamar indefinidamente al método contadorImagenes() por cada una, tras un periodo de tiempo obtenido del fichero "intervalo.txt" dentro de la carpeta "raw".

Al mismo tiempo, el método descargarFrases() descarga y lee el fichero "frases.txt", utilizando también el método leer(), almacena las frases y seguidamente llama al método contadorFrases() para que muestre cada una de ellas indefinidamente en el mismo intervalo de tiempo.

Además, por cada error que se produce en la aplicación, se suben los errores al Servidor llamando al método subirError().

MEJORAS

Por cada llamada a los métodos contadorImagenes() y contadorFrases() se van almacenando en un ArrayList<CountDownTimer>() cada una de las instancias de los CountDownTimer que se van creando; de modo que al salir de la aplicación, al pausarla o cada vez que se hace click en el botón btnDescargar, se llama al método pararAplicacion() que cancela todos los CountDownTimer() que se han creado al llamar a los métodos contadorImagenes() y contadorFrases() desde el último instanciado hasta el primero. Esto evita que varios hilos de ejecución muestren a la vez y a destiempo las imágenes y las frases.

Además, este método, inicializa los contadores de posición de las imágenes y las frases almacenadas para que comiencen desde cero.