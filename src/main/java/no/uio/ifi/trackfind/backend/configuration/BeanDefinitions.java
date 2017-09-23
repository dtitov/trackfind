package no.uio.ifi.trackfind.backend.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static no.uio.ifi.trackfind.TrackFindApplication.INDICES_FOLDER;

@Configuration
public class BeanDefinitions {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Gson gson() {
        return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().setDateFormat(DATE_FORMAT).create();
    }

    @Bean
    public Git git() throws IOException, GitAPIException {
        Git git = Git.init().setDirectory(new File(INDICES_FOLDER)).call();
        try {
            git.log().call();
        } catch (NoHeadException e) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit.").call();
        }
        return git;
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).
                useDefaultResponseMessages(false).
                select().
                apis(RequestHandlerSelectors.basePackage("no.uio.ifi.trackfind.backend.rest.controllers")).
                paths(PathSelectors.any()).
                build().
                apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo("TrackFind",
                "Search engine for finding genomic tracks",
                String.valueOf(getClass().getPackage().getImplementationVersion()),
                null,
                new Contact(null, "https://github.com/elixir-no-nels/trackfind", null),
                null,
                null,
                Collections.emptyList());
    }

    @Bean
    public ExecutorService workStealingPool() {
        return Executors.newWorkStealingPool(10);
    }

    @Bean
    public ExecutorService singleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean
    public ExecutorService fixedThreadPool() {
        return Executors.newFixedThreadPool(4);
    }

}
