package service.cloud.request.clientRequest.prueba.model;

public class Archivo {

    private RequestDeclare requestDeclare;

    public Archivo(RequestDeclare requestDeclare) {
        this.requestDeclare = requestDeclare;
    }

    public RequestDeclare getRequestDeclare() {
        return requestDeclare;
    }

    public void setRequestDeclare(RequestDeclare requestDeclare) {
        this.requestDeclare = requestDeclare;
    }

    @Override
    public String toString() {
        return "Archivo{" +
                "requestDeclare=" + requestDeclare +
                '}';
    }
}
