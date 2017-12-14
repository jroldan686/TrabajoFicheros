package jrl.acdat.trabajoficheros;

public class Resultado {
    private boolean codigo; //true es correcto y false indica error
    private String mensaje;
    private String contenido;
    public boolean getCodigo() {
        return codigo;
    }
    public void setCodigo(boolean codigo) {
        this.codigo = codigo;
    }
    public String getMensaje() {
        return mensaje;
    }
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    public String getContenido() {
        return contenido;
    }
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
}