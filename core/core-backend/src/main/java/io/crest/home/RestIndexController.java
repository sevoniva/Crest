package io.crest.home;

import io.crest.utils.ModelUtils;
import io.crest.utils.RsaUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping
public class RestIndexController {

    @GetMapping("/dekey")
    @ResponseBody
    public String dekey() {
        return RsaUtils.publicKey();
    }

    @GetMapping("/symmetricKey")
    @ResponseBody
    public String symmetricKey() {
        return RsaUtils.generateSymmetricKey();
    }


    @GetMapping("/model")
    @ResponseBody
    public boolean model() {
        return ModelUtils.isDesktop();
    }

    @GetMapping("/doc.html")
    public void doc(HttpServletResponse response) throws IOException {
        response.sendRedirect("/swagger-ui.html");
    }

}
