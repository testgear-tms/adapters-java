package io.test_gear.listener;

import io.test_gear.annotations.*;
import org.junit.runner.Description;

import io.test_gear.annotations.*;
import io.test_gear.models.LinkItem;
import java.util.LinkedList;
import java.util.List;
import io.test_gear.models.Label;

public class Utils {

    public static String extractExternalID(final Description method) {
        final ExternalId annotation = method.getAnnotation(ExternalId.class);
        return (annotation != null) ? annotation.value() : null;
    }

    public static String extractDisplayName(final Description method) {
        final DisplayName annotation = method.getAnnotation(DisplayName.class);
        return (annotation != null) ? annotation.value() : null;
    }

    public static String extractWorkItemId(final Description method) {
        final WorkItemId annotation = method.getAnnotation(WorkItemId.class);
        return (annotation != null) ? annotation.value() : null;
    }

    public static List<LinkItem> extractLinks(final Description method) {
        final List<LinkItem> links = new LinkedList<LinkItem>();
        final Links linksAnnotation = method.getAnnotation(Links.class);
        if (linksAnnotation != null) {
            for (final Link link : linksAnnotation.links()) {
                links.add(makeLink(link));
            }
        }
        else {
            final Link linkAnnotation = method.getAnnotation(Link.class);
            if (linkAnnotation != null) {
                links.add(makeLink(linkAnnotation));
            }
        }
        return links;
    }

    public static List<Label> extractLabels(final Description method) {
        final List<Label> labels = new LinkedList<Label>();
        final Labels annotation = method.getAnnotation(Labels.class);
        if (annotation != null) {
            for (final String s : annotation.value()) {
                final Label label = new Label();
                label.setName(s);
                labels.add(label);
            }
        }
        return labels;
    }

    public static String extractTitle(final Description method) {
        final Title annotation = method.getAnnotation(Title.class);
        return (annotation != null) ? annotation.value() : null;
    }

    private static LinkItem makeLink(final Link linkAnnotation) {
        return new LinkItem()
                .setTitle(linkAnnotation.title())
                .setDescription(linkAnnotation.description())
                .setUrl(linkAnnotation.url())
                .setType(linkAnnotation.type());
    }

    public static String extractDescription(final Description method) {
        final io.test_gear.annotations.Description annotation = method.getAnnotation(io.test_gear.annotations.Description.class);
        return (annotation != null) ? annotation.value() : null;
    }
}
