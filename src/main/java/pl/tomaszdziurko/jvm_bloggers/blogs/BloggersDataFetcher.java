package pl.tomaszdziurko.jvm_bloggers.blogs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tomaszdziurko.jvm_bloggers.blogs.domain.BlogType;
import pl.tomaszdziurko.jvm_bloggers.blogs.json_data.BloggersData;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Component
@Slf4j
public class BloggersDataFetcher {

    private final Optional<URL> bloggersUrlOptional;
    private final Optional<URL> companiesUrlOptional;
    private final BloggersDataUpdater bloggersDataUpdater;

    @Autowired
    public BloggersDataFetcher(@Value("${bloggers.data.file.url}") String bloggersDataUrlString,
                               @Value("${companies.data.file.url}") String companiesDataUrlString,
                               BloggersDataUpdater bloggersDataUpdater) {
        bloggersUrlOptional = convertToUrl(bloggersDataUrlString);
        companiesUrlOptional = convertToUrl(companiesDataUrlString);
        this.bloggersDataUpdater = bloggersDataUpdater;
    }

    private Optional<URL> convertToUrl(String urlString)  {
        try {
            return Optional.of(new URL(urlString));
        } catch (MalformedURLException e) {
            log.error("Invalid URL " + urlString);
            return Optional.empty();
        }
    }

    public void refreshData() {
        refreshBloggersDataFor(bloggersUrlOptional, BlogType.PERSONAL);
        refreshBloggersDataFor(companiesUrlOptional, BlogType.COMPANY);
    }

    private void refreshBloggersDataFor(Optional<URL> blogsDataUrl, BlogType blogType) {
        if (!blogsDataUrl.isPresent()) {
            log.warn("No valid URL specified for {}. Skipping.", blogType);
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            BloggersData bloggers = mapper.readValue(blogsDataUrl.get(), BloggersData.class);
            bloggers.getBloggers().stream().forEach(it -> it.setBlogType(blogType));
            bloggersDataUpdater.updateData(bloggers);
        } catch (IOException e) {
            log.error("Exception during parse process for {}", blogType, e);
        }
    }

}