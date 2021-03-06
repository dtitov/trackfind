package no.uio.ifi.trackfind.frontend;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.ValueProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.TextRenderer;
import elemental.json.JsonValue;
import lombok.extern.slf4j.Slf4j;
import no.uio.ifi.trackfind.backend.pojo.TfHub;
import no.uio.ifi.trackfind.backend.pojo.TfUser;
import no.uio.ifi.trackfind.backend.pojo.TfVersion;
import no.uio.ifi.trackfind.backend.repositories.HubRepository;
import no.uio.ifi.trackfind.backend.services.impl.MetamodelService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Versions Vaadin UI of the application.
 * Uses custom theme (VAADIN/themes/trackfind/trackfind.scss).
 * Uses custom WidgetSet (TrackFindWidgetSet.gwt.xml).
 *
 * @author Dmytro Titov
 */
@SpringUI(path = "/versions")
@Widgetset("TrackFindWidgetSet")
@Title("Versions")
@Theme("trackfind")
@Slf4j
public class TrackFindVersionsUI extends AbstractUI {

    private MetamodelService metamodelService;
    private HubRepository hubRepository;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        HorizontalLayout headerLayout = buildHeaderLayout();
        VerticalLayout versionsLayout = buildVersionsLayout();
        HorizontalLayout mainLayout = buildMainLayout(versionsLayout);
        HorizontalLayout footerLayout = buildFooterLayout();
        VerticalLayout outerLayout = buildOuterLayout(headerLayout, mainLayout, footerLayout);
        setContent(outerLayout);
    }

    @SuppressWarnings("unchecked")
    private VerticalLayout buildVersionsLayout() {
        VerticalLayout versionsLayout = new VerticalLayout();
        versionsLayout.setSizeFull();
        TabSheet versionsTabSheet = new TabSheet();
        versionsTabSheet.setSizeFull();
        Panel versionsPanel = new Panel("Versions", versionsTabSheet);
        versionsPanel.setSizeFull();
        for (TfHub hub : trackFindService.getTrackHubs(true)) {
            Grid<TfVersion> grid = new Grid<>(TfVersion.class);
            grid.setSizeFull();
            grid.removeColumn("current");
            grid.removeColumn("hub");
            grid.removeColumn("objectTypes");
            grid.removeColumn("mappings");
            grid.setStyleGenerator((StyleGenerator<TfVersion>) item -> {
                if (item.getCurrent()) {
                    return "green";
                } else {
                    return "white";
                }
            });
            ButtonRenderer buttonRenderer = new ButtonRenderer((ClickableRenderer.RendererClickListener<TfVersion>) event -> {
                metamodelService.activateVersion(event.getItem());
                TfHub updatedHub = hubRepository.findByRepositoryAndName(hub.getRepository(), hub.getName());
                grid.setItems(updatedHub.getVersions());
            }) {
                @Override
                public JsonValue encode(Object value) {
                    return super.encode("Set current");
                }
            };
            grid.getColumn("id").setRenderer(buttonRenderer).setCaption("Action");
            grid.getColumn("user").setRenderer((ValueProvider) user -> {
                TfUser tfUser = (TfUser) user;
                return tfUser == null ? null : tfUser.getFullName();
            }, new TextRenderer("Auto-crawled"));
            grid.setColumnOrder("version", "operation", "user", "time", "validation", "id");
            grid.sort("id");
            grid.setData(hub);
            grid.setItems(hub.getVersions());
            versionsTabSheet.addTab(grid, hub.getRepository() + ": " + hub.getName()).getComponent().setSizeFull();
        }
        versionsLayout.addComponents(versionsPanel);
        versionsLayout.setExpandRatio(versionsPanel, 1f);
        return versionsLayout;
    }

    private HorizontalLayout buildMainLayout(VerticalLayout leftLayout) {
        HorizontalLayout mainLayout = new HorizontalLayout(leftLayout);
        mainLayout.setExpandRatio(leftLayout, 0.66f);
        mainLayout.setSizeFull();
        return mainLayout;
    }

    @Autowired
    public void setMetamodelService(MetamodelService metamodelService) {
        this.metamodelService = metamodelService;
    }

    @Autowired
    public void setHubRepository(HubRepository hubRepository) {
        this.hubRepository = hubRepository;
    }

}
