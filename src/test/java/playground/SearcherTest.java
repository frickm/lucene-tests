package playground;

import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;

public abstract class SearcherTest {
    protected RAMDirectory index;
    protected JoinedSearcher indexManager;

    @Before
    public void initialize() {
        this.index = new RAMDirectory();
        this.indexManager = new JoinedSearcher();
    }
}
