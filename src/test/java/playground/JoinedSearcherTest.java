package playground;

import com.google.common.collect.ImmutableList;
import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class JoinedSearcherTest {

    private RAMDirectory index;
    private JoinedSearcher indexManager;

    @Before
    public void initialize() {
        this.index = new RAMDirectory();
        this.indexManager = new JoinedSearcher();
    }

    @Test
    public void testCreateWriter() throws Exception {
        Assert.assertNotNull(indexManager.createWriter(index));
    }

    @Test
    public void testAddDocuments() throws Exception {
        insertDocuments();

        // get number of documents
        IndexReader reader = DirectoryReader.open(index);
        Assert.assertEquals(reader.getDocCount("key"), 3);
    }

    @Test
    public void testAddDocumentsAndFind() throws Exception {
        insertDocuments();

        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);

        // get number of documents
        TopScoreDocCollector collector = TopScoreDocCollector.create(5, true);
        Query qPrefix = new PrefixQuery(new Term("name", "rota"));
        searcher.search(qPrefix, collector);

        Assert.assertEquals(2, collector.topDocs().scoreDocs.length);

        TopScoreDocCollector ccollector = TopScoreDocCollector.create(5, true);
        Query qTerm = new TermQuery(new Term("name", "vertigo"));
        searcher.search(qTerm, ccollector);

        Assert.assertEquals(2, ccollector.topDocs().scoreDocs.length);
    }

    private void insertDocuments() throws IOException {
        ImmutableList<Document> docs = ImmutableList.<Document>of(getDoc("ikey1", "rotatory", null),
                getDoc("ikey2", "rotatory vertigo", null),
                getDoc("ikey3", "absent vertigo", null)
        );

        IndexWriter writer = indexManager.createWriter(index);
        writer.addDocuments(docs);
        writer.commit();
    }

    public Document getDoc(String key, String name, String docType) {
        Document doc = new Document();
        doc.add(new TextField("key", key, Field.Store.YES));
        doc.add(new TextField("name", name, Field.Store.YES));

        if(docType != null) {
            doc.add(new TextField("docType", docType, Field.Store.YES));
        }

        return doc;
    }
}
