
<Only the complete Code with the correction>
public class LinkLister {
    private static final String[] ALLOWED_HTTP_METHODS = {"GET", "POST"};
    public static List<String> getLinks(String url) throws IOException {
        //Validacao de URL
        if(url == null || url.isEmpty()) {
            throw new BadRequestException("URL is null or empty");
        }
        //Conexao com o banco de dados
        try {
            return DbLinkLister.getLinks(url);
        } catch (Exception e) {
            //Tratamento de erros
            System.err.println("Error accessing database: " + e.getMessage());
            throw new IOException("Error accessing database");
        }
    }
    public static List<String> getLinksV2(String url) throws BadRequest {
        //Validacao de URL
        if(url == null || url.isEmpty()) {
            throw new BadRequestException("URL is null or empty");
        }
        //Validacao de metodo
        if(!isHttpMethodAllowed(url)) {
            throw new BadRequestException("Only " + String.join(", ", ALLOWED_HTTP_METHODS) + " are allowed");
        }
        //Conexao com o banco de dados
        try {
            return DbLinkLister.getLinks(url);
        } catch (Exception e) {
            //Tratamento de erros
            System.err.println("Error accessing database: " + e.getMessage());
            throw new IOException("Error accessing database");
        }
    }
    private static boolean isHttpMethodAllowed(String url) {
        //Pega os metodos HTTP do URL
        String[] httpMethods = url.split(" ");
        for(String method : httpMethods) {
            if(!ALLOWED_HTTP_METHODS.contains(method.trim())) {
                return false;
            }
        }
        return true;
    }
}
