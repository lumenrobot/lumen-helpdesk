package org.lskk.lumen.helpdesk.web.searchindex;

import com.google.common.collect.ImmutableList;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.table.BootstrapDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.lskk.lumen.helpdesk.searchindex.SearchIndex;
import org.lskk.lumen.helpdesk.searchindex.SearchIndexRepository;
import org.lskk.lumen.helpdesk.web.UserLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.wicketstuff.annotation.mount.MountPath;

import javax.inject.Inject;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Map;

@MountPath("search_indexes")
public class SearchIndexesPage extends UserLayout {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexesPage.class);

    @Inject
    private SearchIndexRepository searchIndexRepo;
    @Inject
    private ElasticsearchOperations esTemplate;

    public SearchIndexesPage(PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        final ImmutableList<IColumn<SearchIndex, String>> columns = ImmutableList.of(
                new IdColumn(),
                new PropertyColumn<SearchIndex, String>(new Model<>("Created"), "creationTime"),
                new PropertyColumn<SearchIndex, String>(new Model<>("Properties"), "properties")
        );
        final int rowsPerPage = 20;
        final SortableDataProvider<SearchIndex, String> searchIndexDp = new SortableDataProvider<SearchIndex, String>() {
            @Override
            public Iterator<? extends SearchIndex> iterator(long first, long count) {
                final Page<SearchIndex> page = searchIndexRepo.findAll(
                        new PageRequest((int) first / rowsPerPage, (int) rowsPerPage,
                                Sort.Direction.ASC, "id"));
                page.forEach(it -> {
                    final Map setting = esTemplate.getSetting(it.getId());
                    log.trace("Setting for {}: {}", it.getId(), setting);
                    final long indexCreationDate = Long.parseLong((String) setting.get("index.creation_date"));
                    it.setCreationTime(OffsetDateTime.ofInstant(Instant.ofEpochMilli(indexCreationDate), ZoneId.of("Asia/Jakarta")));

                    final Map core2 = esTemplate.getMapping(it.getId(), "core2");
                    log.trace("Mapping core2 for {}: {}", it.getId(), core2);
                    final Map<String, Object> properties = (Map<String, Object>) core2.get("properties");
                    it.setProperties(ImmutableList.copyOf(properties.keySet()));
                });

                return page.iterator();
            }

            @Override
            public long size() {
                return searchIndexRepo.count();
            }

            @Override
            public IModel<SearchIndex> model(SearchIndex object) {
                return new Model<>(object);
            }
        };
        add(new BootstrapDefaultDataTable<>("searchIndexTable", columns, searchIndexDp, rowsPerPage));
    }

    @Override
    public IModel<String> getTitleModel() {
        return new Model<>("Indexes | Lumen Helpdesk");
    }

    private static class IdColumn extends AbstractColumn<SearchIndex, String> {
        public IdColumn() {
            super(new Model<>("ID"), "id");
        }

        @Override
        public void populateItem(Item<ICellPopulator<SearchIndex>> cellItem, String componentId, IModel<SearchIndex> rowModel) {
//            final BookmarkablePageLink<Object> link = new BookmarkablePageLink<>(componentId, SearchIndexEditPage.class,
//                    new PageParameters().set("searchIndexId", rowModel.getObject().getId()));
//            link.setBody(new PropertyModel<>(rowModel, "id"));
//            link.setRenderBodyOnly(false);
//            cellItem.add(link);
            cellItem.add(new IdPanel(componentId, new PropertyModel<>(rowModel, "id"),
                    SearchIndexEditPage.class,
                    "searchIndexId", rowModel.getObject().getId()));
        }
    }
}
