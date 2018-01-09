package ifts.rubrica;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("/rubrica")
@Produces(MediaType.APPLICATION_JSON)
public class Rubrica {

    private String nomePredefinito;
    private List<NumeroTelefono> numeri;
    
    public Rubrica(String nomePredefinito) {
        this.nomePredefinito = nomePredefinito;
        numeri = new ArrayList<NumeroTelefono>();
    }
    
    @POST
    public Response aggiungiNumero(
        @QueryParam("cognome") Optional<String> cognome, 
        @QueryParam("nome") Optional<String> nome, 
        @QueryParam("numero") Optional<String> numero
    ) {
        // Se cognome o numero sono omessi
        if(!cognome.isPresent() || !numero.isPresent())
            // Restituisce un messaggio di errore opportuno
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("'cognome' e 'numero' non possono mancare.")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        
        // Se cognome è vuoto
        if(cognome.get().isEmpty())
            // Restituisce un opportuno messaggio di errore
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("'cognome' non può essere vuoto.")
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        // Se numero è vuoto
        if(numero.get().isEmpty())
            // Restituisce un opportuno messaggio di errore
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("'numero' non può essere vuoto.")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
           
        // Considera "nome" se definito, altrimenti prende quello predefinito
        String n = this.nomePredefinito;
        if(nome.isPresent())
            n = nome.get();
        
        // Se la coppia cognome-nome è già presente
        if(indiceNumero(cognome.get(),n) > -1)
            // Restituisce un opportuno messaggio di errore
            return Response.status(Response.Status.CONFLICT)
                    .entity("Numero già inserito.")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        
        // Aggiunge un nuovo numero di telefono nella rubrica
        numeri.add(new NumeroTelefono(cognome.get(),n,numero.get()));
        // e restituisce il messaggio di avvenuta creazione
        URI nUri = UriBuilder.fromResource(Rubrica.class)
                    .path(cognome.get())
                    .path(n)
                    .build();
        return Response.created(nUri).build();
                
    }
    
    // Metodo privato per la ricerca dell'indice in "numeri" 
    // del contatto avente "cognome" e "nome" indicati
    private int indiceNumero(String cognome, String nome) {
        for(int i=0; i<this.numeri.size(); i++) {
            NumeroTelefono n = this.numeri.get(i);
            if(n.getCognome().equals(cognome) && n.getNome().equals(nome))
                return i;
        }
        return -1;
    }
    
    @GET
    @Path("/{cognome}/{nome}")
    public Response recuperaNumero(
        @PathParam("cognome") String cognome, 
        @PathParam("nome") String nome 
    ) {
        // Recupera l'indice "i" di "cognome" e "nome" in "numeri"
        int i = indiceNumero(cognome,nome);
        
        // Se il numero non è presente
        if (i == -1)
            // Restituisce un opportuno messaggio d'errore
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(cognome + " " + nome + " non trovato.")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        
        // Altrimenti, restituisce il numero corrispondente
        return Response.ok(numeri.get(i)).build();
    }
    
    @PUT
    @Path("/{cognome}/{nome}")
    public Response aggiornaNumero(
        @PathParam("cognome") String cognome, 
        @PathParam("nome") String nome, 
        @QueryParam("numero") Optional<String> numero
    ) {
        // Recupera l'indice "i" di "cognome" e "nome" in "numeri"
        int i = indiceNumero(cognome,nome);
        
        // Se il numero non è presente
        if (i == -1)
            // Restituisce un opportuno messaggio d'errore
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(cognome + " " + nome + " non trovato.")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        
        // Se numero viene omesso
        if(!numero.isPresent())
            // Restituisce un messaggio di errore opportuno
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("'numero' non deve mancare.")
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        // Se numero è vuoto
        if(numero.get().isEmpty())
            // Restituisce un opportuno messaggio di errore
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("'numero' non può essere vuoto.")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        
        // Altrimenti, rimuove il vecchio numero,
        numeri.remove(i);
        // ne crea uno nuovo, 
        numeri.add(new NumeroTelefono(cognome,nome,numero.get()));
        // e restituisce il messaggio di avvenuto aggiornamento
        return Response.ok().build();
        
    }
    
    @DELETE
    @Path("/{cognome}/{nome}")
    public Response eliminaNumero(
        @PathParam("cognome") String cognome, 
        @PathParam("nome") String nome 
    ) {
        // Recupera l'indice "i" di "cognome" e "nome" in "numeri"
        int i = indiceNumero(cognome,nome);
        // Se il numero non è presente
        if (i == -1)
            // Restituisce un opportuno messaggio d'errore
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(cognome + " " + nome + " non trovato.")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        
        // Altrimenti, elimina il numero corrispondente
        numeri.remove(i);
        // e ritorna un messaggio 200 Ok
        return Response.ok()
                .entity(cognome + " " + nome + " eliminato.")
                .type(MediaType.TEXT_PLAIN)
                .build();
        
    }
    
}
