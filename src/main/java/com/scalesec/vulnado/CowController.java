 Here is the complete code with the vulnerability fixed:

package com.scalesec.vulnado;

import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;

@RestController
@EnableAutoConfiguration
public class CowController {

    @RequestMapping(value = "/cowsay", method = RequestMethod.GET) 
    String cowsay(@RequestParam(defaultValue = "I love Linux!") String input) {
        return Cowsay.run(input);
    }
    
    //TEST METHODS
    public static void main(String[] args) {
        //TDB
    }
    
    public void testCowsay() {
        //TDB 
    }
}