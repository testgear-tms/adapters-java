package io.test_gear.services;

import io.test_gear.models.LinkItem;
import io.test_gear.models.LinkType;
import io.test_gear.properties.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class Adapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Adapter.class);
    private static AdapterManager adapterManager;
    private static ResultStorage storage;

    public static AdapterManager getAdapterManager() {
        if (Objects.isNull(adapterManager)) {
            Properties appProperties = AppProperties.loadProperties();
            ConfigManager manager = new ConfigManager(appProperties);
            adapterManager = new AdapterManager(manager);
        }
        return adapterManager;
    }

    public static ResultStorage getResultStorage() {
        if (Objects.isNull(storage)) {
            storage = new ResultStorage();
        }
        return storage;
    }

    /**
     * @deprecated This method is no longer acceptable to compute time between versions.
     * <p> Use {@link Adapter#addLinks(String, String, String, LinkType)} instead.
     */
    @Deprecated
    public static void link(final String title, final String description, final LinkType type, final String url) {
        final LinkItem link = new LinkItem().setTitle(title).setDescription(description).setType(type).setUrl(url);
        getAdapterManager().updateTestCase(testResult -> testResult.getResultLinks().add(link));
    }

    /**
     * @deprecated This method is no longer acceptable to compute time between versions.
     * <p> Use {@link Adapter#addLinks(String, String, String, LinkType)} instead.
     */
    @Deprecated
    public static void addLink(final String url, final String title, final String description, final LinkType type) {
        LinkItem link = new LinkItem().setTitle(title)
                .setDescription(description)
                .setType(type)
                .setUrl(url);

        List<LinkItem> links = new ArrayList<>();
        links.add(link);

        addLinks(links);
    }

    public static void addLinks(final String url, final String title, final String description, final LinkType type) {
        LinkItem link = new LinkItem().setTitle(title)
                .setDescription(description)
                .setType(type)
                .setUrl(url);

        List<LinkItem> links = new ArrayList<>();
        links.add(link);

        addLinks(links);
    }

    public static void addLinks(List<LinkItem> links) {
        getAdapterManager().updateTestCase(testResult -> testResult.getResultLinks().addAll(links));
    }

    public static void addAttachments(List<String> attachments) {
        getAdapterManager().addAttachments(attachments);
    }

    public static void addAttachments(String attachment) {
        List<String> attachments = new ArrayList<>();
        attachments.add(attachment);

        addAttachments(attachments);
    }

    public static void addAttachments(String content, String fileName) {
        if (fileName == null) {
            fileName = UUID.randomUUID() + "-attachment.txt";
        }

        Path path = Paths.get(fileName);
        try {
            BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset());
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            LOGGER.error(String.format("Can not write file '%s':", fileName), e);
        }

        addAttachments(fileName);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.error(String.format("Can not delete file '%s':", fileName), e);
        }
    }

    /**
     * @deprecated This method is no longer acceptable to compute time between versions.
     * <p> Use {@link Adapter#addAttachments(String attachment)} instead.
     */
    @Deprecated
    public static void addAttachment(String attachment) {
        List<String> attachments = new ArrayList<>();
        attachments.add(attachment);

        getAdapterManager().addAttachments(attachments);
    }

    public static void addMessage(String message) {
        getAdapterManager().updateTestCase(testResult -> testResult.setMessage(message));
    }
}
