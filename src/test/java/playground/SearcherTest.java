package playground;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
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

    protected Document getDoc(String key, String name, String docType) {
        Document doc = new Document();
        doc.add(new TextField("key", key, Field.Store.YES));
        doc.add(new TextField("name", name, Field.Store.YES));

        if(docType != null) {
            doc.add(new TextField("docType", docType, Field.Store.YES));
        }

        return doc;
    }
}
