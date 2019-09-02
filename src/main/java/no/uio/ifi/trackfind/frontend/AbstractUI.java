package no.uio.ifi.trackfind.frontend;

import com.vaadin.data.HasValue;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.*;
import lombok.extern.slf4j.Slf4j;
import no.uio.ifi.trackfind.backend.data.TreeNode;
import no.uio.ifi.trackfind.backend.pojo.TfHub;
import no.uio.ifi.trackfind.backend.services.SchemaService;
import no.uio.ifi.trackfind.backend.services.TrackFindService;
import no.uio.ifi.trackfind.frontend.components.TrackFindTree;
import no.uio.ifi.trackfind.frontend.filters.TreeFilter;
import no.uio.ifi.trackfind.frontend.providers.TrackFindDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Main Vaadin UI of the application.
 * Capable of displaying metadata for repositories, constructing and executing search queries along with exporting the results.
 * Uses custom theme (VAADIN/themes/trackfind/trackfind.scss).
 * Uses custom WidgetSet (TrackFindWidgetSet.gwt.xml).
 *
 * @author Dmytro Titov
 */
@Slf4j
public abstract class AbstractUI extends UI {

    @Value("${trackfind.separator}")
    protected String separator;

    protected SchemaService schemaService;
    protected TrackFindService trackFindService;
    protected TrackFindDataProvider trackFindDataProvider;

    protected TabSheet tabSheet;

    @SuppressWarnings("unchecked")
    public TrackFindTree<TreeNode> getCurrentTree() {
        return (TrackFindTree<TreeNode>) tabSheet.getSelectedTab();
    }

    protected TfHub getCurrentHub() {
        return getCurrentTree().getHub();
    }

    protected VerticalLayout buildOuterLayout(HorizontalLayout headerLayout, HorizontalLayout mainLayout, HorizontalLayout footerLayout) {
        VerticalLayout outerLayout = new VerticalLayout(headerLayout, mainLayout, footerLayout);
        outerLayout.setSizeFull();
        outerLayout.setExpandRatio(headerLayout, 0.10f);
        outerLayout.setExpandRatio(mainLayout, 0.8f);
        outerLayout.setExpandRatio(footerLayout, 0.10f);
        outerLayout.setMargin(false);
        return outerLayout;
    }

    protected HorizontalLayout buildFooterLayout() {
        Link nels = new Link(null, new ExternalResource("https://nels.bioinfo.no/"));
        nels.setIcon(new ThemeResource("images/nels.png"));
        Link elixir = new Link(null, new ExternalResource("https://www.elixir-norway.org/"));
        elixir.setIcon(new ThemeResource("images/elixir.png"));
        Link uio = new Link(null, new ExternalResource("https://www.mn.uio.no/ifi/english/research/groups/bmi/"));
        uio.setIcon(new ThemeResource("images/uio.png"));

        HorizontalLayout leftFooterLayout = new HorizontalLayout(nels, elixir, uio);
        leftFooterLayout.setHeight("100%");
        leftFooterLayout.setComponentAlignment(nels, Alignment.MIDDLE_LEFT);
        leftFooterLayout.setComponentAlignment(elixir, Alignment.MIDDLE_LEFT);
        leftFooterLayout.setComponentAlignment(uio, Alignment.MIDDLE_LEFT);

        Link gdpr = new Link("GDPR", new ExternalResource("/GDPR"));
        Link pp = new Link("Privacy Policy", new ExternalResource("/pp"));
        Link tos = new Link("Terms of Use", new ExternalResource("/tos"));
        HorizontalLayout rightFooterLayout = new HorizontalLayout(gdpr, pp, tos);
        rightFooterLayout.setHeight("100%");
        rightFooterLayout.setComponentAlignment(gdpr, Alignment.MIDDLE_RIGHT);
        rightFooterLayout.setComponentAlignment(pp, Alignment.MIDDLE_RIGHT);
        rightFooterLayout.setComponentAlignment(tos, Alignment.MIDDLE_RIGHT);
        HorizontalLayout footerLayout = new HorizontalLayout(leftFooterLayout, rightFooterLayout);
        footerLayout.setSizeFull();
        footerLayout.setComponentAlignment(leftFooterLayout, Alignment.BOTTOM_LEFT);
        footerLayout.setComponentAlignment(rightFooterLayout, Alignment.BOTTOM_RIGHT);
        footerLayout.setMargin(new MarginInfo(false, true, false, true));
        return footerLayout;
    }

    protected HorizontalLayout buildHeaderLayout() {
        HorizontalLayout leftHeaderLayout = new HorizontalLayout();
        leftHeaderLayout.setSizeFull();
        Link logo = new Link(null, new ExternalResource("/"));
        logo.setIcon(new ThemeResource("images/logo.png"));
        HorizontalLayout middleHeaderLayout = new HorizontalLayout(logo);
        middleHeaderLayout.setSizeFull();
        middleHeaderLayout.setComponentAlignment(logo, Alignment.BOTTOM_CENTER);
        Link login = new Link(null, new ExternalResource("/login"));
        login.setIcon(new ThemeResource("images/login.png"));
        HorizontalLayout rightHeaderLayout = new HorizontalLayout(login);
        rightHeaderLayout.setMargin(new MarginInfo(false, true, false, false));
        rightHeaderLayout.setSizeFull();
        rightHeaderLayout.setComponentAlignment(login, Alignment.TOP_RIGHT);
        HorizontalLayout headerLayout = new HorizontalLayout(leftHeaderLayout, middleHeaderLayout, rightHeaderLayout);
        headerLayout.setSizeFull();
        headerLayout.setExpandRatio(leftHeaderLayout, 0.33f);
        headerLayout.setExpandRatio(middleHeaderLayout, 0.33f);
        headerLayout.setExpandRatio(rightHeaderLayout, 0.33f);
        return headerLayout;
    }

    @SuppressWarnings("unchecked")
    protected TextField createFilter(boolean attributes) {
        TextField attributesFilterTextField = new TextField(attributes ? "Filter attributes" : "Filter values", (HasValue.ValueChangeListener<String>) event -> {
            TrackFindTree<TreeNode> currentTree = getCurrentTree();
            TreeFilter filter = (TreeFilter) ((TreeGrid<TreeNode>) currentTree.getCompositionRoot()).getFilter();
            if (attributes) {
                filter.setAttributesFilter(event.getValue());
            } else {
                filter.setValuesFilter(event.getValue());
            }
            currentTree.getDataProvider().refreshAll();
        });
        attributesFilterTextField.setValueChangeMode(ValueChangeMode.EAGER);
        attributesFilterTextField.setWidth(100, Unit.PERCENTAGE);
        return attributesFilterTextField;
    }

    @Autowired
    public void setSchemaService(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @Autowired
    public void setTrackFindService(TrackFindService trackFindService) {
        this.trackFindService = trackFindService;
    }

    @Autowired
    public void setTrackFindDataProvider(TrackFindDataProvider trackFindDataProvider) {
        this.trackFindDataProvider = trackFindDataProvider;
    }

}
