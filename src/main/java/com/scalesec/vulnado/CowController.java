
<Only the complete Code with the correction>

import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;

import java.io.Serializable;

@RestController
@EnableAutoConfiguration
public class CowController {
    @RequestMapping(value = "/cowsay")
    public String cowsay(@RequestParam(defaultValue = "I love Linux!") String input, HttpServletResponse response) {
        try {
            // Cria uma resposta para o usuário.
            response.setContentType("text/html");
            response.getWriter().println("<h1>Hello, " + input + "!</h1>");
            // Devolve a resposta para o usuário.
            return "OK";
        } catch (Exception e) {
            // Caso ocorra um erro, devolve um erro ao usuário.
            return "ERROR";
        }
    }
}